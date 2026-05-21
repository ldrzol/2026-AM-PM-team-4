package com.mintly.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Favorite
import com.mintly.app.data.model.Transaction
import com.mintly.app.data.repository.GroupRepository
import com.mintly.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

data class BudgetUiState(
    val year: Int  = LocalDate.now(KOREA_ZONE).year,
    val month: Int = LocalDate.now(KOREA_ZONE).monthValue,
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val selectedGroupId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMsg: String? = null,
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val txRepo: TransactionRepository,
    private val groupRepo: GroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = _uiState.value
            val txList = txRepo.getTransactionsForMonth(state.year, state.month)
            val cats   = txRepo.getCategories()
            val favs   = txRepo.getFavorites()
            val groups = groupRepo.getMyGroups()
            _uiState.value = _uiState.value.copy(
                transactions = txList,
                categories   = cats,
                favorites    = favs,
                selectedGroupId = _uiState.value.selectedGroupId ?: groups.firstOrNull()?.id,
                isLoading    = false,
            )
        }
    }

    fun changeMonth(year: Int, month: Int) {
        _uiState.value = _uiState.value.copy(year = year, month = month)
        viewModelScope.launch {
            val txList = txRepo.getTransactionsForMonth(year, month)
            _uiState.value = _uiState.value.copy(transactions = txList)
        }
    }

    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            txRepo.addTransaction(tx.copy(groupId = _uiState.value.selectedGroupId)).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMsg = "저장되었습니다")
                    loadAll()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = "저장 실패: ${it.message}")
                }
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            txRepo.deleteTransaction(id).fold(
                onSuccess = { loadAll() },
                onFailure = { _uiState.value = _uiState.value.copy(error = "삭제 실패") }
            )
        }
    }

    fun addCategory(name: String, icon: String, color: String, kind: String) {
        viewModelScope.launch {
            val cat = Category(name = name, icon = icon, color = color, kind = kind)
            txRepo.addCategory(cat).fold(
                onSuccess = {
                    val cats = txRepo.getCategories()
                    _uiState.value = _uiState.value.copy(categories = cats)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = "카테고리 추가 실패") }
            )
        }
    }

    fun addFavorite(fav: Favorite) {
        viewModelScope.launch {
            txRepo.addFavorite(fav).fold(
                onSuccess = {
                    val favs = txRepo.getFavorites()
                    _uiState.value = _uiState.value.copy(favorites = favs)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = "즐겨찾기 추가 실패") }
            )
        }
    }

    fun deleteFavorite(id: String) {
        viewModelScope.launch {
            txRepo.deleteFavorite(id)
            val favs = txRepo.getFavorites()
            _uiState.value = _uiState.value.copy(favorites = favs)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMsg = null)
    }

    // 날짜별 거래 그룹핑
    fun transactionsByDate(): Map<String, List<Transaction>> =
        _uiState.value.transactions.groupBy { it.occurredOn }
            .toSortedMap(compareByDescending { it })
}
