package com.example.projetopratico_mobile1.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.User
import com.example.projetopratico_mobile1.databinding.ActivityRegisterBinding
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast
import java.util.UUID

/**
 * Tela para cadastrar novos usuários
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarCliques()
    }

    private fun configurarCliques() {
        binding.btnSalvar.setOnClickListener {
            tentarCadastro()
        }
    }

    private fun tentarCadastro() {
        val nome = binding.campoNome.text.toString()
        val email = binding.campoEmail.text.toString()
        val senha = binding.campoSenha.text.toString()
        val confirmaSenha = binding.campoConfirmaSenha.text.toString()

        // valida todos os campos
        if (!validarCampos(nome, email, senha, confirmaSenha)) {
            return
        }

        // cria o usuário e salva
        val novoUsuario = User(
            id = UUID.randomUUID().toString(),
            name = nome,
            email = email,
            password = senha
        )

        InMemoryStore.adicionarUsuario(novoUsuario)
        binding.root.showToast(getString(R.string.conta_criada))

        // volta para a tela de login
        finish()
    }

    private fun validarCampos(nome: String, email: String, senha: String, confirmaSenha: String): Boolean {
        // verifica se todos os campos foram preenchidos
        if (!Validators.notBlank(nome, email, senha, confirmaSenha)) {
            binding.root.showToast(getString(R.string.erro_campo_vazio))
            return false
        }

        // verifica se email é válido
        if (!Validators.isEmailValid(email)) {
            binding.root.showToast(getString(R.string.erro_email_invalido))
            return false
        }

        // verifica se as senhas coincidem
        if (senha != confirmaSenha) {
            binding.root.showToast(getString(R.string.erro_senhas_diferentes))
            return false
        }

        // verifica se o email já está em uso
        if (InMemoryStore.existeEmail(email)) {
            binding.root.showToast("Este e-mail já está cadastrado")
            return false
        }

        return true
    }
}
