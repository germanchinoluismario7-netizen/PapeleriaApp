package com.example.papeleriaapp.core.model

data class Store(
    val id : String = "",
    val sellerId : String = "",
    val name : String = "",
    val description : String = "",
    val category : String = "",
    val imageURL : String = "",
    val isActive : Boolean = false,
    val createAt : Long = 0L
)
