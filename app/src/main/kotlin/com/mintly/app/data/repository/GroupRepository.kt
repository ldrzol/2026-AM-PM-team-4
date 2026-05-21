package com.mintly.app.data.repository

import com.mintly.app.data.model.FriendGroup
import com.mintly.app.data.model.GroupMember
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    // ─── 초대코드 생성 ───────────────────────────────────────
    // 36^6 = 약 22억 가지 → 중복 확률 무시 가능, DB SELECT 없이 생성
    private val CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    private fun generateCode(): String =
        (1..6).map { CODE_CHARS.random() }.joinToString("")

    // ─── 그룹 조회 ───────────────────────────────────────────
    suspend fun getMyGroups(): List<FriendGroup> {
        val uid = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return runCatching {
            val memberRows = client.from("group_members")
                .select(Columns.raw("group:friend_groups(*)")) {
                    filter { eq("user_id", uid) }
                }
                .decodeList<Map<String, FriendGroup>>()
            memberRows.mapNotNull { it["group"] }
        }.getOrElse { emptyList() }
    }

    suspend fun createGroup(name: String, emoji: String): Result<FriendGroup> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        val inviteCode = generateCode()  // DB SELECT 없이 바로 생성 (RLS 재귀 완전 차단)
        // 클라이언트에서 UUID 직접 생성 → SELECT 재조회 불필요
        val groupId = java.util.UUID.randomUUID().toString()

        // INSERT — id를 명시적으로 지정
        client.from("friend_groups")
            .insert(mapOf(
                "id"          to groupId,
                "name"        to name,
                "emoji"       to emoji,
                "owner_id"    to uid,
                "invite_code" to inviteCode,
            ))

        // 방장을 멤버로 추가 — group_id 이미 알고 있음
        client.from("group_members").insert(
            mapOf("group_id" to groupId, "user_id" to uid)
        )

        // SELECT 없이 로컬에서 객체 반환
        FriendGroup(
            id         = groupId,
            name       = name,
            emoji      = emoji,
            inviteCode = inviteCode,
            ownerId    = uid,
        )
    }

    suspend fun joinGroup(inviteCode: String): Result<FriendGroup> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        val group = client.from("friend_groups")
            .select { filter { eq("invite_code", inviteCode.uppercase()) } }
            .decodeSingleOrNull<FriendGroup>() ?: error("방을 찾을 수 없습니다")
        client.from("group_members").insert(
            mapOf("group_id" to group.id, "user_id" to uid)
        )
        group
    }

    suspend fun getGroupMembers(groupId: String): List<GroupMember> = runCatching {
        client.from("group_members")
            .select(Columns.raw("*, profile:profiles(*)")) {
                filter { eq("group_id", groupId) }
            }
            .decodeList<GroupMember>()
    }.getOrElse { emptyList() }

    suspend fun leaveGroup(groupId: String): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        client.from("group_members").delete {
            filter {
                eq("group_id", groupId)
                eq("user_id", uid)
            }
        }
    }
}
