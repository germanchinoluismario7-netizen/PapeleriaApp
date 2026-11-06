package com.example.papeleriaapp.presentation.products

import androidx.lifecycle.SavedStateHandle
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

data class ProductFormUiState(
    val isLoading : Boolean = false,
    val isEdit : Boolean = false,
    val name : String = "",
    val description : String = "",
    val category : String = "",
    val price : String = "",
    val stock : String = "",
    val errorMessage : String? = null
)

sealed interface ProductFormEvent{
    data class OnNameChange(val value : String) : ProductFormEvent
    data class OnDescriptionChange(val value : String) : ProductFormEvent
    data class OnCategoryChange(val value : String) : ProductFormEvent
    data class OnPriceChange(val value : String) : ProductFormEvent
    data class OnStockChange(val value : String) : ProductFormEvent
    data object OnSaveClick : ProductFormEvent
}

sealed interface ProductFormEffect{
    data class ShowMessage(val message : String) : ProductFormEffect
    data object NavigateBack : ProductFormEffect
}

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val productRepository : ProductRepository,
    private val storeRepository : StoreRepository,
    private val authRepository : AuthRepository,
    savedStateHandle : SavedStateHandle
) : ViewModel() {

    /** id del producto a editar; vacio si es un producto nuevo. */
    private val productId : String =
        savedStateHandle.get<String>("productId")?.trim().orEmpty()

    private var existingProduct : Product? = null
    private var storeId : String = ""

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState : StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProductFormEffect>()
    val effect : SharedFlow<ProductFormEffect> = _effect.asSharedFlow()

    init {
        if (productId.isNotBlank()) {
            loadProduct()
        }
        loadStoreId()
    }

    fun onEvent(event : ProductFormEvent){
        when(event){
            is ProductFormEvent.OnNameChange -> update { it.copy(name = event.value) }
            is ProductFormEvent.OnDescriptionChange -> update { it.copy(description = event.value) }
            is ProductFormEvent.OnCategoryChange -> update { it.copy(category = event.value) }
            is ProductFormEvent.OnPriceChange -> update { it.copy(price = event.value) }
            is ProductFormEvent.OnStockChange -> update { it.copy(stock = event.value) }
            ProductFormEvent.OnSaveClick -> save()
        }
    }

    private fun update(transform : (ProductFormUiState) -> ProductFormUiState){
        _uiState.update { current -> transform(current).copy(errorMessage = null) }
    }

    private fun loadProduct(){
        _uiState.update { current -> current.copy(isEdit = true, isLoading = true) }

        viewModelScope.launch {
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    if (product == null) {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                isEdit = false
                            )
                        }
                        _effect.emit(ProductFormEffect.ShowMessage("No se encontró el producto"))
                        _effect.emit(ProductFormEffect.NavigateBack)
                    } else {
                        existingProduct = product
                        storeId = product.storeId
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                name = product.name,
                                description = product.description,
                                category = product.category,
                                price = formatPrice(product.price),
                                stock = product.stock.toString()
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isEdit = false
                        )
                    }
                    _effect.emit(
                        ProductFormEffect.ShowMessage(e.message ?: "No se pudo cargar el producto")
                    )
                }
        }
    }

    private fun loadStoreId(){
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            val store = storeRepository.getStoreBySeller(currentUser.uid).getOrNull()
            if (store != null && storeId.isBlank()) {
                storeId = store.id
            }
        }
    }

    private fun save(){
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val nameTrimmed = _uiState.value.name.trim()
            val descriptionTrimmed = _uiState.value.description.trim()
            val categoryTrimmed = _uiState.value.category.trim()
            val priceTrimmed = _uiState.value.price.trim().replace(',', '.')
            val stockTrimmed = _uiState.value.stock.trim()

            if (nameTrimmed.isEmpty()) {
                _effect.emit(ProductFormEffect.ShowMessage("Ingresa el nombre del producto"))
                return@launch
            }

            if (categoryTrimmed.isEmpty()) {
                _effect.emit(ProductFormEffect.ShowMessage("Selecciona una categoría"))
                return@launch
            }

            val price : Double? = priceTrimmed.toDoubleOrNull()
            if (price == null || price < 0.0) {
                _effect.emit(ProductFormEffect.ShowMessage("Ingresa un precio válido"))
                return@launch
            }

            val stock : Int? = stockTrimmed.toIntOrNull()
            if (stock == null || stock < 0) {
                _effect.emit(ProductFormEffect.ShowMessage("Ingresa un stock válido"))
                return@launch
            }

            if (existingProduct == null && storeId.isBlank()) {
                _effect.emit(ProductFormEffect.ShowMessage("Primero debes registrar tu papelería"))
                return@launch
            }

            _uiState.update { current ->
                current.copy(isLoading = true, errorMessage = null)
            }

            val result : Result<Unit> = if (existingProduct != null) {
                productRepository.updateProduct(
                    existingProduct!!.copy(
                        name = nameTrimmed,
                        description = descriptionTrimmed,
                        category = categoryTrimmed,
                        price = price,
                        stock = stock
                    )
                )
            } else {
                productRepository.createProduct(
                    Product(
                        storeId = storeId,
                        name = nameTrimmed,
                        description = descriptionTrimmed,
                        category = categoryTrimmed,
                        price = price,
                        stock = stock,
                        isActive = true
                    )
                ).map { }
            }

            result
                .onSuccess {
                    _uiState.update { current -> current.copy(isLoading = false) }
                    _effect.emit(
                        ProductFormEffect.ShowMessage(
                            if (existingProduct != null) "Producto actualizado con éxito"
                            else "Producto agregado con éxito"
                        )
                    )
                    _effect.emit(ProductFormEffect.NavigateBack)
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo guardar el producto"
                        )
                    }
                    _effect.emit(
                        ProductFormEffect.ShowMessage(e.message ?: "No se pudo guardar el producto")
                    )
                }
        }
    }

    private fun formatPrice(price : Double) : String =
        if (price == price.toLong().toDouble()) {
            price.toLong().toString()
        } else {
            price.toString()
        }
}
