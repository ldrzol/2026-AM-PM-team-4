package com.mintly.app.data.repository

import com.mintly.app.data.model.DailyRanking
import com.mintly.app.data.model.GroupMember
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
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
        return getGroupMembersTodaySpend(groupId, today)
    }

    private suspend fun getGroupMembersTodaySpend(groupId: String, date: String): List<RankedMember> {
        return runCatching {
            // 정산된 데이터가 있으면 사용, 없으면 실시간 transactions 합산
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
                    val income = runCatching {
                        client.from("transactions")
                            .select(Columns.raw("amount")) {
                                filter {
                                    eq("user_id",     r.userId)
                                    eq("group_id",    groupId)
                                    eq("occurred_on", date)
                                    eq("kind",        "income")
                                }
                            }
                            .decodeList<Map<String, Int>>()
                            .sumOf { it["amount"] ?: 0 }
                    }.getOrElse { 0 }
                    val sv = buildShareValue(r.spentAmount, income)
                    RankedMember(profile, r.spentAmount, income, r.rank, r.isWinner, r.isLoser, sv)
                }
            } else {
                getLiveGroupSpend(groupId, date)
            }
        }.getOrElse { emptyList() }
    }

    /**
     * 자정 정산 전 실시간 랭킹.
     * - Map<String,Any?> 대신 @Serializable GroupMember 사용 (supabase-kt 3.x 호환)
     * - 수입 대비 지출 % 기준 오름차순 (낮을수록 1위)
     * - 한 명이라도 입력하면 순위 표시, 미입력자는 맨 아래 "미입력" 표기
     */
    private suspend fun getLiveGroupSpend(groupId: String, date: String): List<RankedMember> {
        val members = client.from("group_members")
            .select(Columns.raw("*, profile:profiles(*)")) { filter { eq("group_id", groupId) } }
            .decodeList<GroupMember>()

        data class MemberSpend(val profile: Profile, val spent: Int, val income: Int)

        val spendList = members.mapNotNull { member ->
            val profile = member.profile ?: return@mapNotNull null

            val spent = runCatching {
                client.from("transactions")
                    .select(Columns.raw("amount")) {
                        filter {
                            eq("user_id",     profile.id)
                            eq("group_id",    groupId)
                            eq("occurred_on", date)
                            eq("kind",        "expense")
                        }
                    }
                    .decodeList<Map<String, Int>>()
                    .sumOf { it["amount"] ?: 0 }
            }.getOrElse { 0 }

            val income = runCatching {
                client.from("transactions")
                    .select(Columns.raw("amount")) {
                        filter {
                            eq("user_id",     profile.id)
                            eq("group_id",    groupId)
                            eq("occurred_on", date)
                            eq("kind",        "income")
                        }
                    }
                    .decodeList<Map<String, Int>>()
                    .sumOf { it["amount"] ?: 0 }
            }.getOrElse { 0 }

            MemberSpend(profile, spent, income)
        }

        // 수입·지출 모두 입력한 멤버만 순위에 포함, 나머지는 미입력으로 표시
        val active   = spendList.filter { it.spent > 0 && it.income > 0 }
        val inactive = spendList.filter { it.spent == 0 || it.income == 0 }

        // 수입 대비 지출 % 오름차순 (수입 없으면 절대 지출액 기준)
        val sortedActive = active.sortedWith(
            compareBy(
                { ms: MemberSpend ->
                    if (ms.income > 0) ms.spent.toDouble() / ms.income.toDouble()
                    else Double.MAX_VALUE          // 수입 0 · 지출 있음 → 꼴등 후보
                },
                { ms: MemberSpend -> ms.spent },   // 동점 시 절대 금액 낮은 쪽이 앞
            )
        )

        val rankedActive = sortedActive.mapIndexed { idx, ms ->
            RankedMember(
                profile      = ms.profile,
                spentAmount  = ms.spent,
                incomeAmount = ms.income,
                rank         = idx + 1,
                isWinner     = idx == 0,
                isLoser      = sortedActive.size > 1 && idx == sortedActive.lastIndex,
                shareValue   = buildShareValue(ms.spent, ms.income),
                isNotEntered = false,
            )
        }

        // 미입력자: 순위 없음, 맨 아래 배치
        val rankedInactive = inactive.map { ms ->
            RankedMember(
                profile      = ms.profile,
                spentAmount  = 0,
                incomeAmount = 0,
                rank         = sortedActive.size + 1,
                isWinner     = false,
                isLoser      = false,
                shareValue   = "미입력",
                isNotEntered = true,
            )
        }

        return rankedActive + rankedInactive
    }

    /** 랭킹 행 요약 텍스트 (수입 대비 지출 %) */
    private fun buildShareValue(spent: Int, income: Int): String =
        if (income > 0) {
            val pct = (spent.toDouble() / income * 100).toInt()
            "지출 ${pct}%  ·  %,d원".format(spent)
        } else {
            "지출 %,d원".format(spent)
        }

    fun rankingChanges(groupId: String): Flow<Unit> = flow {
        val channel = client.channel("ranking-$groupId")
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "daily_rankings"
            filter(FilterOperation("group_id", FilterOperator.EQ, groupId))
        }.collect { emit(Unit) }
    }
}
