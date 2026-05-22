package com.mintly.app.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.FriendGroup
import com.mintly.app.data.model.Profile
import com.mintly.app.data.model.RankedMember
import com.mintly.app.data.repository.AuthRepository
import com.mintly.app.data.repository.GroupRepository
import com.mintly.app.data.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
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
    val myUserId: String? = null,
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val groupRepo: GroupRepository,
    private val rankingRepo: RankingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()
    private var realtimeJob: Job? = null

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val myUserId = authRepo.currentUserId()
            val groups = groupRepo.getMyGroups()
            var groupWithMostMembers: FriendGroup? = null
            var mostMembers = -1
            val memberCounts = mutableMapOf<String, Int>()
            for (group in groups) {
                val memberCount = rankingRepo.getTodayRankingMemberCount(group.id)
                memberCounts[group.id] = memberCount
                if (memberCount > mostMembers) {
                    groupWithMostMembers = group
                    mostMembers = memberCount
                }
            }
            val previousGroupId = _uiState.value.selectedGroupId
                ?.takeIf { selectedId -> groups.any { it.id == selectedId } }
            val previousMemberCount = previousGroupId?.let { memberCounts[it] } ?: 0
            val selectedGroupId = if (previousGroupId != null && previousMemberCount >= mostMembers) {
                previousGroupId
            } else {
                groupWithMostMembers?.id
            }
            _uiState.value = _uiState.value.copy(
                groups = groups,
                selectedGroupId = selectedGroupId,
                isLoading = false,
                myUserId = myUserId,
            )
            selectedGroupId?.let {
                loadRanking(it)
                startRealtime(it)
            }
        }
    }

    fun selectGroup(groupId: String) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
        loadRanking(groupId)
        startRealtime(groupId)
    }

    fun loadRanking(groupId: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
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
        val groupId = _uiState.value.selectedGroupId ?: return
        startRealtime(groupId)
    }

    private fun startRealtime(groupId: String) {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            rankingRepo.anyRankingChange(groupId).collect {
                delay(300)
                loadRanking(groupId)
            }
        }
    }

    override fun onCleared() {
        realtimeJob?.cancel()
        super.onCleared()
    }
}
