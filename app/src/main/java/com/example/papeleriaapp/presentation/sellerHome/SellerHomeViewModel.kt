package com.example.papeleriaapp.presentation.sellerHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.model.Order
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.example.papeleriaapp.domain.repository.OrderRepository
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
    val isLoading : Boolean = false,
    val storeName : String = "Mi papelería",
    val orders : List<Order> = emptyList(),
    val errorMessage : String? = null
)

sealed interface SellerEvent{
    data object OnRefresh : SellerEvent
    data class OnStatusChange(
        val orderId : String,
        val status : String
    ) : SellerEvent
}

sealed interface SellerEffect{
    data class ShowMessage(val message : String) : SellerEffect
}

@HiltViewModel
class SellerHomeViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val storeRepository : StoreRepository,
    private val orderRepository : OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerEffect>()
    val effect : SharedFlow<SellerEffect> = _effect.asSharedFlow()

    init {
        loadOrders()
    }

    fun onEvent(event : SellerEvent){
        when(event){
            SellerEvent.OnRefresh -> loadOrders()
            is SellerEvent.OnStatusChange -> changeStatus(event.orderId, event.status)
        }
    }

    private fun loadOrders(){
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

            val store = storeRepository.getStoreBySeller(currentUser.uid).getOrNull()

            if (store != null) {
                _uiState.update { current ->
                    current.copy(storeName = store.name)
                }
            }

            orderRepository.getAllOrders()
                .onSuccess { orders ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            orders = orders,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudieron cargar los pedidos"
                        )
                    }
                    _effect.emit(
                        SellerEffect.ShowMessage(e.message ?: "No se pudieron cargar los pedidos")
                    )
                }
        }
    }

    private fun changeStatus(orderId : String, status : String){
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            orders = current.orders.map { order ->
                                if (order.id == orderId) order.copy(status = status) else order
                            }
                        )
                    }
                    _effect.emit(SellerEffect.ShowMessage("Estado actualizado"))
                }
                .onFailure { e ->
                    _effect.emit(
                        SellerEffect.ShowMessage(e.message ?: "No se pudo actualizar el estado")
                    )
                }
        }
    }
}
