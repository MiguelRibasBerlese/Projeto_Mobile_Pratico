// Arquivo de verificação rápida para identificar erros de compilação
package com.example.projetopratico_mobile1.test

// Imports essenciais dos modelos de dados
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.data.models.User

// Imports de autenticação
import com.example.projetopratico_mobile1.data.auth.AuthManager
import com.example.projetopratico_mobile1.data.auth.AuthResult

// Imports de utilitários
import com.example.projetopratico_mobile1.util.Validators

/**
 * Classe de teste para verificar se todos os componentes estão acessíveis
 */
class CompilationTest {

    fun testImports() {
        // Se este arquivo compilar, significa que todos os imports estão corretos
        println("Todos os imports estão funcionando!")
    }

    fun testModels() {
        // Testa criação de modelos básicos
        val categoria = Categoria.ALIMENTOS
        val item = Item("1", "Arroz", 1.0, "kg", categoria, false)
        val lista = ShoppingList("1", "Supermercado", null, mutableListOf())
        val user = User(
            name = "João",
            email = "joao@test.com",
            password = "123"
        )

        println("Modelos criados com sucesso!")
    }

    fun testAuth() {
        // Testa autenticação
        AuthManager.ensureDemoUser()
        val isLoggedIn = AuthManager.isLoggedIn()

        // Testa login
        val result = AuthManager.signIn("demo@demo.com", "123")

        println("Autenticação testada: logado=$isLoggedIn, resultado=$result")
    }

    fun testValidation() {
        // Testa validações
        val emailValido = Validators.isEmailValid("demo@demo.com")
        val emailInvalido = Validators.isEmailValid("email_invalido")

        println("Validação testada: emailValido=$emailValido, emailInvalido=$emailInvalido")
    }
}
