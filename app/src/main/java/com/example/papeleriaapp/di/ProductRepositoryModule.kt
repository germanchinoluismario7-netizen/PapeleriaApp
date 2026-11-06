package com.example.papeleriaapp.di

import com.example.papeleriaapp.data.repository.ProductRepositoryImpl
import com.example.papeleriaapp.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProductRepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        firestore: FirebaseFirestore
    ) : ProductRepository =
        ProductRepositoryImpl(
            firestore = firestore
        )
}
