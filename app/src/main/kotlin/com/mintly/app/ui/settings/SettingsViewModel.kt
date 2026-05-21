package com.mintly.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.model.Category
import com.mintly.app.data.model.Favorite
import com.mintly.app.data.model.FriendGroup
import com.mintly.app.data.model.Profile
import com.mintly.app.data.repository.AuthRepository
import com.mintly.app.data.repository.GroupRepository
import com.mintly.app.data.repository.ProfileRepository
import com.mintly.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val profile: Profile? = null,
    val groups: List<FriendGroup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMsg: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val groupRepo: GroupRepository,
    private val authRepo: AuthRepository,
    private val txRepo: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val profile = profileRepo.getMyProfile()
            val groups  = groupRepo.getMyGroups()
            val cats    = txRepo.getCategories()
            val favs    = txRepo.getFavorites()
            _uiState.value = _uiState.value.copy(
                profile    = profile,
                groups     = groups,
                categories = cats,
                favorites  = favs,
                isLoading  = false,
            )
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            profileRepo.updateDisplayName(name).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(successMsg = "닉네임 변경 완료"); load() },
                onFailure = { _uiState.value = _uiState.value.copy(error = "변경 실패: ${it.message}") }
            )
        }
    }

    fun createRoom(name: String, emoji: String, onSuccess: (FriendGroup) -> Unit) {
        viewModelScope.launch {
            groupRepo.createGroup(name, emoji).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMsg = "방 생성 완료!")
                    load()
                    onSuccess(it)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = "방 생성 실패: ${it.message}") }
            )
        }
    }

    fun joinRoom(code: String, onSuccess: (FriendGroup) -> Unit) {
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "코드를 입력해주세요")
            return
        }
        viewModelScope.launch {
            groupRepo.joinGroup(code.trim()).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMsg = "'${it.name}' 방 참여 완료!")
                    load()
                    onSuccess(it)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message ?: "참여 실패") }
            )
        }
    }

    fun updateShareMode(mode: String) {
        viewModelScope.launch {
            profileRepo.updateShareMode(mode).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(successMsg = "공유 설정 변경"); load() },
                onFailure = { _uiState.value = _uiState.value.copy(error = "변경 실패") }
            )
        }
    }

    // ── 카테고리 ──────────────────────────────────────────

    fun addCategory(name: String, icon: String, color: String, kind: String) {
        viewModelScope.launch {
            val cat = Category(name = name, icon = icon, color = color, kind = kind)
            txRepo.addCategory(cat).fold(
                onSuccess = {
                    val cats = txRepo.getCategories()
                    _uiState.value = _uiState.value.copy(categories = cats, successMsg = "카테고리가 추가되었습니다")
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = "카테고리 추가 실패") }
            )
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            txRepo.deleteCategory(id).fold(
                onSuccess = {
                    val cats = txRepo.getCategories()
                    _uiState.value = _uiState.value.copy(categories = cats)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = "카테고리 삭제 실패") }
            )
        }
    }

    // ── 즐겨찾기 ──────────────────────────────────────────

    fun addFavorite(fav: Favorite) {
        viewModelScope.launch {
            txRepo.addFavorite(fav).fold(
                onSuccess = {
                    val favs = txRepo.getFavorites()
                    _uiState.value = _uiState.value.copy(favorites = favs, successMsg = "즐겨찾기에 추가되었습니다")
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

    // ── 개인정보 ──────────────────────────────────────────

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            authRepo.changePassword(newPassword).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(successMsg = "비밀번호가 변경되었습니다") },
                onFailure = { _uiState.value = _uiState.value.copy(error = "변경 실패: ${it.message}") }
            )
        }
    }

    // ── 기타 ──────────────────────────────────────────────

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepo.signOut()
            onDone()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMsg = null)
    }
}
