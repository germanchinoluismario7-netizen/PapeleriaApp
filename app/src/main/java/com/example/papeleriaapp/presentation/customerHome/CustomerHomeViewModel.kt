package com.example.papeleriaapp.presentation.customerHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.cart.CartManager
import com.example.papeleriaapp.core.model.Product
import com.example.papeleriaapp.core.model.StationeryCategories
import com.example.papeleriaapp.domain.repository.ProductRepository
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
    val products : List<Product> = emptyList(),
    val visibleProducts : List<Product> = emptyList(),
    val searchText : String = "",
    val selectedCategory : String = "Todos",
    val cartCount : Int = 0,
    val errorMessage : String? = null
)

sealed interface CustomerEvent{
    data class OnSearchChange(val value : String) : CustomerEvent
    data class OnCategoryChange(val value : String) : CustomerEvent
    data class OnAddToCart(val product : Product) : CustomerEvent
    data object OnRefresh : CustomerEvent
}

sealed interface CustomerEffect{
    data class ShowMessage(val message : String) : CustomerEffect
}

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val productRepository : ProductRepository,
    private val cartManager : CartManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState : StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomerEffect>()
    val effect : SharedFlow<CustomerEffect> = _effect.asSharedFlow()

    init {
        loadProducts()

        viewModelScope.launch {
            cartManager.items.collect { items ->
                _uiState.update { current ->
                    current.copy(cartCount = items.sumOf { it.quantity })
                }
            }
        }
    }

    fun onEvent(event : CustomerEvent){
        when(event){
            is CustomerEvent.OnSearchChange -> updateSearch(event.value)
            is CustomerEvent.OnCategoryChange -> updateCategory(event.value)
            is CustomerEvent.OnAddToCart -> addToCart(event.product)
            CustomerEvent.OnRefresh -> loadProducts()
        }
    }

    private fun updateSearch(value : String){
        _uiState.update { current ->
            current.copy(searchText = value)
        }
        applyFilters()
    }

    private fun updateCategory(value : String){
        _uiState.update { current ->
            current.copy(selectedCategory = value)
        }
        applyFilters()
    }

    private fun addToCart(product : Product){
        if (product.stock <= 0) {
            viewModelScope.launch {
                _effect.emit(CustomerEffect.ShowMessage("Producto agotado"))
            }
            return
        }

        cartManager.add(product)

        viewModelScope.launch {
            _effect.emit(CustomerEffect.ShowMessage("${product.name} agregado al carrito"))
        }
    }

    private fun loadProducts(){
        _uiState.update { current ->
            current.copy(isLoading = true, errorMessage = null)
        }

        viewModelScope.launch {
            productRepository.getActiveProducts()
                .onSuccess { products ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            products = products
                        )
                    }
                    applyFilters()
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo cargar el catálogo"
                        )
                    }
                }
        }
    }

    private fun applyFilters(){
        _uiState.update { current ->
            val search = current.searchText.trim().lowercase()

            val filtered = current.products.filter { product ->
                val matchesSearch = search.isEmpty() ||
                        product.name.lowercase().contains(search) ||
                        product.description.lowercase().contains(search)

                val matchesCategory =
                    current.selectedCategory == StationeryCategories.productCategories.first() ||
                            product.category == current.selectedCategory

                matchesSearch && matchesCategory
            }

            current.copy(visibleProducts = filtered)
        }
    }
}
