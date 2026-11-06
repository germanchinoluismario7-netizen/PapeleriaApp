package com.example.papeleriaapp.domain.repository

import com.example.papeleriaapp.core.model.Order

interface OrderRepository {

    /** Crea el pedido en Firestore y descuenta el stock de cada producto. */
    suspend fun createOrder(
        order : Order
    ) : Result<Unit>

    /** Pedidos de un cliente en particular. */
    suspend fun getMyOrders(
        customerId : String
    ) : Result<List<Order>>

    /** Todos los pedidos de la papeleria (para el vendedor). */
    suspend fun getAllOrders() : Result<List<Order>>

    suspend fun updateOrderStatus(
        orderId : String,
        status : String
    ) : Result<Unit>
}
