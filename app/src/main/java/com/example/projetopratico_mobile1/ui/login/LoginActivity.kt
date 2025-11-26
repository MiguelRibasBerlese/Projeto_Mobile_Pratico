package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projetopratico_mobile1.databinding.ActivityLoginBinding
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.auth.AuthRepository
import com.example.projetopratico_mobile1.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // ViewModel com Factory para passar a dependência AuthRepository
    private val authViewModel: AuthViewModel by viewModels {
        AuthVMFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeAuthState()

        // Criar dados de exemplo para funcionar na Fase 1
        InMemoryStore.criarDadosExemplo()
    }

    private fun setupClickListeners() {
        binding.btnEntrar.setOnClickListener {
            val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
            val senha = binding.edtSenha.text?.toString().orEmpty()
            authViewModel.signIn(email, senha)
        }

        // Por enquanto, usar a mesma tela para cadastro (simplificado)
        binding.txtCadastrar.setOnClickListener {
            val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
            val senha = binding.edtSenha.text?.toString().orEmpty()

            if (email.isNotBlank() && senha.isNotBlank()) {
                authViewModel.signUp(email, senha)
            } else {
                Toast.makeText(this, "Preencha email e senha para cadastrar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.txtEsqueciSenha.setOnClickListener {
            val email = binding.edtEmail.text?.toString()?.trim().orEmpty()

            if (email.isNotBlank()) {
                authViewModel.recover(email)
            } else {
                Toast.makeText(this, "Digite o email para recuperar a senha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Observa mudanças no estado de auth com repeatOnLifecycle para evitar vazamentos
     */
    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                authViewModel.state.collect { state ->
                    handleAuthState(state)
                }
            }
        }
    }

    /**
     * Reage aos estados: loading, error, sucesso (uid != null)
     */
    private fun handleAuthState(state: AuthState) {
        // Loading - desabilita botões
        binding.btnEntrar.isEnabled = !state.isLoading

        // Error - mostra toast e limpa
        state.error?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }

        // Sucesso - navegação com CLEAR_TASK
        state.uid?.let { uid ->
            navigateToHome()
        }
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
