package com.mintly.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
import com.mintly.app.data.model.Transaction
import com.mintly.app.data.model.UserCostume
import com.mintly.app.data.repository.GroupRepository
import com.mintly.app.data.repository.ProfileRepository
import com.mintly.app.data.repository.RankingRepository
import com.mintly.app.data.repository.ShopRepository
import com.mintly.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val profile: Profile? = null,
    val todayIncome: Int = 0,
    val todayExpense: Int = 0,
    val monthlyIncome: Int = 0,
    val monthlyExpense: Int = 0,
    val monthlyStats: Map<Category, Int> = emptyMap(),
    val inventory: List<UserCostume> = emptyList(),
    val friendsRanking: List<RankedMember> = emptyList(),
    val groupId: String? = null,
    val checkinWeekDays: List<Boolean> = List(7) { false },
    val hasCheckedToday: Boolean = false,
    val checkInMessage: String? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val txRepo: TransactionRepository,
    private val shopRepo: ShopRepository,
    private val groupRepo: GroupRepository,
    private val rankingRepo: RankingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val profile = profileRepo.getMyProfile()
            val now     = LocalDate.now()

            // 이번달 거래 한 번만 조회
            val allMonthTx = txRepo.getTransactionsForMonth(now.year, now.monthValue)
            val todayStr   = "%04d-%02d-%02d".format(now.year, now.monthValue, now.dayOfMonth)
            val todayTx    = allMonthTx.filter { it.occurredOn == todayStr }

            val monthlyStats    = txRepo.getMonthlyCategoryStats(now.year, now.monthValue)
            val monthlyIncome   = allMonthTx.filter { it.kind == "income" }.sumOf { it.amount }
            val monthlyExpense  = allMonthTx.filter { it.kind == "expense" }.sumOf { it.amount }

            val inventory = shopRepo.getMyInventory()

            // 그룹 & 친구 랭킹
            val groups     = groupRepo.getMyGroups()
            val firstGroup = groups.firstOrNull()
            val ranking    = if (firstGroup != null) {
                runCatching { rankingRepo.getTodayRanking(firstGroup.id) }.getOrElse { emptyList() }
            } else emptyList()

            // 출석체크
            val weekDays      = computeCheckinWeek(profile?.checkStreak ?: 0, profile?.lastCheckin)
            val hasChecked    = profile?.lastCheckin == now.toString()

            _uiState.value = HomeUiState(
                profile        = profile,
                todayIncome    = todayTx.filter { it.kind == "income" }.sumOf { it.amount },
                todayExpense   = todayTx.filter { it.kind == "expense" }.sumOf { it.amount },
                monthlyIncome  = monthlyIncome,
                monthlyExpense = monthlyExpense,
                monthlyStats   = monthlyStats,
                inventory      = inventory,
                friendsRanking = ranking,
                groupId        = firstGroup?.id,
                checkinWeekDays = weekDays,
                hasCheckedToday = hasChecked,
                isLoading      = false,
            )
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            profileRepo.checkIn().fold(
                onSuccess = { coins ->
                    if (coins > 0) {
                        _uiState.value = _uiState.value.copy(
                            checkInMessage = "+${coins}코인 획득! 🎉",
                            hasCheckedToday = true,
                        )
                        load()
                        kotlinx.coroutines.delay(2500)
                        _uiState.value = _uiState.value.copy(checkInMessage = null)
                    }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun equipHat(hatId: String?) {
        viewModelScope.launch { profileRepo.equipHat(hatId); load() }
    }

    fun equipOutfit(outfitId: String?) {
        viewModelScope.launch { profileRepo.equipOutfit(outfitId); load() }
    }

    fun updateAvatar(color: String, face: Map<String, String>) {
        viewModelScope.launch { profileRepo.updateAvatar(color, face); load() }
    }

    // ─── 출석 주간 계산 ───────────────────────────────────────
    private fun computeCheckinWeek(streak: Int, lastCheckin: String?): List<Boolean> {
        if (streak <= 0 || lastCheckin == null) return List(7) { false }
        val today    = LocalDate.now()
        val dow      = today.dayOfWeek.value   // Mon=1, Sun=7
        val lastDate = runCatching { LocalDate.parse(lastCheckin) }.getOrNull()
            ?: return List(7) { false }
        return (1..7).map { d ->
            val dayDate     = today.minusDays((dow - d).toLong())
            val daysFromLast = ChronoUnit.DAYS.between(dayDate, lastDate)
            !dayDate.isAfter(today) && daysFromLast in 0 until streak
        }
    }
}
