package com.mintly.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.Costume
import com.mintly.app.data.model.DEFAULT_COSTUMES
import com.mintly.app.data.model.RouletteSegment
import com.mintly.app.data.model.UserCostume
import com.mintly.app.data.repository.ProfileRepository
import com.mintly.app.data.repository.ShopRepository
import com.mintly.app.data.repository.ShopRepository.Companion.TICKET_PRICE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val coins: Int = 0,
    val ticketCount: Int = 0,
    val shopCostumes: List<Costume> = emptyList(),
    val ownedCostumeIds: Set<String> = emptySet(),
    val isSpinning: Boolean = false,
    val spinResult: RouletteSegment? = null,
    val spinAngle: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMsg: String? = null,
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepo: ShopRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val profile   = profileRepo.getMyProfile()
            val costumes  = shopRepo.getShopCostumes().ifEmpty { DEFAULT_COSTUMES.filter { it.isShop } }
                .filterNot { it.id in setOf("hat_cap", "hat_bucket", "hat_side_ribbon") || it.name.contains("버킷") || it.name.contains("사이드 리본") }
            val inventory = shopRepo.getMyInventory()
            val tickets   = shopRepo.getMyTicketCount()
            _uiState.value = _uiState.value.copy(
                coins            = profile?.coins ?: 0,
                ticketCount      = tickets,
                shopCostumes     = costumes,
                ownedCostumeIds  = inventory.map { it.costumeId }.toSet(),
                isLoading        = false,
            )
        }
    }

    fun spinRoulette() {
        if (_uiState.value.ticketCount <= 0) {
            _uiState.value = _uiState.value.copy(error = "티켓이 없습니다")
            return
        }
        if (_uiState.value.isSpinning) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSpinning = true, spinResult = null)
            shopRepo.spinRoulette().fold(
                onSuccess = { segment ->
                    _uiState.value = _uiState.value.copy(
                        isSpinning = false,
                        spinResult = segment,
                        successMsg = "🎉 ${segment.label} 획득!",
                    )
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSpinning = false,
                        error = it.message ?: "룰렛 실패",
                    )
                }
            )
        }
    }

    fun purchaseCostume(costume: Costume) {
        if (_uiState.value.coins < costume.price) {
            _uiState.value = _uiState.value.copy(error = "코인이 부족합니다")
            return
        }
        viewModelScope.launch {
            shopRepo.purchaseCostume(costume.id, costume.price).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMsg = "${costume.name} 구매 완료!")
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = "구매 실패: ${it.message}")
                }
            )
        }
    }

    fun purchaseTicket() {
        if (_uiState.value.coins < TICKET_PRICE) {
            _uiState.value = _uiState.value.copy(error = "코인이 부족합니다 (필요: ${TICKET_PRICE}코인)")
            return
        }
        viewModelScope.launch {
            shopRepo.purchaseRouletteTicket().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMsg = "룰렛 티켓 구매 완료! 🎟️")
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "구매 실패")
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMsg = null, spinResult = null)
    }
}
