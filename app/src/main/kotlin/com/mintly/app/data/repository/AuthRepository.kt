package com.mintly.app.data.repository

import com.mintly.app.data.model.Profile
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
    ): Result<Unit> = runCatching {
        // on_auth_user_created 트리거가 profiles 행을 자동 생성하므로 별도 insert 불필요
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("username", username)
                put("display_name", username)
            }
        }
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        client.auth.signOut()
    }

    suspend fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    suspend fun getProfile(userId: String): Profile? = runCatching {
        client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<Profile>()
    }.getOrNull()

    suspend fun isLoggedIn(): Boolean = client.auth.currentUserOrNull() != null

    suspend fun changePassword(newPassword: String): Result<Unit> = runCatching {
        client.auth.updateUser { password = newPassword }
    }

    suspend fun changeEmail(newEmail: String): Result<Unit> = runCatching {
        // auth.updateUser 대신 profiles 테이블 직접 업데이트 → 인증 링크 미발송
        val userId = client.auth.currentUserOrNull()?.id ?: error("로그인이 필요합니다")
        val json = buildJsonObject { put("email", newEmail) }
        client.from("profiles").update(json) { filter { eq("id", userId) } }
    }
}
