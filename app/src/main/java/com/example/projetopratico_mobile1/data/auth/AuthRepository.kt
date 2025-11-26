package com.example.projetopratico_mobile1.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Repository para autenticação com Firebase Auth
 * Métodos suspend para usar com await() - sem callbacks aninhados
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    /**
     * Retorna o UID do usuário atual ou null se não logado
     */
    fun currentUid(): String? = auth.currentUser?.uid

    /**
     * Criar conta com email/senha
     */
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID null após cadastro")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login com email/senha
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID null após login")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Enviar email de recuperação de senha
     */
    suspend fun recover(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout - não precisa ser suspend pois é instantâneo
     */
    fun signOut() {
        auth.signOut()
    }
}
