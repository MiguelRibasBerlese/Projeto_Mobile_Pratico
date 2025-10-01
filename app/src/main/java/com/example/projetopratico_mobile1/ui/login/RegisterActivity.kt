package com.example.projetopratico_mobile1.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.User
import com.example.projetopratico_mobile1.databinding.ActivityRegisterBinding
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast
import com.example.projetopratico_mobile1.util.hideKeyboard

/**
 * Tela para cadastrar novos usuários
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSalvar.setOnClickListener {
            validarESalvar()
        }
    }

    private fun limparErros() {
        binding.edtNome.error = null
        binding.edtEmail.error = null
        binding.edtSenha.error = null
        binding.edtConfirma.error = null
    }

    private fun validarESalvar() {
        // Esconde o teclado ao validar
        binding.root.hideKeyboard()

        val nome = binding.edtNome.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val senha = binding.edtSenha.text.toString()
        val confirma = binding.edtConfirma.text.toString()

        // Limpa erros anteriores
        limparErros()

        if (nome.isEmpty()) {
            binding.edtNome.error = "Campo obrigatório"
            binding.edtNome.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.edtEmail.error = "Campo obrigatório"
            binding.edtEmail.requestFocus()
            return
        }

        if (!Validators.isEmailValid(email)) {
            binding.edtEmail.error = "E-mail inválido"
            binding.edtEmail.requestFocus()
            return
        }

        if (InMemoryStore.users.any { it.email.equals(email, true) }) {
            binding.edtEmail.error = "E-mail já cadastrado"
            binding.edtEmail.requestFocus()
            return
        }

        if (senha.isEmpty()) {
            binding.edtSenha.error = "Campo obrigatório"
            binding.edtSenha.requestFocus()
            return
        }

        if (senha.length < 6) {
            binding.edtSenha.error = "Mínimo 6 caracteres"
            binding.edtSenha.requestFocus()
            return
        }

        if (confirma.isEmpty()) {
            binding.edtConfirma.error = "Campo obrigatório"
            binding.edtConfirma.requestFocus()
            return
        }

        if (senha != confirma) {
            binding.edtConfirma.error = "Senhas não conferem"
            binding.edtConfirma.requestFocus()
            return
        }

        InMemoryStore.users.add(User(name = nome, email = email, password = senha))
        binding.root.showToast("Conta criada!")
        finish()
    }
}
