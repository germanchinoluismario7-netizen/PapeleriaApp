package com.example.papeleriaapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.cart.CartManager
import com.example.papeleriaapp.core.model.AppUser
import com.example.papeleriaapp.core.model.UserRole
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.example.papeleriaapp.domain.repository.StoreRepository
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

data class UiState(
    val email : String = "",
    val password : String = "",
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)

sealed interface LoginEvent{
    data class OnEmailChange(
        val value : String
    ) : LoginEvent

    data class OnPasswordChange(
        val value : String
    ) : LoginEvent

    data object OnLoginClick : LoginEvent

    /** Revisa si ya hay una sesion iniciada en Firebase Auth. */
    data object RestoreSession : LoginEvent
}

sealed interface LoginEffect{
    data class ShowMessage(
        val message : String
    ) : LoginEffect

    data object NavigateToCustomerHome : LoginEffect

    data object NavigateToSellerHome : LoginEffect

    /** El vendedor inicio sesion pero aun no ha registrado su papeleria. */
    data class NavigateToRegisterStore(
        val sellerUid : String
    ) : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val storeRepository : StoreRepository,
    private val cartManager : CartManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()

    val effect : SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun onEvent(event : LoginEvent){
        when(event){
            is LoginEvent.OnEmailChange -> updateEmail(event.value)
            is LoginEvent.OnPasswordChange -> updatePassword(event.value)
            LoginEvent.OnLoginClick -> loginUser()
            LoginEvent.RestoreSession -> restoreSession()
        }
    }

    private fun updateEmail(value : String){
        _uiState.update { current ->
            current.copy(
                email = value,
                errorMessage = null
            )
        }
    }

    private fun updatePassword(value : String){
        _uiState.update { current ->
            current.copy(
                password = value,
                errorMessage = null
            )
        }
    }

    private fun restoreSession(){
        viewModelScope.launch {
            // Al entrar al login (inicio de app o cierre de sesion) el carrito arranca vacio
            cartManager.clear()

            val currentUser : AppUser? = authRepository.getCurrentUser()
            if (currentUser != null) {
                routeByUser(currentUser)
            }
        }
    }

    private fun loginUser(){
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val emailTrimmed : String = _uiState.value.email.trim()
            val passwordTrimmed : String = _uiState.value.password.trim()

            if (emailTrimmed.isEmpty()){
                _effect.emit(LoginEffect.ShowMessage("Ingrese su correo!!!"))
                return@launch
            }

            if (passwordTrimmed.isEmpty()){
                _effect.emit(LoginEffect.ShowMessage("Ingrese su contraseña!!!"))
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result : Result<AppUser> =
                authRepository.login(
                    email = emailTrimmed,
                    password = passwordTrimmed
                )

            result.onSuccess { user ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }

                _effect.emit(LoginEffect.ShowMessage("¡Inicio de sesión exitoso!"))
                routeByUser(user)
            }.onFailure { e ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "No se pudo iniciar sesión!!!"
                    )
                }
                _effect.emit(LoginEffect.ShowMessage(e.message ?: "No se pudo iniciar sesión!!!"))
            }
        }
    }

    private suspend fun routeByUser(user : AppUser){
        if (user.role == UserRole.SELLER.name) {
            val store = storeRepository.getStoreBySeller(user.uid).getOrNull()

            if (store == null) {
                _effect.emit(LoginEffect.NavigateToRegisterStore(user.uid))
            } else {
                _effect.emit(LoginEffect.NavigateToSellerHome)
            }
        } else {
            _effect.emit(LoginEffect.NavigateToCustomerHome)
        }
    }
}
