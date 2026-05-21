package com.mintly.app.data.supabase

import android.content.Context
import com.mintly.app.BuildConfig
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context
) {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Postgrest)
        install(Realtime)
        install(Auth) {
            val prefs = context.getSharedPreferences("거지방_session", Context.MODE_PRIVATE)
            sessionManager = io.github.jan.supabase.auth.SettingsSessionManager(
                SharedPreferencesSettings(prefs)
            )
            autoLoadFromStorage = true
            autoSaveToStorage   = true
        }
    }

    val sessionStatus: StateFlow<SessionStatus>
        get() = client.auth.sessionStatus

    suspend fun currentUserId(): String? =
        client.auth.currentUserOrNull()?.id
}
