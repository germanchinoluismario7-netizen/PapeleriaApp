package com.example.papeleriaapp.core.model

data class Product(
    val id : String = "",
    val storeId : String = "",
    val name : String = "",
    val description : String = "",
    val category : String = "",
    val price : Double = 0.0,
    val stock : Int = 0,
    val imageURL : String = "",
    val isActive : Boolean = true,
    val createAt : Long = 0L
)
