package com.example.papeleriaapp.di

import com.example.papeleriaapp.data.repository.StoreRepositoryImpl
import com.example.papeleriaapp.domain.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StoreRepositoryModule {

    @Provides
    @Singleton
    fun provideStoreRepository(
        firestore: FirebaseFirestore
    ) : StoreRepository =
        StoreRepositoryImpl(
            firestore = firestore
    )
}