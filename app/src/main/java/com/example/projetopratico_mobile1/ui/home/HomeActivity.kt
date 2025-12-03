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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.auth.AuthManager
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.repo.RepoProvider
import com.example.projetopratico_mobile1.databinding.ActivityHomeBinding
import com.example.projetopratico_mobile1.ui.listdetail.ListDetailActivity
import com.example.projetopratico_mobile1.ui.listform.ListFormActivity
import com.example.projetopratico_mobile1.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * Tela principal que mostra as listas de compras do usuário
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ListaComprasAdapter

    // ViewModel com factory para injeção de dependência - usa RepoProvider
    private val viewModel: HomeViewModel by viewModels {
        ListViewModelFactory(RepoProvider.provideListRepository(this))
    }

    // launcher para resultado do formulário de lista
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Com MVVM, não precisa reload manual - o Flow atualiza automaticamente
        if (result.resultCode == Activity.RESULT_OK) {
            // Dados já atualizados via Flow
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

        // Observar mudanças de estado via Flow
        observarEstado()
    }

    override fun onStart() {
        super.onStart()
        // Auth Guard - verifica se usuário está logado (Firebase Auth ou AuthManager)
        val isLoggedIn = isUserLoggedIn()

        if (!isLoggedIn) {
            // Não logado - redireciona para LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // CORREÇÃO: Não criar dados de exemplo automaticamente
        // Usuário deve criar suas próprias listas
        InMemoryStore.criarDadosExemplo()
    }

    // REMOVIDO onResume problemático - O StateFlow já é reativo

    /**
     * Verifica se usuário está logado (AuthManager primeiro, depois Firebase se necessário)
     */
    private fun isUserLoggedIn(): Boolean {
        // Primeiro verifica AuthManager (Fase 1 - dados locais)
        if (AuthManager.isLoggedIn()) {
            return true
        }

        // Só verifica Firebase se AuthManager não estiver logado
        // Para evitar inicialização desnecessária do Firebase
        return try {
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.currentUser != null
        } catch (e: Exception) {
            // Se Firebase não estiver configurado ou falhar, usar apenas AuthManager
            false
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
                // Envia query para ViewModel que atualiza o Flow
                viewModel.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun configurarFab() {
        binding.fabAdd.setOnClickListener {
            criarNovaLista()
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
                    println("DEBUG: HomeActivity - ESTADO COLETADO com ${state.allLists.size} listas totais, ${state.filteredLists.size} filtradas")
                    state.filteredLists.forEach { lista ->
                        println("DEBUG: Lista CHEGOU na UI: ${lista.id} - ${lista.titulo}")
                    }

                    // CORREÇÃO: Criar nova lista para forçar DiffUtil a detectar mudanças
                    val newList = state.filteredLists.toList()
                    adapter.submitList(newList)

                    println("DEBUG: HomeActivity - ADAPTER ATUALIZADO com ${newList.size} listas")

                    // Atualizar empty state
                    atualizarEmptyState(state.filteredLists.isEmpty())
                }
            }
        }
    }

    private fun atualizarEmptyState(isEmpty: Boolean) {
        println("DEBUG: HomeActivity - Atualizando empty state: isEmpty=$isEmpty")
        binding.txtEmptyState.isVisible = isEmpty
        binding.recycler.isVisible = !isEmpty

        if (isEmpty) {
            binding.txtEmptyState.text = "Nenhuma lista ainda.\nToque no + para criar sua primeira lista!"
        }
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
        // Usar ViewModel que atualiza o Flow automaticamente
        viewModel.deleteList(lista.id)

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
        // Logout do AuthManager primeiro (dados em memória)
        AuthManager.signOut()

        // Logout do Firebase Auth apenas se necessário
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Ignorar se Firebase não estiver configurado
        }

        // Redirecionar para LoginActivity com flags para limpar pilha
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // onResume removido - com MVVM e Flow, as atualizações são automáticas
}
