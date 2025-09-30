package com.example.projetopratico_mobile1.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.databinding.ActivityLoginBinding
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast
import com.example.projetopratico_mobile1.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntrar.setOnClickListener { validarELogar() }
        binding.txtCadastrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun tentarLogin() {
        val email = binding.edtEmail.text.toString()
        val senha = binding.edtSenha.text.toString()

        // valida campos antes de tentar login
            binding.edtEmail.error = "E-mail inválido"
            return
        }

            binding.edtSenha.error = "Informe a senha"
        if (InMemoryStore.fazerLogin(email, senha)) {
            binding.root.showToast("Login realizado!")
            irParaHome()
        // Autenticação virá no C3
        binding.root.showToast("Validação ok (mock)")
            binding.root.showToast(getString(R.string.erro_campo_vazio))
        }

        return true
    }

    private fun irParaHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // remove da pilha para não voltar com back
    }
}
