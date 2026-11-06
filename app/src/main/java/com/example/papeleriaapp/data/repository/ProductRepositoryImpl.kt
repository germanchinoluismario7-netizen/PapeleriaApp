package com.example.papeleriaapp.data.repository

import com.example.papeleriaapp.core.model.Product
import com.example.papeleriaapp.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl(
    private val firestore : FirebaseFirestore
) : ProductRepository {

    private val collectionProducts : String = "products"

    private fun productsRef() = firestore.collection(collectionProducts)

    override suspend fun getActiveProducts() : Result<List<Product>> = runCatching {
        val snapshot = productsRef()
            .whereEqualTo("isActive", true)
            .get()
            .await()

        snapshot.documents
            .mapNotNull { document ->
                // Si un documento viene mal formado no rompe todo el catalogo
                runCatching { document.toObject(Product::class.java) }.getOrNull()
            }
            .sortedBy { product -> product.name.lowercase() }
    }

    override suspend fun getProductsByStore(
        storeId : String
    ) : Result<List<Product>> = runCatching {
        val storeIdTrimmed : String = storeId.trim()

        if (storeIdTrimmed.isBlank()) {
            throw IllegalArgumentException("El id de la papeleria esta vacio!!!")
        }

        val snapshot = productsRef()
            .whereEqualTo("storeId", storeIdTrimmed)
            .get()
            .await()

        snapshot.documents
            .mapNotNull { document ->
                runCatching { document.toObject(Product::class.java) }.getOrNull()
            }
            .sortedBy { product -> product.name.lowercase() }
    }

    override suspend fun getProductById(
        productId : String
    ) : Result<Product?> = runCatching {
        val productIdTrimmed : String = productId.trim()

        if (productIdTrimmed.isBlank()) {
            throw IllegalArgumentException("El id del producto esta vacio!!!")
        }

        val document = productsRef()
            .document(productIdTrimmed)
            .get()
            .await()

        document.toObject(Product::class.java)
    }

    override suspend fun createProduct(
        product : Product
    ) : Result<String> = runCatching {
        val storeIdTrimmed : String = product.storeId.trim()
        val nameTrimmed : String = product.name.trim()
        val descriptionTrimmed : String = product.description.trim()
        val categoryTrimmed : String = product.category.trim()

        if (storeIdTrimmed.isBlank()) {
            throw IllegalArgumentException("Primero debes registrar tu papelería!!!")
        }

        if (nameTrimmed.isBlank()) {
            throw IllegalArgumentException("El nombre del producto esta vacio!!!")
        }

        if (categoryTrimmed.isBlank()) {
            throw IllegalArgumentException("Debe seleccionar una categoria!!!")
        }

        if (product.price < 0.0) {
            throw IllegalArgumentException("El precio no puede ser negativo!!!")
        }

        if (product.stock < 0) {
            throw IllegalArgumentException("El stock no puede ser negativo!!!")
        }

        val productId : String = productsRef().document().id
        val createdAt : Long = System.currentTimeMillis()

        val finalProduct : Product = product.copy(
            id = productId,
            storeId = storeIdTrimmed,
            name = nameTrimmed,
            description = descriptionTrimmed,
            category = categoryTrimmed,
            isActive = true,
            createAt = createdAt
        )

        productsRef()
            .document(productId)
            .set(productToMap(finalProduct))
            .await()

        productId
    }

    override suspend fun updateProduct(
        product : Product
    ) : Result<Unit> = runCatching {
        val productId : String = product.id.trim()
        val nameTrimmed : String = product.name.trim()
        val descriptionTrimmed : String = product.description.trim()
        val categoryTrimmed : String = product.category.trim()

        if (productId.isBlank()) {
            throw IllegalArgumentException("El id del producto esta vacio!!!")
        }

        if (nameTrimmed.isBlank()) {
            throw IllegalArgumentException("El nombre del producto esta vacio!!!")
        }

        if (categoryTrimmed.isBlank()) {
            throw IllegalArgumentException("Debe seleccionar una categoria!!!")
        }

        if (product.price < 0.0) {
            throw IllegalArgumentException("El precio no puede ser negativo!!!")
        }

        if (product.stock < 0) {
            throw IllegalArgumentException("El stock no puede ser negativo!!!")
        }

        productsRef()
            .document(productId)
            .update(
                mapOf(
                    "name" to nameTrimmed,
                    "description" to descriptionTrimmed,
                    "category" to categoryTrimmed,
                    "price" to product.price,
                    "stock" to product.stock
                )
            )
            .await()

        Unit
    }

    override suspend fun setProductActive(
        productId : String,
        isActive : Boolean
    ) : Result<Unit> = runCatching {
        val productIdTrimmed : String = productId.trim()

        if (productIdTrimmed.isBlank()) {
            throw IllegalArgumentException("El id del producto esta vacio!!!")
        }

        productsRef()
            .document(productIdTrimmed)
            .update("isActive", isActive)
            .await()

        Unit
    }

    private fun productToMap(product : Product) = mapOf(
        "id" to product.id,
        "storeId" to product.storeId,
        "name" to product.name,
        "description" to product.description,
        "category" to product.category,
        "price" to product.price,
        "stock" to product.stock,
        "imageURL" to product.imageURL,
        "isActive" to product.isActive,
        "createAt" to product.createAt
    )
}
