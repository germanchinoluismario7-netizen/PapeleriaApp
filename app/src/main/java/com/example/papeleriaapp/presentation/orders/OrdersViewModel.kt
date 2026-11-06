package com.example.papeleriaapp.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.model.Order
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.example.papeleriaapp.domain.repository.OrderRepository
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
    val isLoading : Boolean = false,
    val orders : List<Order> = emptyList(),
    val errorMessage : String? = null
)

sealed interface OrdersEvent{
    data object OnRefresh : OrdersEvent
}

sealed interface OrdersEffect{
    data class ShowMessage(val message : String) : OrdersEffect
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val orderRepository : OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<OrdersEffect>()
    val effect : SharedFlow<OrdersEffect> = _effect.asSharedFlow()

    init {
        loadMyOrders()
    }

    fun onEvent(event : OrdersEvent){
        when(event){
            OrdersEvent.OnRefresh -> loadMyOrders()
        }
    }

    private fun loadMyOrders(){
        _uiState.update { current ->
            current.copy(isLoading = true, errorMessage = null)
        }

        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()

            if (currentUser == null) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "Tu sesión expiró, ingresa de nuevo"
                    )
                }
                return@launch
            }

            orderRepository.getMyOrders(currentUser.uid)
                .onSuccess { orders ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            orders = orders
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudieron cargar tus pedidos"
                        )
                    }
                    _effect.emit(
                        OrdersEffect.ShowMessage(e.message ?: "No se pudieron cargar tus pedidos")
                    )
                }
        }
    }
}
