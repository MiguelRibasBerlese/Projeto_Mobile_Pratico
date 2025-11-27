package com.example.projetopratico_mobile1.ui.listdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.repo.RepoProvider
import com.example.projetopratico_mobile1.databinding.ActivityListDetailBinding
import com.example.projetopratico_mobile1.ui.itemform.ItemFormActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Tela que mostra os itens de uma lista específica
 * Itens agrupados por categoria com seção de comprados separada
 * Usa MVVM com Firestore (logado) ou InMemory (offline)
 */
class ListDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListDetailBinding
    private lateinit var adapter: ItensAdapter
    private var listaId: String? = null

    // ViewModel com factory para injeção de repositório
    private val viewModel: ItemListViewModel by viewModels {
        ItemListViewModelFactory(
            repository = RepoProvider.provideItemRepository(),
            listId = listaId!!
        )
    }

    // launcher para resultado do formulário de item
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Com MVVM, não precisa reload manual - o Flow atualiza automaticamente
        if (result.resultCode == Activity.RESULT_OK) {
            // Dados já atualizados via Flow do repositório
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IMPORTANTE: definir listaId ANTES de inicializar ViewModel
        listaId = intent.getStringExtra("LISTA_ID")
        if (listaId == null) {
            finish()
            return
        }

        binding = ActivityListDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarRecyclerView()
        configurarBusca()
        configurarFab()

        // Observar mudanças de estado via Flow com lifecycle safety
        observarEstado()
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Itens da Lista"
    }

    private fun configurarRecyclerView() {
        adapter = ItensAdapter(
            aoClicarItem = { item -> editarItem(item) },
            aoToggleComprado = { item -> viewModel.togglePurchased(item) }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun configurarBusca() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Envia query para ViewModel que atualiza o Flow
                viewModel.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun configurarFab() {
        binding.fabAddItem.setOnClickListener {
            criarNovoItem()
        }
    }

    /**
     * Observa mudanças de estado do ViewModel usando Flow
     * Com lifecycle safety (repeatOnLifecycle)
     */
    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Converte dados agrupados em lista de RowItems para o adapter
                    val rowItems = AdapterDataConverter.convertToRowItems(state.groupedItems)
                    adapter.submitList(rowItems)

                    // Atualizar empty state
                    atualizarEmptyState(state.allItems.isEmpty())
                }
            }
        }
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



    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
