package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.databinding.ActivityLoginBinding
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.auth.AuthManager
import com.example.projetopratico_mobile1.data.auth.AuthResult
import com.example.projetopratico_mobile1.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        // CORREÇÃO: Não criar dados de exemplo automaticamente
        // Cada usuário inicia com lista vazia
        InMemoryStore.criarDadosExemplo()

        // Preencher campos automaticamente para teste
        binding.edtEmail.setText("demo@demo.com")
        binding.edtSenha.setText("123")

        // Garantir que usuários demo existem
        AuthManager.ensureDemoUser()

        // Adicionar botão de teste
        setupTestButton()

        // TESTE: Verificar se o elemento txtCadastrar existe
        println("DEBUG: LoginActivity - txtCadastrar encontrado: ${::binding.isInitialized && binding.txtCadastrar != null}")

        // TESTE: Adicionar clique de teste no título para verificar se eventos funcionam
        binding.txtCadastrar.setOnLongClickListener {
            println("DEBUG: LoginActivity - LONG CLICK no txtCadastrar funcionando")
            Toast.makeText(this, "Long click funcionando!", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun setupTestButton() {
        // Adicionar listener de duplo clique no texto "Esqueci minha senha" para teste de cadastro
        binding.txtEsqueciSenha.setOnLongClickListener {
            println("DEBUG: LoginActivity - Long press em txtEsqueciSenha - executando teste de cadastro")
            testDirectSignUp()
            true
        }
    }

    private fun testDirectLogin() {
        Toast.makeText(this, "Fazendo login direto...", Toast.LENGTH_SHORT).show()
        AuthManager.ensureDemoUser()
        navigateToHome()
    }

    private fun testDirectSignUp() {
        println("DEBUG: LoginActivity - testDirectSignUp() INICIADO")
        Toast.makeText(this, "Testando cadastro direto...", Toast.LENGTH_SHORT).show()

        val testEmail = "teste.${System.currentTimeMillis()}@test.com"
        val testPassword = "123456"

        AuthManager.ensureDemoUser()
        val result = AuthManager.signUp("Usuário Teste", testEmail, testPassword, testPassword)

        when (result) {
            is AuthResult.Ok -> {
                Toast.makeText(this, "TESTE: Cadastro direto funcionou! Email: $testEmail", Toast.LENGTH_LONG).show()
                println("DEBUG: LoginActivity - Teste de cadastro SUCESSO")
            }
            is AuthResult.Error -> {
                Toast.makeText(this, "TESTE: Erro no cadastro direto: ${result.message}", Toast.LENGTH_LONG).show()
                println("DEBUG: LoginActivity - Teste de cadastro ERRO: ${result.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEntrar.setOnClickListener {
            val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
            val senha = binding.edtSenha.text?.toString().orEmpty()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Desabilitar botão durante processamento
            binding.btnEntrar.isEnabled = false
            binding.btnEntrar.text = "Entrando..."

            // Usar apenas AuthManager - SEM Firebase
            val result = AuthManager.signIn(email, senha)
            when (result) {
                is AuthResult.Ok -> {
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    resetButton()
                }
            }
        }

        // Cadastro simples usando AuthManager
        binding.txtCadastrar.setOnClickListener {
            println("DEBUG: LoginActivity - Botão CADASTRAR CLICADO")
            val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
            val senha = binding.edtSenha.text?.toString().orEmpty()

            println("DEBUG: LoginActivity - Email para cadastro: '$email'")
            println("DEBUG: LoginActivity - Senha para cadastro: '$senha'")

            if (email.isNotBlank() && senha.isNotBlank()) {
                println("DEBUG: LoginActivity - Campos válidos, chamando AuthManager.signUp()")

                // Garantir que usuários demo existem
                AuthManager.ensureDemoUser()

                val result = AuthManager.signUp("Usuário", email, senha, senha)
                println("DEBUG: LoginActivity - Resultado do signUp: $result")

                when (result) {
                    is AuthResult.Ok -> {
                        println("DEBUG: LoginActivity - Cadastro realizado com SUCESSO")
                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                    is AuthResult.Error -> {
                        println("DEBUG: LoginActivity - ERRO no cadastro: ${result.message}")
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                println("DEBUG: LoginActivity - Campos vazios - email: '$email', senha: '$senha'")
                Toast.makeText(this, "Preencha email e senha para cadastrar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.txtEsqueciSenha.setOnClickListener {
            Toast.makeText(this, "Funcionalidade não disponível no modo offline", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetButton() {
        binding.btnEntrar.isEnabled = true
        binding.btnEntrar.text = "Entrar"
    }


    /**
     * Navega para Home limpando a pilha de activities
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
