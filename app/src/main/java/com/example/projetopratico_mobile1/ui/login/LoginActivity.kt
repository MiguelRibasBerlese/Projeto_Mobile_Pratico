package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.databinding.ActivityLoginBinding
import com.example.projetopratico_mobile1.data.auth.AuthManager
import com.example.projetopratico_mobile1.data.auth.AuthResult
import com.example.projetopratico_mobile1.ui.home.HomeActivity
import com.example.projetopratico_mobile1.util.showToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Garante que existe usuário demo para testes
        AuthManager.ensureDemoUser()

        binding.btnEntrar.setOnClickListener { validarELogar() }
        binding.txtCadastrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validarELogar() {
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        val senha = binding.edtSenha.text?.toString().orEmpty()

        when (val result = AuthManager.signIn(email, senha)) {
            is AuthResult.Ok -> {
                // Login bem-sucedido, navegar para HomeActivity
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            is AuthResult.Error -> {
                // Exibe erro e foca no campo apropriado
                binding.root.showToast(result.message)
                when {
                    email.isBlank() -> binding.edtEmail.requestFocus()
                    senha.isBlank() -> binding.edtSenha.requestFocus()
                    else -> binding.edtSenha.requestFocus()
                }
            }
        }
    }
}
