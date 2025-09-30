package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.databinding.ActivityLoginBinding
import com.example.projetopratico_mobile1.ui.home.HomeActivity
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast

/**
 * Tela de login do usuário
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // botão de entrar
        binding.btnEntrar.setOnClickListener {
            validarELogar()
        }

        // link para cadastro
        binding.linkCadastro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarELogar() {
        val email = binding.edtEmail.text.toString()
        val senha = binding.edtSenha.text.toString()

        if (!Validators.notBlank(email, senha)) {
            binding.root.showToast("Preencha todos os campos")
            return
        }

        if (!Validators.isEmailValid(email)) {
            binding.edtEmail.error = "Email inválido"
            return
        }

        val user = InMemoryStore.users.firstOrNull { it.email.equals(email, true) && it.password == senha }
        if (user == null) {
            binding.edtSenha.error = "Credenciais inválidas"
            return
        }

        InMemoryStore.currentUser = user
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
