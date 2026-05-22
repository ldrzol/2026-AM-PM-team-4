package com.mintly.app.data.repository

import com.mintly.app.data.model.GroupMember
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
import com.mintly.app.data.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

@Serializable
private data class RankingMemberRow(
    @SerialName("user_id") val userId: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_seed") val avatarSeed: String? = null,
    @SerialName("avatar_color") val avatarColor: String? = null,
    @SerialName("avatar_face") val avatarFace: Map<String, String>? = null,
    val email: String? = null,
    val coins: Int? = null,
    val phone: String? = null,
    @SerialName("current_hat") val currentHat: String? = null,
    @SerialName("current_outfit") val currentOutfit: String? = null,
    @SerialName("forced_outfit") val forcedOutfit: String? = null,
    @SerialName("forced_until") val forcedUntil: String? = null,
    @SerialName("has_crown_until") val hasCrownUntil: String? = null,
    @SerialName("share_mode") val shareMode: String? = null,
    @SerialName("check_streak") val checkStreak: Int? = null,
    @SerialName("last_checkin") val lastCheckin: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("spent_amount") val spentAmount: Long? = null,
    @SerialName("income_amount") val incomeAmount: Long? = null,
) {
    fun toProfile(): Profile =
        Profile(
            id = userId,
            username = username.orEmpty(),
            displayName = displayName.orEmpty(),
            avatarSeed = avatarSeed ?: "default",
            avatarColor = avatarColor ?: "mint",
            avatarFace = avatarFace,
            email = email,
            coins = coins ?: 0,
            phone = phone,
            currentHat = currentHat,
            currentOutfit = currentOutfit,
            forcedOutfit = forcedOutfit,
            forcedUntil = forcedUntil,
            hasCrownUntil = hasCrownUntil,
            shareMode = shareMode ?: "percent",
            checkStreak = checkStreak ?: 0,
            lastCheckin = lastCheckin,
            createdAt = createdAt,
        )
}

private data class MemberSpend(
    val profile: Profile,
    val spent: Int,
    val income: Int,
)

@Singleton
class RankingRepository @Inject constructor(
    private val supabase: SupabaseManager,
    private val groupRepo: GroupRepository,
) {
    private val client get() = supabase.client

    suspend fun getTodayRanking(groupId: String): List<RankedMember> {
        return getLiveRanking(groupId, rankingDate())
    }

    suspend fun getTodayRankingMemberCount(groupId: String): Int {
        return getRankingRows(groupId, rankingDate()).size
            .coerceAtLeast(groupRepo.getGroupMemberCount(groupId))
            .coerceAtLeast(getBasicRoomMembers(groupId).size)
    }

    private suspend fun getLiveRanking(groupId: String, date: String): List<RankedMember> {
        val members = mergeRoomMembers(
            rankedRows = getRankingRows(groupId, date),
            roomMembers = groupRepo.getGroupMembers(groupId) + getBasicRoomMembers(groupId),
        )

        val active = members.filter { it.income > 0 }
        val inactive = members.filter { it.income <= 0 }
        val sortedActive = active.sortedWith(
            compareBy(
                { spendingRatio(it) },
                { it.spent },
                { it.profile.displayName.ifBlank { it.profile.username } },
            )
        )

        val rankedActive = sortedActive.mapIndexed { index, member ->
            RankedMember(
                profile = member.profile,
                spentAmount = member.spent,
                incomeAmount = member.income,
                rank = index + 1,
                isWinner = index == 0,
                isLoser = sortedActive.size > 1 && index == sortedActive.lastIndex,
                shareValue = buildShareValue(member.spent, member.income),
                isNotEntered = false,
            )
        }

        val inactiveRank = rankedActive.size + 1
        val rankedInactive = inactive.map { member ->
            RankedMember(
                profile = member.profile,
                spentAmount = member.spent,
                incomeAmount = member.income,
                rank = inactiveRank,
                isWinner = false,
                isLoser = false,
                shareValue = "순위권 밖",
                isNotEntered = true,
            )
        }

        return rankedActive + rankedInactive
    }

    private suspend fun getRankingRows(groupId: String, date: String): List<RankingMemberRow> =
        runCatching {
            client.postgrest
                .rpc("get_group_ranking_members", buildJsonObject {
                    put("p_group_id", groupId)
                    put("p_date", date)
                })
                .decodeList<RankingMemberRow>()
        }.getOrElse { emptyList() }

    private fun mergeRoomMembers(
        rankedRows: List<RankingMemberRow>,
        roomMembers: List<GroupMember>,
    ): List<MemberSpend> {
        val byUserId = linkedMapOf<String, MemberSpend>()

        rankedRows.forEach { row ->
            byUserId[row.userId] = MemberSpend(
                profile = row.toProfile(),
                spent = row.spentAmount?.toInt() ?: 0,
                income = row.incomeAmount?.toInt() ?: 0,
            )
        }

        roomMembers.forEach { member ->
            if (member.userId.isNotBlank() && byUserId[member.userId] == null) {
                byUserId[member.userId] = MemberSpend(
                    profile = member.profile ?: fallbackProfile(member.userId),
                    spent = 0,
                    income = 0,
                )
            }
        }

        return byUserId.values.toList()
    }

    private fun fallbackProfile(userId: String): Profile =
        Profile(
            id = userId,
            username = "friend",
            displayName = "친구",
        )

    private suspend fun getBasicRoomMembers(groupId: String): List<GroupMember> =
        runCatching {
            client.from("group_members")
                .select {
                    filter { eq("group_id", groupId) }
                }
                .decodeList<GroupMember>()
        }.getOrElse { emptyList() }

    private fun rankingDate(): String {
        val now = LocalDateTime.now(KOREA_ZONE)
        val date = if (now.hour < 4) now.toLocalDate().minusDays(1) else now.toLocalDate()
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun spendingRatio(member: MemberSpend): Double =
        member.spent.toDouble() / member.income.toDouble()

    private fun buildShareValue(spent: Int, income: Int): String =
        if (income > 0) {
            val percent = (spent.toDouble() / income * 100).toInt()
            "지출 ${percent}% · %,d원".format(spent)
        } else {
            "수익 미입력 · 지출 %,d원".format(spent)
        }

    fun rankingChanges(groupId: String): Flow<Unit> = flow {
        val channel = client.channel("ranking-$groupId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "daily_rankings"
            filter(FilterOperation("group_id", FilterOperator.EQ, groupId))
        }
        channel.subscribe()
        changeFlow.collect { emit(Unit) }
    }

    fun transactionChanges(groupId: String): Flow<Unit> = flow {
        val channel = client.channel("tx-ranking-$groupId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "transactions"
            filter(FilterOperation("group_id", FilterOperator.EQ, groupId))
        }
        channel.subscribe()
        changeFlow.collect { emit(Unit) }
    }

    fun groupMemberChanges(groupId: String): Flow<Unit> = flow {
        val channel = client.channel("members-ranking-$groupId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "group_members"
            filter(FilterOperation("group_id", FilterOperator.EQ, groupId))
        }
        channel.subscribe()
        changeFlow.collect { emit(Unit) }
    }

    fun anyRankingChange(groupId: String): Flow<Unit> =
        merge(rankingChanges(groupId), transactionChanges(groupId), groupMemberChanges(groupId))
}
