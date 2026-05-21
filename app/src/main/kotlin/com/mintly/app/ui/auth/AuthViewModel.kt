package com.mintly.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mintly.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "이메일과 비밀번호를 입력해주세요")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepo.signIn(email.trim(), password).fold(
                onSuccess = {
                    _uiState.value = AuthUiState(isSuccess = true)
                    onSuccess()
                },
                onFailure = {
                    _uiState.value = AuthUiState(error = "로그인 실패: ${it.message}")
                }
            )
        }
    }

    fun signUp(
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        onSuccess: () -> Unit,
    ) {
        when {
            email.isBlank() || password.isBlank() || nickname.isBlank() ->
                _uiState.value = AuthUiState(error = "모든 항목을 입력해주세요")
            password != confirmPassword ->
                _uiState.value = AuthUiState(error = "비밀번호가 일치하지 않습니다")
            password.length < 6 ->
                _uiState.value = AuthUiState(error = "비밀번호는 6자 이상이어야 합니다")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState(isLoading = true)
                authRepo.signUp(email.trim(), password, nickname.trim()).fold(
                    onSuccess = {
                        _uiState.value = AuthUiState(isSuccess = true)
                        onSuccess()
                    },
                    onFailure = {
                        _uiState.value = AuthUiState(error = "회원가입 실패: ${it.message}")
                    }
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
