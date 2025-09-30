package com.example.projetopratico_mobile1.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.User
import com.example.projetopratico_mobile1.databinding.ActivityRegisterBinding
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast

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
            val nome = binding.edtNome.text.toString()
            val email = binding.edtEmail.text.toString()
            val senha = binding.edtSenha.text.toString()
            val confirma = binding.edtConfirma.text.toString()

            if (!Validators.notBlank(nome, email, senha, confirma)) {
                it.showToast("Preencha tudo")
                return@setOnClickListener
            }
            if (!Validators.isEmailValid(email)) {
                binding.edtEmail.error = "Email inválido"
                return@setOnClickListener
            }
            if (senha != confirma) {
                binding.edtConfirma.error = "Senhas diferentes"
                return@setOnClickListener
            }
            if (InMemoryStore.users.any { it.email.equals(email, true) }) {
                binding.edtEmail.error = "Já existe usuário"
                return@setOnClickListener
            }

            InMemoryStore.users.add(User(name = nome, email = email, password = senha))
            it.showToast("Conta criada!")
            finish()
        }
    }
}
