package com.example.papeleriaapp.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.cart.CartItem
import com.example.papeleriaapp.core.cart.CartManager
import com.example.papeleriaapp.core.model.Order
import com.example.papeleriaapp.core.model.OrderItem
import com.example.papeleriaapp.core.model.OrderStatus
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
    val items : List<CartItem> = emptyList(),
    val total : Double = 0.0,
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)

sealed interface CartEvent{
    data class OnIncrease(val productId : String) : CartEvent
    data class OnDecrease(val productId : String) : CartEvent
    data class OnRemove(val productId : String) : CartEvent
    data object OnCheckout : CartEvent
}

sealed interface CartEffect{
    data class ShowMessage(val message : String) : CartEffect
    data object NavigateToOrders : CartEffect
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartManager : CartManager,
    private val authRepository : AuthRepository,
    private val orderRepository : OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CartEffect>()
    val effect : SharedFlow<CartEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            cartManager.items.collect { items ->
                _uiState.update { current ->
                    current.copy(
                        items = items,
                        total = items.sumOf { it.price * it.quantity }
                    )
                }
            }
        }
    }

    fun onEvent(event : CartEvent){
        when(event){
            is CartEvent.OnIncrease -> cartManager.increase(event.productId)
            is CartEvent.OnDecrease -> cartManager.decrease(event.productId)
            is CartEvent.OnRemove -> cartManager.remove(event.productId)
            CartEvent.OnCheckout -> checkout()
        }
    }

    private fun checkout(){
        if (_uiState.value.isLoading) return
        if (_uiState.value.items.isEmpty()) {
            viewModelScope.launch {
                _effect.emit(CartEffect.ShowMessage("El carrito está vacío"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isLoading = true, errorMessage = null)
            }

            val currentUser = authRepository.getCurrentUser()

            if (currentUser == null) {
                _uiState.update { current ->
                    current.copy(isLoading = false)
                }
                _effect.emit(CartEffect.ShowMessage("Tu sesión expiró, ingresa de nuevo"))
                return@launch
            }

            val order = Order(
                customerId = currentUser.uid,
                customerName = "${currentUser.firstName} ${currentUser.lastName}".trim(),
                customerEmail = currentUser.email,
                items = _uiState.value.items.map { item ->
                    OrderItem(
                        productId = item.productId,
                        name = item.name,
                        price = item.price,
                        quantity = item.quantity
                    )
                },
                status = OrderStatus.PENDIENTE.name
            )

            orderRepository.createOrder(order)
                .onSuccess {
                    cartManager.clear()

                    _uiState.update { current ->
                        current.copy(isLoading = false)
                    }

                    _effect.emit(CartEffect.ShowMessage("¡Pedido realizado con éxito!"))
                    _effect.emit(CartEffect.NavigateToOrders)
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo realizar el pedido"
                        )
                    }
                    _effect.emit(
                        CartEffect.ShowMessage(e.message ?: "No se pudo realizar el pedido")
                    )
                }
        }
    }
}
