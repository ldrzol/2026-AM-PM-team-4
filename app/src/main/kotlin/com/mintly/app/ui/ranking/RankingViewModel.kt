package com.mintly.app.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.FriendGroup
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
import com.mintly.app.data.repository.GroupRepository
import com.mintly.app.data.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RankingUiState(
    val groups: List<FriendGroup> = emptyList(),
    val selectedGroupId: String? = null,
    val rankings: List<RankedMember> = emptyList(),
    val selectedFriend: Profile? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val groupRepo: GroupRepository,
    private val rankingRepo: RankingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val groups = groupRepo.getMyGroups()
            val firstGroupId = _uiState.value.selectedGroupId ?: groups.firstOrNull()?.id
            _uiState.value = _uiState.value.copy(groups = groups, selectedGroupId = firstGroupId, isLoading = false)
            firstGroupId?.let { loadRanking(it) }
        }
    }

    fun selectGroup(groupId: String) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
        loadRanking(groupId)
    }

    fun loadRanking(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val rankings = rankingRepo.getTodayRanking(groupId)
            _uiState.value = _uiState.value.copy(rankings = rankings, isLoading = false)
        }
    }

    fun selectFriend(profile: Profile) {
        _uiState.value = _uiState.value.copy(selectedFriend = profile)
    }

    fun clearSelectedFriend() {
        _uiState.value = _uiState.value.copy(selectedFriend = null)
    }

    fun startRealtime() {
        viewModelScope.launch {
            val groupId = _uiState.value.selectedGroupId ?: return@launch
            rankingRepo.rankingChanges(groupId).collect {
                delay(500)
                loadRanking(groupId)
            }
        }
    }
}
