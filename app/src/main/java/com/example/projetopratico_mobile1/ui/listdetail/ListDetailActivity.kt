package com.example.projetopratico_mobile1.ui.listdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.databinding.ActivityListDetailBinding
import com.example.projetopratico_mobile1.ui.itemform.ItemFormActivity
import com.example.projetopratico_mobile1.util.GroupingUtils
import com.google.android.material.snackbar.Snackbar

/**
 * Tela que mostra os itens de uma lista específica
 * Itens agrupados por categoria com seção de comprados separada
 */
class ListDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListDetailBinding
    private lateinit var adapter: ItensAdapter
    private val viewModel: ListDetailViewModel by viewModels()
    private var listaId: String? = null
    private var termoBusca: String = ""

    // launcher para resultado do formulário de item
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            carregarItens()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listaId = intent.getStringExtra("LISTA_ID")
        if (listaId == null) {
            finish()
            return
        }

        configurarToolbar()
        configurarRecyclerView()
        configurarBusca()
        configurarFab()
        carregarItens()
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // mostra nome da lista na toolbar
        val lista = InMemoryStore.buscarLista(listaId!!)
        supportActionBar?.title = lista?.titulo ?: "Lista"
    }

    private fun configurarRecyclerView() {
        adapter = ItensAdapter(
            aoClicarItem = { item -> editarItem(item) },
            aoToggleComprado = { item -> toggleComprado(item) }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun configurarBusca() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                termoBusca = newText.orEmpty()
                carregarItens()
                return true
            }
        })
    }

    private fun configurarFab() {
        binding.fabAddItem.setOnClickListener {
            criarNovoItem()
        }
    }

    private fun carregarItens() {
        val lista = InMemoryStore.buscarLista(listaId!!) ?: return

        // filtra itens se tem busca
        val itensFiltrados = if (termoBusca.isEmpty()) {
            lista.itens
        } else {
            // filtra apenas não comprados por nome
            lista.itens.filter { !it.comprado && it.nome.contains(termoBusca, ignoreCase = true) }
        }

        // se não tem busca, mostra agrupado com comprados
        val dadosAgrupados = if (termoBusca.isEmpty()) {
            GroupingUtils.buildDataWithComprados(lista.itens)
        } else {
            // se tem busca, só mostra não comprados agrupados
            GroupingUtils.buildGroupedData(itensFiltrados)
        }

        adapter.submitList(dadosAgrupados)
        atualizarEmptyState(dadosAgrupados.isEmpty())
    }

    private fun atualizarEmptyState(isEmpty: Boolean) {
        binding.txtEmptyState.isVisible = isEmpty
        binding.recycler.isVisible = !isEmpty
    }

    private fun criarNovoItem() {
        val intent = ItemFormActivity.novoItem(this, listaId!!)
        formLauncher.launch(intent)
    }

    private fun editarItem(item: Item) {
        val intent = ItemFormActivity.editarItem(this, listaId!!, item.id)
        formLauncher.launch(intent)
    }

    private fun toggleComprado(item: Item) {
        val lista = InMemoryStore.buscarLista(listaId!!) ?: return

        // encontra o item na lista e alterna estado
        val itemIndex = lista.itens.indexOfFirst { it.id == item.id }
        if (itemIndex != -1) {
            lista.itens[itemIndex] = item.copy(comprado = !item.comprado)

            val mensagem = if (!item.comprado) "Item marcado como comprado" else "Item desmarcado"
            Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_SHORT).show()

            carregarItens() // recarrega para agrupar novamente
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
