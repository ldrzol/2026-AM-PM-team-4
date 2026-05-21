package com.mintly.app.data.repository

import com.mintly.app.data.model.ChatMessage
import com.mintly.app.data.model.ChatRoom
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    suspend fun getGroupChatRooms(groupId: String): List<ChatRoom> = runCatching {
        client.from("chat_rooms")
            .select { filter { eq("group_id", groupId) } }
            .decodeList<ChatRoom>()
    }.getOrElse { emptyList() }

    suspend fun getOrCreateChatRoom(groupId: String, name: String): ChatRoom = runCatching {
        val existing = client.from("chat_rooms")
            .select { filter { eq("group_id", groupId) } }
            .decodeList<ChatRoom>()
            .firstOrNull()
        existing ?: client.from("chat_rooms")
            .insert(mapOf("group_id" to groupId, "name" to name))
            .decodeSingle<ChatRoom>()
    }.getOrThrow()

    suspend fun getMessages(roomId: String, limit: Int = 50): List<ChatMessage> = runCatching {
        client.from("chat_messages")
            .select(Columns.raw("*, profile:profiles(id, display_name, avatar_color, avatar_face, current_hat, current_outfit, has_crown_until)")) {
                filter { eq("room_id", roomId) }
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ChatMessage>()
            .reversed()
    }.getOrElse { emptyList() }

    suspend fun sendMessage(roomId: String, content: String): Result<Unit> = runCatching {
        val uid = client.auth.currentUserOrNull()?.id ?: error("Not logged in")
        client.from("chat_messages")
            .insert(mapOf("room_id" to roomId, "user_id" to uid, "content" to content))
    }

    fun messageFlow(roomId: String): Flow<ChatMessage> = flow {
        val channel = client.channel("chat-$roomId")
        val msgJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "chat_messages"
            filter(FilterOperation("room_id", FilterOperator.EQ, roomId))
        }
        channel.subscribe()
        changeFlow.collect { action ->
            val msg = runCatching {
                msgJson.decodeFromString<ChatMessage>(action.record.toString())
            }.getOrNull()
            if (msg != null && msg.content.isNotBlank()) emit(msg)
        }
    }
}
