package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.databinding.ActivityLoginBinding
import com.example.projetopratico_mobile1.ui.home.HomeActivity
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast
import com.example.projetopratico_mobile1.util.hideKeyboard

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

    override fun onResume() {
        super.onResume()
        // Limpa os campos ao retornar para a tela
        limparCampos()
    }

    private fun limparCampos() {
        binding.edtEmail.text?.clear()
        binding.edtSenha.text?.clear()
        binding.edtEmail.error = null
        binding.edtSenha.error = null
    }

    private fun validarELogar() {
        // Esconde o teclado ao validar
        binding.root.hideKeyboard()

        val email = binding.edtEmail.text.toString().trim()
        val senha = binding.edtSenha.text.toString()

        // Limpa erros anteriores
        binding.edtEmail.error = null
        binding.edtSenha.error = null

        if (email.isEmpty()) {
            binding.edtEmail.error = "Campo obrigatório"
            binding.edtEmail.requestFocus()
            return
        }

        if (senha.isEmpty()) {
            binding.edtSenha.error = "Campo obrigatório"
            binding.edtSenha.requestFocus()
            return
        }

        if (!Validators.isEmailValid(email)) {
            binding.edtEmail.error = "E-mail inválido"
            binding.edtEmail.requestFocus()
            return
        }

        val user = InMemoryStore.users.firstOrNull { it.email.equals(email, true) && it.password == senha }
        if (user == null) {
            binding.edtSenha.error = "Dados incorretos"
            binding.edtSenha.requestFocus()
            return
        }

        InMemoryStore.currentUser = user
        binding.root.showToast("Login realizado!")
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
