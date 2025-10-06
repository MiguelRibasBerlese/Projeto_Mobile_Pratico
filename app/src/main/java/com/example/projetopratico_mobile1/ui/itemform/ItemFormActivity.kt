package com.example.projetopratico_mobile1.ui.itemform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.databinding.ActivityItemFormBinding
import java.util.UUID

/**
 * Activity para criar ou editar um item da lista
 */
class ItemFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemFormBinding
    private var listaId: String? = null
    private var itemId: String? = null
    private var categoriaSelecionada: Categoria? = null
    private var unidadeSelecionada: String = "Unidade"

    // Opções de unidade disponíveis
    private val opcoesUnidade = arrayOf("Unidade", "Gramas (g)", "Kilogramas (kg)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listaId = intent.getStringExtra("lista_id")
        itemId = intent.getStringExtra("item_id")

        if (listaId == null) {
            finish()
            return
        }

        configurarTela()
        configurarSpinner()
        configurarEventos()
        carregarDados()
    }

    private fun configurarTela() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (itemId != null) {
            binding.txtTitulo.text = "Editar Item"
        } else {
            binding.txtTitulo.text = "Novo Item"
        }
    }

    private fun configurarSpinner() {
        // configura spinner de categorias (exceto COMPRADOS)
        val categorias = Categoria.values().filter { it != Categoria.COMPRADOS }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categorias.map { it.nome }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter

        binding.spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoriaSelecionada = categorias[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                categoriaSelecionada = null
            }
        }
    }

    private fun configurarEventos() {
        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnSalvar.setOnClickListener {
            salvarItem()
        }

        binding.btnUnidade.setOnClickListener {
            mostrarDialogoUnidade()
        }
    }

    private fun mostrarDialogoUnidade() {
        AlertDialog.Builder(this)
            .setTitle("Selecione")
            .setItems(opcoesUnidade) { _, which ->
                unidadeSelecionada = opcoesUnidade[which]
                binding.btnUnidade.text = unidadeSelecionada
            }
            .show()
    }

    private fun carregarDados() {
        itemId?.let { id ->
            val lista = InMemoryStore.buscarLista(listaId!!)
            val item = lista?.itens?.find { it.id == id }

            item?.let {
                binding.edtNome.setText(it.nome)
                binding.edtQuantidade.setText(it.quantidade.toString())

                // Converte a abreviação armazenada para o texto completo no botão
                unidadeSelecionada = when (it.unidade) {
                    "Uni" -> "Unidade"
                    "g" -> "Gramas (g)"
                    "kg" -> "Kilogramas (kg)"
                    else -> if (it.unidade.isNotEmpty()) it.unidade else "Unidade"
                }
                binding.btnUnidade.text = unidadeSelecionada

                // seleciona categoria no spinner
                val categorias = Categoria.values().filter { cat -> cat != Categoria.COMPRADOS }
                val index = categorias.indexOf(it.categoria)
                if (index >= 0) {
                    binding.spinnerCategoria.setSelection(index)
                }
            }
        }
    }

    private fun salvarItem() {
        val nome = binding.edtNome.text.toString().trim()
        val quantidadeText = binding.edtQuantidade.text.toString().trim()

        // valida campos obrigatórios
        if (nome.isEmpty()) {
            binding.edtNome.error = "Informe o nome"
            binding.edtNome.requestFocus()
            return
        }

        if (quantidadeText.isEmpty()) {
            binding.edtQuantidade.error = "Informe a quantidade"
            binding.edtQuantidade.requestFocus()
            return
        }

        val quantidade = quantidadeText.toDoubleOrNull()
        if (quantidade == null || quantidade <= 0) {
            binding.edtQuantidade.error = "Quantidade deve ser maior que 0"
            binding.edtQuantidade.requestFocus()
            return
        }

        if (categoriaSelecionada == null) {
            Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show()
            return
        }

        // Converte a unidade selecionada para abreviação
        val unidadeAbreviada = when (unidadeSelecionada) {
            "Unidade" -> "Uni"
            "Gramas (g)" -> "g"
            "Kilogramas (kg)" -> "kg"
            else -> unidadeSelecionada
        }

        try {
            val lista = InMemoryStore.buscarLista(listaId!!) ?: return

            if (itemId != null) {
                // edição
                val itemIndex = lista.itens.indexOfFirst { it.id == itemId }
                if (itemIndex != -1) {
                    val itemAtualizado = lista.itens[itemIndex].copy(
                        nome = nome,
                        quantidade = quantidade,
                        unidade = unidadeAbreviada,
                        categoria = categoriaSelecionada!!
                    )
                    lista.itens[itemIndex] = itemAtualizado
                    Toast.makeText(this, "Item atualizado!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // criação
                val novoItem = Item(
                    id = UUID.randomUUID().toString(),
                    nome = nome,
                    quantidade = quantidade,
                    unidade = unidadeAbreviada,
                    categoria = categoriaSelecionada!!,
                    comprado = false
                )
                lista.itens.add(novoItem)
                Toast.makeText(this, "Item adicionado!", Toast.LENGTH_SHORT).show()
            }

            setResult(Activity.RESULT_OK)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar item", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        fun novoItem(activity: Activity, listaId: String): Intent {
            return Intent(activity, ItemFormActivity::class.java).apply {
                putExtra("lista_id", listaId)
            }
        }

        fun editarItem(activity: Activity, listaId: String, itemId: String): Intent {
            return Intent(activity, ItemFormActivity::class.java).apply {
                putExtra("lista_id", listaId)
                putExtra("item_id", itemId)
            }
        }
    }
}
