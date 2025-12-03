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
     * Garante que existem usuários demo para testes
     */
    fun ensureDemoUser() {
        if (InMemoryStore.users.isEmpty()) {
            // Usuário demo principal
            val demoUser = User(
                name = "Demo User",
                email = "demo@demo.com",
                password = "123"
            )
            InMemoryStore.users.add(demoUser)

            // Usuário admin para testes
            val adminUser = User(
                name = "Admin",
                email = "admin@admin.com",
                password = "admin"
            )
            InMemoryStore.users.add(adminUser)

            // Usuário teste
            val testUser = User(
                name = "Teste",
                email = "teste@teste.com",
                password = "teste"
            )
            InMemoryStore.users.add(testUser)
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
        println("DEBUG: AuthManager - signUp() INICIADO")
        println("DEBUG: AuthManager - name: '$name', email: '$email', password: '$password', confirmPassword: '$confirmPassword'")

        if (name.isBlank()) {
            println("DEBUG: AuthManager - ERRO: Nome vazio")
            return AuthResult.Error("Nome não pode estar vazio")
        }

        if (!Validators.isEmailValid(email)) {
            println("DEBUG: AuthManager - ERRO: Email inválido '$email'")
            return AuthResult.Error("E-mail inválido")
        }

        if (password.isBlank()) {
            println("DEBUG: AuthManager - ERRO: Senha vazia")
            return AuthResult.Error("Senha não pode estar vazia")
        }

        if (password != confirmPassword) {
            println("DEBUG: AuthManager - ERRO: Senhas não coincidem: '$password' != '$confirmPassword'")
            return AuthResult.Error("Senhas não coincidem")
        }

        // Verifica se email já existe
        println("DEBUG: AuthManager - Verificando se email já existe")
        println("DEBUG: AuthManager - Usuários existentes: ${InMemoryStore.users.size}")
        InMemoryStore.users.forEach { user ->
            println("DEBUG: AuthManager - Usuário existente: ${user.email}")
        }

        if (InMemoryStore.users.any { it.email.equals(email, ignoreCase = true) }) {
            println("DEBUG: AuthManager - ERRO: Email já cadastrado '$email'")
            return AuthResult.Error("E-mail já cadastrado")
        }

        println("DEBUG: AuthManager - Criando novo usuário")
        val newUser = User(
            name = name,
            email = email,
            password = password
        )

        InMemoryStore.users.add(newUser)
        InMemoryStore.currentUser = newUser

        println("DEBUG: AuthManager - Usuário criado com sucesso: ${newUser.email}")
        println("DEBUG: AuthManager - Total de usuários agora: ${InMemoryStore.users.size}")

        return AuthResult.Ok(newUser)
    }

    /**
     * Verifica se há usuário logado (em memória)
     */
    fun isLoggedIn(): Boolean {
        return InMemoryStore.currentUser != null
    }

    /**
     * Retorna o usuário atual logado
     */
    fun getCurrentUser(): User? {
        return InMemoryStore.currentUser
    }

    /**
     * Logout - limpa usuário atual
     */
    fun signOut() {
        val currentUserId = InMemoryStore.currentUser?.id
        println("DEBUG: AuthManager - Fazendo logout do usuário: $currentUserId")
        InMemoryStore.currentUser = null
        println("DEBUG: AuthManager - Logout realizado, usuário atual limpo")
    }
}
