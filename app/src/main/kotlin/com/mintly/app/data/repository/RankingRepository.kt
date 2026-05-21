package com.mintly.app.data.repository

import com.mintly.app.data.model.DailyRanking
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepository @Inject constructor(
    private val supabase: SupabaseManager,
) {
    private val client get() = supabase.client

    suspend fun getTodayRanking(groupId: String): List<RankedMember> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val members = getGroupMembersTodaySpend(groupId, today)
        return members
    }

    private suspend fun getGroupMembersTodaySpend(groupId: String, date: String): List<RankedMember> {
        return runCatching {
            // 오늘 랭킹 테이블에 정산된 데이터 사용 (없으면 transactions 실시간 합산)
            val rankings = client.from("daily_rankings")
                .select(Columns.raw("*, profile:profiles(*)")) {
                    filter {
                        eq("group_id", groupId)
                        eq("for_date", date)
                    }
                    order("rank", Order.ASCENDING)
                }
                .decodeList<DailyRanking>()

            if (rankings.isNotEmpty()) {
                rankings.mapNotNull { r ->
                    val profile = r.profile ?: return@mapNotNull null
                    RankedMember(profile, r.spentAmount, r.rank, r.isWinner, r.isLoser, "%,d원".format(r.spentAmount))
                }
            } else {
                // 정산 전이라면 transactions 테이블에서 실시간 합산
                getLiveGroupSpend(groupId, date)
            }
        }.getOrElse { emptyList() }
    }

    private suspend fun getLiveGroupSpend(groupId: String, date: String): List<RankedMember> {
        val members = client.from("group_members")
            .select(Columns.raw("*, profile:profiles(*)")) { filter { eq("group_id", groupId) } }
            .decodeList<Map<String, Any?>>()

        // 각 멤버의 오늘 지출 합산
        val spendList = members.mapNotNull { row ->
            @Suppress("UNCHECKED_CAST")
            val profileMap = row["profile"] as? Map<String, Any?> ?: return@mapNotNull null
            val userId = profileMap["id"] as? String ?: return@mapNotNull null
            val displayName = profileMap["display_name"] as? String ?: ""
            val spent = runCatching {
                client.from("transactions")
                    .select(Columns.raw("amount")) {
                        filter {
                            eq("user_id", userId)
                            eq("group_id", groupId)
                            eq("occurred_on", date)
                            eq("kind", "expense")
                        }
                    }
                    .decodeList<Map<String, Int>>()
                    .sumOf { it["amount"] ?: 0 }
            }.getOrElse { 0 }
            Pair(userId, spent)
        }.sortedBy { it.second }

        return spendList.mapIndexed { idx, (uid, spent) ->
            val profileRow = members.find {
                @Suppress("UNCHECKED_CAST")
                (it["profile"] as? Map<String, Any?>)?.get("id") == uid
            }
            @Suppress("UNCHECKED_CAST")
            val pm = profileRow?.get("profile") as? Map<String, Any?> ?: return@mapIndexed null
            val profile = Profile(
                id = uid,
                username = pm["username"] as? String ?: "",
                displayName = pm["display_name"] as? String ?: "",
                avatarColor = pm["avatar_color"] as? String ?: "mint",
                avatarFace = (pm["avatar_face"] as? Map<*, *>)
                    ?.entries?.associate { (k, v) -> k.toString() to v.toString() }
                    ?: emptyMap(),
                coins = (pm["coins"] as? Number)?.toInt() ?: 0,
                shareMode = pm["share_mode"] as? String ?: "percent",
                currentHat = pm["current_hat"] as? String,
                currentOutfit = pm["current_outfit"] as? String,
                hasCrownUntil = pm["has_crown_until"] as? String,
                forcedOutfit = pm["forced_outfit"] as? String,
            )
            RankedMember(profile, spent, idx + 1, idx == 0, idx == spendList.lastIndex, "${spent}원")
        }.filterNotNull()
    }

    fun rankingChanges(groupId: String): Flow<Unit> = flow {
        val channel = client.channel("ranking-$groupId")
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "daily_rankings"
            filter(FilterOperation("group_id", FilterOperator.EQ, groupId))
        }.collect { emit(Unit) }
    }
}
