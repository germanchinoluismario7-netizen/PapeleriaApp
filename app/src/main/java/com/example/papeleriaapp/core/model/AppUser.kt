package com.example.papeleriaapp.core.model

data class AppUser(
    val uid : String = "",
    val firstName : String = "",
    val lastName : String = "",
    val email : String = "",
    val phone : String = "",
    val role : String = "",
    val hasStore: Boolean = false,
    val storeId : String? = null,
    val createAt : Long = 0L
)
