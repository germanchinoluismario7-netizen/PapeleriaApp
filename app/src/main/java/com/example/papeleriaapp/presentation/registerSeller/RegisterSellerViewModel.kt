package com.example.papeleriaapp.presentation.registerSeller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.model.AppUser
import com.example.papeleriaapp.core.model.UserRole
import com.example.papeleriaapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.onFailure

data class UiState(
    val firstName : String = "",
    val lastName : String = "",
    val email : String = "",
    val password : String = "",
    val confirmPassword : String = "",
    val phone: String = "",
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)

sealed interface SellerEvent{
    data class OnFirstNameChange(
        val value : String
    ) : SellerEvent

    data class OnLastNameChange(
        val value : String
    ) : SellerEvent

    data class OnEmailChange(
        val value : String
    ) : SellerEvent

    data class OnPasswordChange(
        val value : String
    ) : SellerEvent

    data class OnConfirmPasswordChange(
        val value : String
    ) : SellerEvent

    data class OnPhoneChange(
        val value : String
    ) : SellerEvent

    data object OnNextClick : SellerEvent
}

sealed interface SellerEffect{
    data class ShowMessage(
        val value : String
    ) : SellerEffect

    data class NavigateToRegisterScreen(
        val sellerUid : String
    ) : SellerEffect
}

@HiltViewModel
class RegisterSellerViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerEffect>()

    val effect : SharedFlow<SellerEffect> = _effect.asSharedFlow()

    fun onEvent(event : SellerEvent){
        when(event){
            is SellerEvent.OnFirstNameChange -> updateFirstName(event.value)
            is SellerEvent.OnLastNameChange -> updateLastName(event.value)
            is SellerEvent.OnEmailChange -> updateEmail(event.value)
            is SellerEvent.OnPasswordChange -> updatePassword(event.value)
            is SellerEvent.OnConfirmPasswordChange -> updateConfirmPassword(event.value)
            is SellerEvent.OnPhoneChange -> updatePhone(event.value)

            SellerEvent.OnNextClick -> registerSeller()
        }
    }

    private fun updateFirstName(value : String){
        _uiState.update { current ->
            current.copy(
                firstName =  value,
                errorMessage = null
            )
        }
    }

    private fun updateLastName(value : String){
        _uiState.update { current ->
            current.copy(
                lastName =  value,
                errorMessage = null
            )
        }
    }

    private fun updateEmail(value : String){
        _uiState.update { current ->
            current.copy(
                email =  value,
                errorMessage = null
            )
        }
    }

    private fun updatePassword(value : String){
        _uiState.update { current ->
            current.copy(
                password =  value,
                errorMessage = null
            )
        }
    }

    private fun updateConfirmPassword(value : String){
        _uiState.update { current ->
            current.copy(
                confirmPassword =  value,
                errorMessage = null
            )
        }
    }

    private fun updatePhone(value : String){
        _uiState.update { current ->
            current.copy(
                phone = value,
                errorMessage = null
            )
        }
    }

    private fun registerSeller() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val firstNameTrimmed: String = _uiState.value.firstName.trim()
            val lastNameTrimmed: String = _uiState.value.lastName.trim()
            val emailTrimmed: String = _uiState.value.email.trim()
            val passwordTrimmed: String = _uiState.value.password.trim()
            val confirmPasswordTrimmed: String = _uiState.value.confirmPassword.trim()
            val phoneTrimmed: String = _uiState.value.phone.trim()

            if (firstNameTrimmed.isEmpty()) {
                _effect.emit(SellerEffect.ShowMessage("Ingresa tus Nombres"))
                return@launch
            }

            if (lastNameTrimmed.isEmpty()) {
                _effect.emit(SellerEffect.ShowMessage("Ingresa tus Apellidos"))
                return@launch
            }

            if (emailTrimmed.isEmpty()) {
                _effect.emit(SellerEffect.ShowMessage("Ingresa tu correo"))
                return@launch
            }

            if (passwordTrimmed.isEmpty()) {
                _effect.emit(SellerEffect.ShowMessage("Ingresa tu contraseña"))
                return@launch
            }

            if (confirmPasswordTrimmed.isEmpty()) {
                _effect.emit(SellerEffect.ShowMessage("Confirma tu contraseña"))
                return@launch
            }

            if (passwordTrimmed != confirmPasswordTrimmed) {
                _effect.emit(SellerEffect.ShowMessage("Las contraseñas no coinciden"))
                return@launch
            }

            if (phoneTrimmed.isEmpty()) {
                _effect.emit(SellerEffect.ShowMessage("Ingresa tu telefono"))
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result: Result<AppUser> =
                authRepository.registerUser(
                    firstName = firstNameTrimmed,
                    lastName = lastNameTrimmed,
                    email = emailTrimmed,
                    password = passwordTrimmed,
                    phone = phoneTrimmed,
                    role = UserRole.SELLER
                )
            result
                .onSuccess { sellerUser ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    _effect.emit(SellerEffect.ShowMessage("La cuenta del vendedor se ha creado con exito"))
                    _effect.emit(SellerEffect.NavigateToRegisterScreen(sellerUser.uid))
                }.onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo registrar al vendedor"
                        )
                    }
                    _effect.emit(
                        SellerEffect.ShowMessage(
                            e.message ?: "No se pudo registrar al vendedor"
                        )
                    )
                }
        }
    }
}