package com.example.papeleriaapp.presentation.registerCustomer

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
import kotlin.String

data class UiState(
    val firstName : String = "",
    val lastName : String = "",
    val email : String = "",
    val password : String = "",
    val confirmPassword : String = "",
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)

sealed interface CustomerEvent{
    data class OnFirstNameChange(
        val value : String
    ) : CustomerEvent

    data class OnLastNameChange(
        val value : String
    ) : CustomerEvent

    data class OnEmailChange(
        val value : String
    ) : CustomerEvent

    data class OnPasswordChange(
        val value : String
    ) : CustomerEvent

    data class OnConfirmPasswordChange(
        val value : String
    ) : CustomerEvent

    data object OnRegisterClick : CustomerEvent

}

sealed interface CustomerEffect{
    data class ShowMessage(
        val message : String
    ) : CustomerEffect

    data object NavigateToHome : CustomerEffect
}

@HiltViewModel
class RegisterCustomerViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomerEffect>()

    val effect : SharedFlow<CustomerEffect> = _effect.asSharedFlow()

    fun onEvent(event : CustomerEvent){
        when(event){
            is CustomerEvent.OnFirstNameChange -> updateFirstName(event.value)
            is CustomerEvent.OnLastNameChange -> updateLastName(event.value)
            is CustomerEvent.OnEmailChange -> updateEmail(event.value)
            is CustomerEvent.OnPasswordChange -> updatePassword(event.value)
            is CustomerEvent.OnConfirmPasswordChange -> updateConfirmPassword(event.value)

            CustomerEvent.OnRegisterClick -> registerCustomer()
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

    private fun registerCustomer(){
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val firstNameTrimmed : String = _uiState.value.firstName.trim()
            val lastNameTrimmed : String = _uiState.value.lastName.trim()
            val emailTrimmed : String = _uiState.value.email.trim()
            val passwordTrimmed : String = _uiState.value.password.trim()
            val confirmPasswordTrimmed : String = _uiState.value.confirmPassword.trim()

            if (firstNameTrimmed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("Ingresa tus Nombres"))
                return@launch
            }

            if (lastNameTrimmed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("Ingresa tus Apellidos"))
                return@launch
            }

            if (emailTrimmed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("Ingresa tu correo"))
                return@launch
            }

            if (passwordTrimmed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("Ingresa tu contraseña"))
                return@launch
            }

            if (confirmPasswordTrimmed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("Confirma tu contraseña"))
                return@launch
            }

            if (passwordTrimmed != confirmPasswordTrimmed) {
                _effect.emit(CustomerEffect.ShowMessage("Las contraseñas no coinciden"))
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result : Result<AppUser> =
                authRepository.registerUser(
                    firstName = firstNameTrimmed,
                    lastName = lastNameTrimmed,
                    email = emailTrimmed,
                    password = passwordTrimmed,
                    role = UserRole.CUSTOMER,
                    phone = ""
            )
            result
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            firstName = "",
                            lastName = "",
                            email = "",
                            password = "",
                            confirmPassword = "",
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    _effect.emit(CustomerEffect.ShowMessage("Cliente registrado con Exito"))
                    _effect.emit(CustomerEffect.NavigateToHome)
                }
                .onFailure{ e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo registrar el cliente"
                        )
                    }
                    _effect.emit(
                        CustomerEffect.ShowMessage(
                            e.message ?: "No se pudo registrar el cliente"
                        )
                    )
                }
        }
    }
}
