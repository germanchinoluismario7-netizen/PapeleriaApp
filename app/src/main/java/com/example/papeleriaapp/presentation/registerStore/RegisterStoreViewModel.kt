package com.example.papeleriaapp.presentation.registerStore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papeleriaapp.core.model.Store
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
    val storeName : String = "",
    val storeDescription : String = "",
    val selectedCategory : String = "",
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)

sealed interface StoreEvent{
    data class OnStoreNameChange(
        val value : String
    ) : StoreEvent

    data class OnStoreDescriptionChange(
        val value : String
    ) : StoreEvent

    data class OnCategoryChange(
        val categoryId : String
    ) : StoreEvent

    data object OnNextClick : StoreEvent
}

sealed interface StoreEffect{
    data class ShowMessage(
        val message : String
    ) : StoreEffect

    data object NavigateToSellerHome : StoreEffect
}

@HiltViewModel
class RegisterStoreViewModel @Inject constructor(
    private val storeRepository : StoreRepository,
    savedStateHandle : SavedStateHandle
) : ViewModel() {

    /** uid del vendedor que llega desde la ruta register_store/{sellerUid} */
    private val sellerUid : String =
        savedStateHandle.get<String>("sellerUid")?.trim().orEmpty()

    private val _uiState = MutableStateFlow(UiState())
    val uiState : StateFlow<UiState> = _uiState.asStateFlow()
    private val _effect = MutableSharedFlow<StoreEffect>()
    val effect : SharedFlow<StoreEffect> = _effect.asSharedFlow()

    fun onEvent(event : StoreEvent){
        when(event) {
            is StoreEvent.OnStoreNameChange -> updateStoreName(event.value)
            is StoreEvent.OnStoreDescriptionChange -> updateStoreDescription(event.value)
            is StoreEvent.OnCategoryChange -> updateCategory(event.categoryId)
            StoreEvent.OnNextClick -> registerStore()
        }
    }

    private fun updateStoreName(value : String){
        _uiState.update { current ->
            current.copy(
                storeName = value,
                errorMessage = null
            )
        }
    }

    private fun updateStoreDescription(value : String){
        _uiState.update { current ->
            current.copy(
                storeDescription = value,
                errorMessage = null
            )
        }
    }

    private fun updateCategory(categoryId: String){
        _uiState.update { current ->
            current.copy(
                selectedCategory = categoryId,
                errorMessage = null
            )
        }
    }

    private fun registerStore(){
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val storeNameTrimmed = _uiState.value.storeName.trim()
            val storeDescriptionTrimmed = _uiState.value.storeDescription.trim()
            val categoryIdTrimmed = _uiState.value.selectedCategory.trim()

            if (sellerUid.isEmpty()) {
                _effect.emit(StoreEffect.ShowMessage("No se encontró el vendedor, vuelve a registrarte!!!"))
                return@launch
            }

            if (storeNameTrimmed.isEmpty()) {
                _effect.emit(StoreEffect.ShowMessage("Ingresa el nombre de la Papelería"))
                return@launch
            }

            if (storeDescriptionTrimmed.isEmpty()) {
                _effect.emit(StoreEffect.ShowMessage("Ingresa la descripción de la Papelería"))
                return@launch
            }

            if (categoryIdTrimmed.isEmpty()) {
                _effect.emit(StoreEffect.ShowMessage("Selecciona una Categoría"))
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val store = Store(
                sellerId = sellerUid,
                name = storeNameTrimmed,
                description = storeDescriptionTrimmed,
                category = categoryIdTrimmed,
                isActive = true
            )

            val result = storeRepository.createStore(store)

            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        storeName = "",
                        storeDescription = "",
                        selectedCategory = "",
                        isLoading = false,
                        errorMessage = null
                    )
                }

                _effect.emit(StoreEffect.ShowMessage("¡Papelería registrada con éxito!"))
                _effect.emit(StoreEffect.NavigateToSellerHome)
            }.onFailure { e->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "No se pudo registrar la papelería"
                    )
                }
                _effect.emit(StoreEffect.ShowMessage(
                    e.message ?: "No se pudo registrar la papelería"))
            }
        }
    }
}
