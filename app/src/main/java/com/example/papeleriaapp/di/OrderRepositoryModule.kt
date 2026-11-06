package com.example.papeleriaapp.di

import com.example.papeleriaapp.data.repository.OrderRepositoryImpl
import com.example.papeleriaapp.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OrderRepositoryModule {

    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore
    ) : OrderRepository =
        OrderRepositoryImpl(
            firestore = firestore
        )
}
