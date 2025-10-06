package com.example.projetopratico_mobile1.data.auth

import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.User
import com.example.projetopratico_mobile1.util.Validators

/**
 * Resultado das operações de autenticação
 */
sealed class AuthResult {
    data class Ok(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Gerenciador de autenticação simples para operações em RAM
 */
object AuthManager {

    /**
     * Garante que existe um usuário demo para testes
     */
    fun ensureDemoUser() {
        if (InMemoryStore.users.isEmpty()) {
            val demoUser = User(
                name = "Demo",
                email = "demo@demo.com",
                password = "123"
            )
            InMemoryStore.users.add(demoUser)
        }
    }

    /**
     * Realiza login com email e senha
     */
    fun signIn(email: String, password: String): AuthResult {
        if (!Validators.isEmailValid(email)) {
            return AuthResult.Error("E-mail inválido")
        }

        if (password.isBlank()) {
            return AuthResult.Error("Senha não pode estar vazia")
        }

        val user = InMemoryStore.users.firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }

        return if (user != null) {
            InMemoryStore.currentUser = user
            AuthResult.Ok(user)
        } else {
            AuthResult.Error("Credenciais inválidas")
        }
    }

    /**
     * Realiza cadastro de novo usuário
     */
    fun signUp(name: String, email: String, password: String, confirmPassword: String): AuthResult {
        if (name.isBlank()) {
            return AuthResult.Error("Nome não pode estar vazio")
        }

        if (!Validators.isEmailValid(email)) {
            return AuthResult.Error("E-mail inválido")
        }

        if (password.isBlank()) {
            return AuthResult.Error("Senha não pode estar vazia")
        }

        if (password != confirmPassword) {
            return AuthResult.Error("Senhas não coincidem")
        }

        // Verifica se email já existe
        if (InMemoryStore.users.any { it.email.equals(email, ignoreCase = true) }) {
            return AuthResult.Error("E-mail já cadastrado")
        }

        val newUser = User(
            name = name,
            email = email,
            password = password
        )

        InMemoryStore.users.add(newUser)
        InMemoryStore.currentUser = newUser

        return AuthResult.Ok(newUser)
    }
}
