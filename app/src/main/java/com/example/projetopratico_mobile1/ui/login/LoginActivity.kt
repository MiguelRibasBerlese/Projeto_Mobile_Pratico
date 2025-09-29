package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.R
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

        configurarCliques()
    }

    private fun configurarCliques() {
        // botão de entrar
        binding.btnEntrar.setOnClickListener {
            tentarLogin()
        }

        // link para cadastro
        binding.linkCadastro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun tentarLogin() {
        val email = binding.campoEmail.text.toString()
        val senha = binding.campoSenha.text.toString()

        // valida campos antes de tentar login
        if (!validarCampos(email, senha)) {
            return
        }

        // tenta fazer login
        if (InMemoryStore.fazerLogin(email, senha)) {
            binding.root.showToast("Login realizado!")
            irParaHome()
        } else {
            binding.root.showToast("E-mail ou senha incorretos")
        }
    }

    private fun validarCampos(email: String, senha: String): Boolean {
        if (!Validators.notBlank(email, senha)) {
            binding.root.showToast(getString(R.string.erro_campo_vazio))
            return false
        }

        if (!Validators.isEmailValid(email)) {
            binding.root.showToast(getString(R.string.erro_email_invalido))
            return false
        }

        return true
    }

    private fun irParaHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // remove da pilha para não voltar com back
    }
}
