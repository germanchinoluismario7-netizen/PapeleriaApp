package com.example.papeleriaapp.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.model.Product
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.example.papeleriaapp.domain.repository.ProductRepository
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

data class ProductsUiState(
    val isLoading : Boolean = false,
    val storeName : String = "Mi papelería",
    val products : List<Product> = emptyList(),
    val showInactive : Boolean = false,
    val errorMessage : String? = null
) {
    /** Productos que se muestran en la lista segun el filtro activo. */
    val visibleProducts : List<Product>
        get() = if (showInactive) products else products.filter { it.isActive }
}

sealed interface ProductsEvent{
    data object OnRefresh : ProductsEvent
    data object OnToggleInactive : ProductsEvent
    data class OnDelete(val product : Product) : ProductsEvent
    data class OnRestore(val product : Product) : ProductsEvent
}

sealed interface ProductsEffect{
    data class ShowMessage(val message : String) : ProductsEffect
}

@HiltViewModel
class SellerProductsViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val storeRepository : StoreRepository,
    private val productRepository : ProductRepository
) : ViewModel() {

    private var storeId : String = ""

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState : StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProductsEffect>()
    val effect : SharedFlow<ProductsEffect> = _effect.asSharedFlow()

    init {
        loadProducts()
    }

    fun onEvent(event : ProductsEvent){
        when(event){
            ProductsEvent.OnRefresh -> loadProducts()
            ProductsEvent.OnToggleInactive -> _uiState.update { current ->
                current.copy(showInactive = !current.showInactive)
            }
            is ProductsEvent.OnDelete -> setActive(event.product, false)
            is ProductsEvent.OnRestore -> setActive(event.product, true)
        }
    }

    private fun loadProducts(){
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

            if (store == null) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "Primero debes registrar tu papelería"
                    )
                }
                return@launch
            }

            storeId = store.id
            _uiState.update { current ->
                current.copy(storeName = store.name)
            }

            productRepository.getProductsByStore(store.id)
                .onSuccess { products ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            products = products,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudieron cargar los productos"
                        )
                    }
                    _effect.emit(
                        ProductsEffect.ShowMessage(e.message ?: "No se pudieron cargar los productos")
                    )
                }
        }
    }

    private fun setActive(product : Product, isActive : Boolean){
        viewModelScope.launch {
            productRepository.setProductActive(product.id, isActive)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            products = current.products.map { item ->
                                if (item.id == product.id) item.copy(isActive = isActive) else item
                            }
                        )
                    }

                    _effect.emit(
                        ProductsEffect.ShowMessage(
                            if (isActive) "${product.name} restaurado"
                            else "${product.name} eliminado del catálogo"
                        )
                    )
                }
                .onFailure { e ->
                    _effect.emit(
                        ProductsEffect.ShowMessage(e.message ?: "No se pudo actualizar el producto")
                    )
                }
        }
    }
}
