package com.mintly.app.data.repository

import com.mintly.app.data.model.Profile
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    suspend fun getMyProfile(): Profile? {
        val uid = client.auth.currentUserOrNull()?.id ?: return null
        return runCatching {
            client.from("profiles")
                .select { filter { eq("id", uid) } }
                .decodeSingleOrNull<Profile>()
        }.getOrNull()
    }

    suspend fun updateProfile(updates: Map<String, Any?>): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        client.from("profiles").update(updates) { filter { eq("id", uid) } }
    }

    suspend fun updateAvatar(color: String, face: Map<String, String>): Result<Unit> =
        updateProfile(mapOf("avatar_color" to color, "avatar_face" to face))

    suspend fun equipHat(hatId: String?): Result<Unit> =
        updateProfile(mapOf("current_hat" to hatId))

    suspend fun equipOutfit(outfitId: String?): Result<Unit> =
        updateProfile(mapOf("current_outfit" to outfitId))

    suspend fun updateShareMode(mode: String): Result<Unit> =
        updateProfile(mapOf("share_mode" to mode))

    suspend fun updateDisplayName(name: String): Result<Unit> =
        updateProfile(mapOf("display_name" to name))

    suspend fun updateUsername(username: String): Result<Unit> =
        updateProfile(mapOf("username" to username))

    suspend fun getGroupMemberProfiles(groupId: String): List<Profile> = runCatching {
        client.from("group_members")
            .select(Columns.raw("profile:profiles(*)")) { filter { eq("group_id", groupId) } }
            .decodeList<Map<String, Profile>>()
            .mapNotNull { it["profile"] }
    }.getOrElse { emptyList() }
}
