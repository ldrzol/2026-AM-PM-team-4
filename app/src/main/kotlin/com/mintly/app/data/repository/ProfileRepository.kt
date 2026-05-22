package com.mintly.app.data.repository

import com.mintly.app.data.model.Profile
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

@Singleton
class ProfileRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    suspend fun getMyProfile(): Profile? {
        val uid = client.auth.currentUserOrNull()?.id ?: return null
        val profile = runCatching {
            client.from("profiles")
                .select { filter { eq("id", uid) } }
                .decodeSingleOrNull<Profile>()
        }.getOrNull()
        return profile?.let { normalizeMyProfile(uid, it) } ?: ensureMyProfile(uid)
    }

    private suspend fun ensureMyProfile(uid: String): Profile? {
        val nickname = "user_${uid.take(6)}"
        return runCatching {
            client.from("profiles").insert(
                mapOf(
                    "id" to uid,
                    "username" to nickname,
                    "display_name" to nickname,
                )
            )
            Profile(id = uid, username = nickname, displayName = nickname)
        }.getOrElse {
            runCatching {
                client.from("profiles")
                    .select { filter { eq("id", uid) } }
                    .decodeSingleOrNull<Profile>()
            }.getOrNull()
        }
    }

    private suspend fun normalizeMyProfile(uid: String, profile: Profile): Profile {
        val nickname = profile.displayName.takeIf { it.isNotBlank() }
            ?: profile.username.takeIf { it.isNotBlank() }
            ?: "user_${uid.take(6)}"

        if (profile.displayName.isNotBlank() && profile.username.isNotBlank()) {
            return profile
        }

        val updates = buildJsonObject {
            if (profile.displayName.isBlank()) put("display_name", nickname)
            if (profile.username.isBlank()) put("username", nickname)
        }
        runCatching {
            client.from("profiles").update(updates) { filter { eq("id", uid) } }
        }

        return profile.copy(
            username = profile.username.ifBlank { nickname },
            displayName = profile.displayName.ifBlank { nickname },
        )
    }

    suspend fun updateProfile(updates: Map<String, Any?>): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        ensureMyProfile(uid)
        val json = buildJsonObject {
            updates.forEach { (k, v) ->
                when (v) {
                    null      -> put(k, JsonNull)
                    is String  -> put(k, v)
                    is Int     -> put(k, v)
                    is Boolean -> put(k, v)
                    is Map<*, *> -> {
                        put(k, buildJsonObject {
                            v.forEach { (mapKey, mapValue) ->
                                if (mapKey is String && mapValue is String) put(mapKey, mapValue)
                            }
                        })
                    }
                    else       -> put(k, v.toString())
                }
            }
        }
        client.from("profiles").update(json) { filter { eq("id", uid) } }
    }

    suspend fun updateAvatar(color: String, face: Map<String, String>): Result<Unit> =
        updateProfile(mapOf("avatar_color" to color, "avatar_face" to face))

    suspend fun equipHat(hatId: String?): Result<Unit> =
        updateProfile(mapOf("current_hat" to hatId))

    suspend fun equipOutfit(outfitId: String?): Result<Unit> =
        updateProfile(mapOf("current_outfit" to outfitId))

    suspend fun updateShareMode(mode: String): Result<Unit> =
        updateProfile(mapOf("share_mode" to mode))

    suspend fun updateDisplayName(name: String): Result<Unit> {
        val nickname = name.trim()
        return if (nickname.isBlank()) {
            Result.failure(IllegalArgumentException("닉네임을 입력해주세요"))
        } else {
            updateProfile(mapOf("display_name" to nickname))
        }
    }

    suspend fun updateUsername(username: String): Result<Unit> =
        updateProfile(mapOf("username" to username))

    /** 출석체크 — 이미 오늘 했으면 0, 처음이면 지급된 코인 수 반환 */
    suspend fun checkIn(): Result<Int> = runCatching {
        client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        runCatching {
            client.postgrest.rpc("check_in", buildJsonObject { }).decodeAs<Int>()
        }.getOrElse {
            checkInLocally()
        }
    }

    private suspend fun checkInLocally(): Int {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        val profile = getMyProfile() ?: error("Profile not found")

        val today = LocalDate.now(KOREA_ZONE).toString()
        if (profile.lastCheckin == today) return 0

        val yesterday   = LocalDate.now(KOREA_ZONE).minusDays(1).toString()
        val newStreak   = if (profile.lastCheckin == yesterday) profile.checkStreak + 1 else 1
        val coinsToAdd  = 50

        val json = buildJsonObject {
            put("check_streak", newStreak)
            put("last_checkin", today)
            put("coins", profile.coins + coinsToAdd)
        }
        client.from("profiles").update(json) { filter { eq("id", uid) } }
        return coinsToAdd
    }

    suspend fun getGroupMemberProfiles(groupId: String): List<Profile> = runCatching {
        client.from("group_members")
            .select(Columns.raw("profile:profiles(*)")) { filter { eq("group_id", groupId) } }
            .decodeList<Map<String, Profile>>()
            .mapNotNull { it["profile"] }
    }.getOrElse { emptyList() }
}
