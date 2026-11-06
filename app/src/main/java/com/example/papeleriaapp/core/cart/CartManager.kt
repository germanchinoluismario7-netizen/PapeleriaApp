package com.example.papeleriaapp.core.cart

import com.example.papeleriaapp.core.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class CartItem(
    val productId : String = "",
    val name : String = "",
    val price : Double = 0.0,
    val quantity : Int = 1,
    val stock : Int = 0,
    val imageURL : String = ""
)

/**
 * Carrito en memoria de la sesion (singleton en Hilt).
 * Se vacia al realizar el pedido o al cerrar sesion.
 */
@Singleton
class CartManager @Inject constructor() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items : StateFlow<List<CartItem>> = _items.asStateFlow()

    fun add(product : Product) {
        _items.update { current ->
            val existing = current.firstOrNull { it.productId == product.id }
            if (existing == null) {
                current + CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = 1,
                    stock = product.stock,
                    imageURL = product.imageURL
                )
            } else {
                val newQuantity = (existing.quantity + 1).coerceAtMost(product.stock.coerceAtLeast(1))
                current.map { item ->
                    if (item.productId == product.id) item.copy(quantity = newQuantity) else item
                }
            }
        }
    }

    fun increase(productId : String) {
        _items.update { current ->
            current.map { item ->
                if (item.productId == productId) {
                    item.copy(quantity = (item.quantity + 1).coerceAtMost(item.stock.coerceAtLeast(1)))
                } else {
                    item
                }
            }
        }
    }

    fun decrease(productId : String) {
        _items.update { current ->
            current.mapNotNull { item ->
                if (item.productId != productId) {
                    item
                } else if (item.quantity <= 1) {
                    null
                } else {
                    item.copy(quantity = item.quantity - 1)
                }
            }
        }
    }

    fun remove(productId : String) {
        _items.update { current -> current.filterNot { it.productId == productId } }
    }

    fun clear() {
        _items.value = emptyList()
    }

    fun count() : Int = _items.value.sumOf { it.quantity }

    fun total() : Double = _items.value.sumOf { it.price * it.quantity }
}
