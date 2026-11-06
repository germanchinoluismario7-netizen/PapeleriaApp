package com.example.papeleriaapp.domain.repository

import com.example.papeleriaapp.core.model.Store

interface StoreRepository {

    suspend fun createStore(store : Store): Result<Unit>

    suspend fun getStoreBySeller(
        sellerId : String
    ): Result<Store?>

    suspend fun updateStore(
        store : Store
    ):Result<Unit>
}