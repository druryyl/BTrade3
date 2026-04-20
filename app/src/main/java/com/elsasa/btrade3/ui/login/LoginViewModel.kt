package com.elsasa.btrade3.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.data.repository.AuthRepositoryImpl
import com.elsasa.btrade3.domain.model.ValidationResult
import com.elsasa.btrade3.domain.usecase.ValidateSalesPersonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object SigningIn : LoginUiState()
    data class Validating(val email: String) : LoginUiState()
    data class Authorized(val email: String) : LoginUiState()
    data class Unauthorized(val email: String, val reason: String?) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val validateSalesPersonUseCase: ValidateSalesPersonUseCase =
        ValidateSalesPersonUseCase(AuthRepositoryImpl())
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onStartSigningIn() {
        _uiState.value = LoginUiState.SigningIn
    }

    fun validateEmail(email: String) {
        _uiState.value = LoginUiState.Validating(email)
        viewModelScope.launch {
            val result = try {
                validateSalesPersonUseCase(email)
            } catch (e: Exception) {
                ValidationResult.Failure("Unexpected error: ${e.message ?: ""}")
            }

            when (result) {
                is ValidationResult.Success -> {
                    if (result.isValid) _uiState.value = LoginUiState.Authorized(email)
                    else _uiState.value = LoginUiState.Unauthorized(email, "Email not registered")
                }
                is ValidationResult.Failure -> {
                    _uiState.value = LoginUiState.Error(result.reason)
                }
            }
        }
    }

    fun reset() {
        _uiState.value = LoginUiState.Idle
    }
}

