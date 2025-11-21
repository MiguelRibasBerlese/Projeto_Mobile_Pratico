package com.example.projetopratico_mobile1.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.ActivityHomeBinding
import com.example.projetopratico_mobile1.ui.listdetail.ListDetailActivity
import com.example.projetopratico_mobile1.ui.listform.ListFormActivity
import com.example.projetopratico_mobile1.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Tela principal que mostra as listas de compras do usuário
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ListaComprasAdapter
    private val viewModel: HomeViewModel by viewModels()

    // launcher para resultado do formulário de lista
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            carregarListas()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarRecyclerView()
        configurarBusca()
        configurarFab()
        carregarListas()
    }

    override fun onStart() {
        super.onStart()
        // Auth guard - verifica se usuário está logado
        if (InMemoryStore.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun configurarRecyclerView() {
        adapter = ListaComprasAdapter(
            aoClicarLista = { lista -> abrirDetalhesLista(lista) },
            aoEditarLista = { lista -> editarLista(lista) },
            aoExcluirLista = { lista -> confirmarExclusao(lista) }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun configurarBusca() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarListas(newText.orEmpty())
                return true
            }
        })
    }

    private fun configurarFab() {
        binding.fabAdd.setOnClickListener {
            criarNovaLista()
        }
    }

    private fun carregarListas() {
        try {
            val listas = InMemoryStore.listasView
            adapter.submitList(listas)
            atualizarEmptyState(listas)
        } catch (e: Exception) {
            // em caso de erro, mostra lista vazia
            adapter.submitList(emptyList())
            atualizarEmptyState(true)
            Toast.makeText(this, "Erro ao carregar listas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filtrarListas(query: String) {
        try {
            val listas = if (query.isEmpty()) {
                InMemoryStore.listasView
            } else {
                InMemoryStore.listasView.filter {
                    it.titulo.contains(query, ignoreCase = true)
                }
            }
            adapter.submitList(listas)
            atualizarEmptyState(listas)
        } catch (e: Exception) {
            adapter.submitList(emptyList())
            atualizarEmptyState(true)
            Toast.makeText(this, "Erro ao filtrar listas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarEmptyState(listas: List<ShoppingList>) {
        atualizarEmptyState(listas.isEmpty())
    }

    private fun atualizarEmptyState(isEmpty: Boolean) {
        binding.txtEmptyState.isVisible = isEmpty
        binding.recycler.isVisible = !isEmpty
    }

    private fun criarNovaLista() {
        val intent = ListFormActivity.novaLista(this)
        formLauncher.launch(intent)
    }

    private fun editarLista(lista: ShoppingList) {
        val intent = ListFormActivity.editarLista(this, lista.id)
        formLauncher.launch(intent)
    }

    private fun confirmarExclusao(lista: ShoppingList) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Lista")
            .setMessage("Tem certeza? Todos os itens da lista serão perdidos.")
            .setPositiveButton("Excluir") { _, _ ->
                excluirLista(lista)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirLista(lista: ShoppingList) {
        InMemoryStore.removerLista(lista.id)
        carregarListas()

        Snackbar.make(binding.root, "Lista excluída", Snackbar.LENGTH_SHORT).show()
    }

    private fun abrirDetalhesLista(lista: ShoppingList) {
        val intent = Intent(this, ListDetailActivity::class.java)
        intent.putExtra("LISTA_ID", lista.id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        InMemoryStore.currentUser = null
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        carregarListas()
    }
}
