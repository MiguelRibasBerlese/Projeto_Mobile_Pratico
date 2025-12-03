package com.example.projetopratico_mobile1.ui.itemform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.data.repo.InMemoryItemRepository
import com.example.projetopratico_mobile1.data.repo.RepoProvider
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
    private var unidadeSelecionada: String = ""

    // ViewModel será inicializado após obter listaId
    private lateinit var itemViewModel: ItemViewModel

    // Opções de unidade conforme especificação
    private val opcoesUnidade = arrayOf("un", "kg", "g", "L", "mL", "cx", "pct")

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

        // Inicializar ViewModel após obter listaId
        val factory = ItemViewModelFactory(RepoProvider.provideItemRepository(), listaId!!)
        itemViewModel = ViewModelProvider(this, factory)[ItemViewModel::class.java]

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

        configurarDropdownUnidade()
    }

    private fun configurarDropdownUnidade() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcoesUnidade)
        binding.edtUnidade.setAdapter(adapter)

        // listener para capturar seleção
        binding.edtUnidade.setOnItemClickListener { _, _, position, _ ->
            unidadeSelecionada = opcoesUnidade[position]
            binding.tilUnidade.error = null // limpa erro se houver
        }
    }

    private fun carregarDados() {
        itemId?.let { id ->
            val lista = InMemoryStore.buscarLista(listaId!!)
            val item = lista?.itens?.find { it.id == id }

            item?.let {
                binding.edtNome.setText(it.nome)
                binding.edtQuantidade.setText(it.quantidade.toString())

                // Define a unidade no dropdown
                unidadeSelecionada = it.unidade
                binding.edtUnidade.setText(it.unidade, false) // false = não dispara listener

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

        // Validação de unidade obrigatória
        if (unidadeSelecionada.isEmpty()) {
            binding.tilUnidade.error = "Selecione uma unidade"
            binding.edtUnidade.requestFocus()
            return
        }

        // Unidade já está na forma final (un, kg, g, etc.)
        val unidadeFinal = unidadeSelecionada

        try {
            if (itemId != null) {
                // edição - atualizar item existente
                val itemAtualizado = Item(
                    id = itemId!!,
                    nome = nome,
                    quantidade = quantidade,
                    unidade = unidadeFinal,
                    categoria = categoriaSelecionada!!,
                    comprado = false // mantem como false na edição
                )
                itemViewModel.updateItem(itemAtualizado)
                Toast.makeText(this, "Item atualizado!", Toast.LENGTH_SHORT).show()
            } else {
                // criação - novo item
                val novoItem = Item(
                    id = UUID.randomUUID().toString(),
                    nome = nome,
                    quantidade = quantidade,
                    unidade = unidadeFinal,
                    categoria = categoriaSelecionada!!,
                    comprado = false
                )
                itemViewModel.addItem(novoItem)
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
