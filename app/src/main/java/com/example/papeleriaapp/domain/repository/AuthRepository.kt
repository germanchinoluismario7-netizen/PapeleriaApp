package com.example.papeleriaapp.domain.repository

import com.example.papeleriaapp.core.model.AppUser
import com.example.papeleriaapp.core.model.UserRole

interface AuthRepository {

    suspend fun registerUser(firstName: String, lastName: String, email: String,
        password : String, phone : String, role: UserRole) : Result<AppUser>

    suspend fun login(email : String, password : String) : Result<AppUser>

    suspend fun getCurrentUser() : AppUser?

    fun logout()
}