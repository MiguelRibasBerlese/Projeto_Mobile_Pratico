package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.databinding.ActivityRegisterBinding
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.auth.AuthManager
import com.example.projetopratico_mobile1.data.auth.AuthResult
import com.example.projetopratico_mobile1.ui.home.HomeActivity
import com.example.projetopratico_mobile1.util.showToast

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configura toolbar para voltar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSalvar.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString()
            val confirma = binding.edtConfirma.text.toString()

            when (val result = AuthManager.signUp(nome, email, senha, confirma)) {
                is AuthResult.Ok -> {
                    // Cadastro bem-sucedido - navegar para HomeActivity (sem dados de exemplo)
                    binding.root.showToast("Conta criada com sucesso!")
                    InMemoryStore.criarDadosExemplo() // Inicializa estrutura vazia para o usuário

                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is AuthResult.Error -> {
                    // Exibe erro e foca no campo apropriado
                    binding.root.showToast(result.message)
                    when {
                        nome.isBlank() -> binding.edtNome.requestFocus()
                        email.isBlank() || result.message.contains("E-mail") -> binding.edtEmail.requestFocus()
                        senha.isBlank() -> binding.edtSenha.requestFocus()
                        result.message.contains("coincidem") -> binding.edtConfirma.requestFocus()
                        else -> binding.edtNome.requestFocus()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
