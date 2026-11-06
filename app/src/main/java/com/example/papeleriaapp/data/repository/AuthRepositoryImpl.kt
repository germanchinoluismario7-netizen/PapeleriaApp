package com.example.papeleriaapp.data.repository

import com.example.papeleriaapp.core.model.AppUser
import com.example.papeleriaapp.core.model.UserRole
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class AuthRepositoryImpl (
    private val auth : FirebaseAuth,
    private val firestore : FirebaseFirestore
) : AuthRepository{
    private val collectionUsers : String = "users"

    private fun usersRef() =
        firestore.collection( collectionUsers)

    override suspend fun registerUser(
        firstName : String,
        lastName : String,
        email : String,
        password : String,
        phone : String,
        role : UserRole
    ) : Result<AppUser> =
        runCatching {
            val firstNameTrimmed: String = firstName.trim()
            val lastNameTrimmed: String = lastName.trim()
            val emailTrimmed: String = email.trim()
            val phoneTrimmed: String = phone.trim()

            if (firstNameTrimmed.isBlank()) {
                throw IllegalArgumentException("El nombre del usuario esta vacio!!!")
            }

            if (lastNameTrimmed.isBlank()) {
                throw IllegalArgumentException("El apellido del usuario esta vacio!!!")
            }

            if (emailTrimmed.isBlank()) {
                throw IllegalArgumentException("El correo del usuario esta vacio!!!")
            }

            if (phoneTrimmed.isBlank()) {
                throw IllegalArgumentException("El telefono del usuario esta vacio!!!")
            }

            if (password.isBlank()) {
                throw IllegalArgumentException("La contraseña del usuario esta vacia!!!")
            }

            val createdAt: Long = System.currentTimeMillis()
            val authResult = auth
                .createUserWithEmailAndPassword(emailTrimmed, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw IllegalArgumentException("No se pudo obtener el usuario registrado desde Firebase!!!")
            val finalUser = AppUser(
                uid = firebaseUser.uid,
                firstName = firstNameTrimmed,
                lastName = lastNameTrimmed,
                email = emailTrimmed,
                phone = phoneTrimmed,
                role = role.name,
                createAt = createdAt
            )

            val data = mapOf(
                "uid" to finalUser.uid,
                "firstName" to finalUser.firstName,
                "lastName" to finalUser.lastName,
                "email" to finalUser.email,
                "phone" to finalUser.phone,
                "role" to finalUser.role,
                "createAt" to finalUser.createAt
            )

            usersRef()
                .document(finalUser.uid)
                .set(data)
                .await()

            finalUser
        }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AppUser> = runCatching {
        val emailTrimmed : String = email.trim()
        if(emailTrimmed.isBlank()) {
            throw IllegalArgumentException("El correo esta vacio!!!")
        }

        if (password.isBlank()) {
            throw IllegalArgumentException("La contraseña esta vacia!!")
        }

        auth.signInWithEmailAndPassword(emailTrimmed, password)

        val currentUser = auth.currentUser
            ?: throw IllegalArgumentException("No se pudo obtener el usuario autenticado!!!")

       val snapshot = usersRef()
           .document(currentUser.uid)
           .get()
           .await()

        val appUser : AppUser =
            snapshot.toObject(AppUser::class.java)
                ?: throw IllegalArgumentException("No se encontro la informacion del usuario en Firestore!!!")
        appUser
    }

    override suspend fun getCurrentUser(): AppUser?  = runCatching {
        val currrentUser = auth.currentUser ?: return null

        val snapshot = usersRef()
            .document(currrentUser.uid)
            .get()
            .await()
        snapshot.toObject(AppUser::class.java)
    }.getOrNull()

    override fun logout() {
        auth.signOut()
    }
}