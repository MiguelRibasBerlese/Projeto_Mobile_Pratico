package com.example.projetopratico_mobile1.ui.listform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.ActivityListFormBinding
import java.util.UUID

/**
 * Activity para criar ou editar uma lista de compras
 */
class ListFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListFormBinding
    private var listaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarTela()
        configurarEventos()
        carregarDados()
    }

    private fun configurarTela() {
        // verifica se é edição
        listaId = intent.getStringExtra("lista_id")
        if (listaId != null) {
            binding.txtTitulo.text = "Editar Lista"
        }
    }

    private fun configurarEventos() {
        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnSalvar.setOnClickListener {
            salvarLista()
        }
    }

    private fun carregarDados() {
        listaId?.let { id ->
            val lista = InMemoryStore.buscarLista(id)
            lista?.let {
                binding.edtNome.setText(it.titulo)
            }
        }
    }

    private fun salvarLista() {
        val nome = binding.edtNome.text.toString().trim()

        // valida campos
        if (nome.isEmpty()) {
            binding.edtNome.error = "Informe o nome da lista"
            return
        }

        try {
            if (listaId != null) {
                // edição
                val lista = InMemoryStore.buscarLista(listaId!!)
                lista?.let {
                    val listaAtualizada = it.copy(
                        titulo = nome,
                        imagemUri = null
                    )
                    InMemoryStore.atualizarLista(listaAtualizada)
                    Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // criação
                val novaLista = ShoppingList(
                    id = UUID.randomUUID().toString(),
                    titulo = nome,
                    imagemUri = null
                )
                InMemoryStore.adicionarLista(novaLista)
                Toast.makeText(this, "Lista criada!", Toast.LENGTH_SHORT).show()
            }

            // volta para home
            setResult(Activity.RESULT_OK)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar lista", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun novaLista(activity: Activity): Intent {
            return Intent(activity, ListFormActivity::class.java)
        }

        fun editarLista(activity: Activity, listaId: String): Intent {
            return Intent(activity, ListFormActivity::class.java).apply {
                putExtra("lista_id", listaId)
            }
        }
    }
}
