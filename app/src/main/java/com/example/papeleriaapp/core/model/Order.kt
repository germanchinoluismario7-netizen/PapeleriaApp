package com.example.papeleriaapp.core.model

data class OrderItem(
    val productId : String = "",
    val name : String = "",
    val price : Double = 0.0,
    val quantity : Int = 1
)

data class Order(
    val id : String = "",
    val customerId : String = "",
    val customerName : String = "",
    val customerEmail : String = "",
    val items : List<OrderItem> = emptyList(),
    val total : Double = 0.0,
    val status : String = OrderStatus.PENDIENTE.name,
    val createAt : Long = 0L
)

enum class OrderStatus {
    PENDIENTE,
    EN_PREPARACION,
    ENTREGADO,
    CANCELADO;

    companion object {
        val labels : Map<String, String> = mapOf(
            "PENDIENTE" to "Pendiente",
            "EN_PREPARACION" to "En preparación",
            "ENTREGADO" to "Entregado",
            "CANCELADO" to "Cancelado"
        )

        fun labelOf(status : String) : String = labels[status] ?: status
    }
}
