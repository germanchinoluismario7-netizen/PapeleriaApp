package com.example.papeleriaapp.domain.repository

import com.example.papeleriaapp.core.model.Product

interface ProductRepository {

    suspend fun getActiveProducts() : Result<List<Product>>

    suspend fun getProductsByStore(
        storeId : String
    ) : Result<List<Product>>

    suspend fun getProductById(
        productId : String
    ) : Result<Product?>

    /** Crea el producto y devuelve su id. */
    suspend fun createProduct(
        product : Product
    ) : Result<String>

    suspend fun updateProduct(
        product : Product
    ) : Result<Unit>

    /**
     * Activa o desactiva el producto (baja logica).
     * Se conserva el documento para no romper los pedidos que ya lo usan.
     */
    suspend fun setProductActive(
        productId : String,
        isActive : Boolean
    ) : Result<Unit>
}
