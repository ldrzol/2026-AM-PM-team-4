package com.mintly.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.Transaction
import com.mintly.app.data.model.UserCostume
import com.mintly.app.data.repository.ProfileRepository
import com.mintly.app.data.repository.ShopRepository
import com.mintly.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val profile: Profile? = null,
    val todayIncome: Int = 0,
    val todayExpense: Int = 0,
    val monthlyStats: Map<Category, Int> = emptyMap(),
    val inventory: List<UserCostume> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val txRepo: TransactionRepository,
    private val shopRepo: ShopRepository,
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
            val now = LocalDate.now()
            val todayTx = getTodayTransactions(now.year, now.monthValue, now.dayOfMonth)
            val monthStats = txRepo.getMonthlyCategoryStats(now.year, now.monthValue)
            val inventory = shopRepo.getMyInventory()
            _uiState.value = HomeUiState(
                profile = profile,
                todayIncome  = todayTx.filter { it.kind == "income"  }.sumOf { it.amount },
                todayExpense = todayTx.filter { it.kind == "expense" }.sumOf { it.amount },
                monthlyStats = monthStats,
                inventory = inventory,
                isLoading = false,
            )
        }
    }

    private suspend fun getTodayTransactions(year: Int, month: Int, day: Int): List<Transaction> {
        val all = txRepo.getTransactionsForMonth(year, month)
        val todayStr = "%04d-%02d-%02d".format(year, month, day)
        return all.filter { it.occurredOn == todayStr }
    }

    fun equipHat(hatId: String?) {
        viewModelScope.launch {
            profileRepo.equipHat(hatId)
            load()
        }
    }

    fun equipOutfit(outfitId: String?) {
        viewModelScope.launch {
            profileRepo.equipOutfit(outfitId)
            load()
        }
    }

    fun updateAvatar(color: String, face: Map<String, String>) {
        viewModelScope.launch {
            profileRepo.updateAvatar(color, face)
            load()
        }
    }
}
