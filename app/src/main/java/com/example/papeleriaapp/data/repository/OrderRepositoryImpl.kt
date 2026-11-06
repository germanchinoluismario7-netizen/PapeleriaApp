package com.example.papeleriaapp.data.repository

import com.example.papeleriaapp.core.model.Order
import com.example.papeleriaapp.core.model.OrderStatus
import com.example.papeleriaapp.domain.repository.OrderRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepositoryImpl(
    private val firestore : FirebaseFirestore
) : OrderRepository {

    private val collectionOrders : String = "orders"
    private val collectionProducts : String = "products"

    private fun ordersRef() = firestore.collection(collectionOrders)

    override suspend fun createOrder(
        order : Order
    ) : Result<Unit> = runCatching {
        val items = order.items

        if (items.isEmpty()) {
            throw IllegalArgumentException("El pedido no tiene productos!!!")
        }

        val customerIdTrimmed : String = order.customerId.trim()

        if (customerIdTrimmed.isBlank()) {
            throw IllegalArgumentException("El id del cliente esta vacio!!!")
        }

        val orderId : String = ordersRef().document().id
        val createdAt : Long = System.currentTimeMillis()
        val total : Double = items.sumOf { item -> item.price * item.quantity }

        val finalOrder : Order = order.copy(
            id = orderId,
            customerId = customerIdTrimmed,
            total = total,
            status = OrderStatus.PENDIENTE.name,
            createAt = createdAt
        )

        val data = mapOf(
            "id" to finalOrder.id,
            "customerId" to finalOrder.customerId,
            "customerName" to finalOrder.customerName,
            "customerEmail" to finalOrder.customerEmail,
            "items" to items.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "name" to item.name,
                    "price" to item.price,
                    "quantity" to item.quantity
                )
            },
            "total" to finalOrder.total,
            "status" to finalOrder.status,
            "createAt" to finalOrder.createAt
        )

        // Un solo lote: crea el pedido y descuenta el stock de cada producto
        val batch = firestore.batch()
        batch.set(ordersRef().document(orderId), data)

        items.forEach { item ->
            if (item.productId.isNotBlank() && item.quantity > 0) {
                batch.update(
                    firestore.collection(collectionProducts).document(item.productId),
                    "stock",
                    FieldValue.increment(-item.quantity.toLong())
                )
            }
        }

        batch.commit().await()

        Unit
    }

    override suspend fun getMyOrders(
        customerId : String
    ) : Result<List<Order>> = runCatching {
        val customerIdTrimmed : String = customerId.trim()

        if (customerIdTrimmed.isBlank()) {
            throw IllegalArgumentException("El id del cliente esta vacio!!!")
        }

        val snapshot = ordersRef()
            .whereEqualTo("customerId", customerIdTrimmed)
            .get()
            .await()

        snapshot.documents
            .mapNotNull { document ->
                runCatching { document.toObject(Order::class.java) }.getOrNull()
            }
            .sortedByDescending { order -> order.createAt }
    }

    override suspend fun getAllOrders() : Result<List<Order>> = runCatching {
        val snapshot = ordersRef()
            .orderBy("createAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()

        snapshot.documents
            .mapNotNull { document ->
                runCatching { document.toObject(Order::class.java) }.getOrNull()
            }
    }

    override suspend fun updateOrderStatus(
        orderId : String,
        status : String
    ) : Result<Unit> = runCatching {
        val orderIdTrimmed : String = orderId.trim()

        if (orderIdTrimmed.isBlank()) {
            throw IllegalArgumentException("El id del pedido esta vacio!!!")
        }

        val allowed : List<String> = OrderStatus.entries.map { entry -> entry.name }

        if (status !in allowed) {
            throw IllegalArgumentException("El estado del pedido no es valido!!!")
        }

        ordersRef()
            .document(orderIdTrimmed)
            .update("status", status)
            .await()

        Unit
    }
}
