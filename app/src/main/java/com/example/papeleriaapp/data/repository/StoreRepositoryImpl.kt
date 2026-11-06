package com.example.papeleriaapp.data.repository

import com.example.papeleriaapp.core.model.Store
import com.example.papeleriaapp.domain.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StoreRepositoryImpl(
    private val firestore : FirebaseFirestore
) : StoreRepository {
    private val collectionStores : String = "stores"

    private fun storeRef() = firestore.collection(collectionStores)

    override suspend fun createStore(
        store: Store
    ): Result<Unit> = runCatching {
        val sellerIdTrimmed : String = store.sellerId.trim()
        val nameTrimmed : String = store.name.trim()
        val descriptionTrimmed : String = store.description.trim()
        val categoryTrimmed : String = store.category.trim()

        if(sellerIdTrimmed.isBlank()){
            throw IllegalArgumentException("El Id del vendedor esta vacio!!!")
        }

        if(nameTrimmed.isBlank()){
            throw IllegalArgumentException("El nombre de la tienda esta vacio!!!")
        }

        if(descriptionTrimmed.isBlank()){
            throw IllegalArgumentException("La descripcion de la tienda esta vacia!!!")
        }

        if(categoryTrimmed.isBlank()){
            throw IllegalArgumentException("Debe seleccionar una categoria!!!")
        }

        val finalStoreId : String = storeRef().document().id

        val createdAt : Long = System.currentTimeMillis()

        val finalStore : Store = store.copy(
            id = finalStoreId,
            sellerId = sellerIdTrimmed,
            name = nameTrimmed,
            description = descriptionTrimmed,
            category = categoryTrimmed,
            createAt = createdAt
        )

        val data = mapOf(
            "id" to finalStore.id,
            "sellerId" to finalStore.sellerId,
            "name" to finalStore.name,
            "description" to finalStore.description,
            "category" to finalStore.category,
            "imageURL" to finalStore.imageURL,
            "isActive" to finalStore.isActive,
            "createAt" to finalStore.createAt
        )

        storeRef()
            .document(finalStoreId)
            .set(data)
            .await()

        Unit
    }

    override suspend fun getStoreBySeller(
        sellerId: String
    ): Result<Store?> = runCatching {
        val sellerIdTrimmed : String = sellerId.trim()

        if(sellerIdTrimmed.isBlank()){
            throw IllegalArgumentException("El id del vendedor esta vacio!!!")
        }

        val snapshot = storeRef()
            .whereEqualTo("sellerId", sellerIdTrimmed)
            .limit(1)
            .get()
            .await()

        val document = snapshot.documents.firstOrNull()

        document?.toObject(Store::class.java)

    }

    override suspend fun updateStore(
        store: Store
    ): Result<Unit> = runCatching {
        val storeId : String = store.id.trim()
        val sellerIdTrimmed : String = store.sellerId.trim()
        val nameTrimmed : String = store.name.trim()
        val descriptionTrimmed : String = store.description.trim()
        val categoryTrimmed : String = store.category.trim()


        if(storeId.isBlank()){
            throw IllegalArgumentException("El Id de la tienda esta vacio!!!")
        }

        if(sellerIdTrimmed.isBlank()){
            throw IllegalArgumentException("El Id del vendedor de la tienda esta vacio!!!")
        }

        if(nameTrimmed.isBlank()){
            throw IllegalArgumentException("El nombre de la tienda esta vacio!!!")
        }

        if(categoryTrimmed.isBlank()){
            throw IllegalArgumentException("Debe seleccionar una categoria!!!")
        }

        val finalStore : Store = store.copy(
            id = storeId,
            sellerId = sellerIdTrimmed,
            name = nameTrimmed,
            description = descriptionTrimmed,
            category = categoryTrimmed
        )

        val data = mapOf(
            "id" to finalStore.id,
            "sellerId" to finalStore.sellerId,
            "name" to finalStore.name,
            "description" to finalStore.description,
            "category" to finalStore.category,
            "imageURL" to finalStore.imageURL,
            "isActive" to finalStore.isActive,
            "createAt" to finalStore.createAt
        )

        storeRef()
            .document(storeId)
            .update(data)
            .await()
        Unit
    }
}