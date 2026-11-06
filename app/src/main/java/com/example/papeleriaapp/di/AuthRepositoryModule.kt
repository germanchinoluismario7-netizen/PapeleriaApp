package com.example.papeleriaapp.di

import com.example.papeleriaapp.data.repository.AuthRepositoryImpl
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthRepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth : FirebaseAuth,
        firestore : FirebaseFirestore
    ) : AuthRepository =
        AuthRepositoryImpl(
            auth = auth,
            firestore = firestore
        )
}