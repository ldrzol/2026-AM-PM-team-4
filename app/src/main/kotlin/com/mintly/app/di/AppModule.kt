package com.mintly.app.di

import android.content.Context
import com.mintly.app.data.supabase.SupabaseManager
import com.mintly.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseManager(
        @ApplicationContext context: Context,
    ): SupabaseManager = SupabaseManager(context)

    @Provides
    @Singleton
    fun provideAuthRepository(supabase: SupabaseManager): AuthRepository =
        AuthRepository(supabase)

    @Provides
    @Singleton
    fun provideProfileRepository(supabase: SupabaseManager): ProfileRepository =
        ProfileRepository(supabase)

    @Provides
    @Singleton
    fun provideTransactionRepository(supabase: SupabaseManager): TransactionRepository =
        TransactionRepository(supabase)

    @Provides
    @Singleton
    fun provideGroupRepository(supabase: SupabaseManager): GroupRepository =
        GroupRepository(supabase)

    @Provides
    @Singleton
    fun provideRankingRepository(supabase: SupabaseManager): RankingRepository =
        RankingRepository(supabase)

    @Provides
    @Singleton
    fun provideShopRepository(supabase: SupabaseManager): ShopRepository =
        ShopRepository(supabase)

    @Provides
    @Singleton
    fun provideChatRepository(supabase: SupabaseManager): ChatRepository =
        ChatRepository(supabase)
}
