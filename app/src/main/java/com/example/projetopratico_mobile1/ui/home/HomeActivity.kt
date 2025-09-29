package com.example.projetopratico_mobile1.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.ActivityHomeBinding
import com.example.projetopratico_mobile1.ui.listdetail.ListDetailActivity
import com.example.projetopratico_mobile1.util.showToast
import java.util.UUID

/**
 * Tela principal que mostra as listas de compras do usuário
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ListaComprasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarFab()
        carregarListas()
    }

    private fun configurarRecyclerView() {
        adapter = ListaComprasAdapter { lista ->
            abrirDetalhesLista(lista)
        }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun configurarFab() {
        binding.fabAdd.setOnClickListener {
            criarNovaLista()
        }
    }

    private fun carregarListas() {
        val listas = InMemoryStore.getListas()
        adapter.submitList(listas)

        // se não tem listas, mostra uma dica
        if (listas.isEmpty()) {
            binding.root.showToast("Toque no + para criar sua primeira lista!")
        }
    }

    private fun criarNovaLista() {
        // TODO: depois fazer um dialog para pedir o nome da lista
        // por enquanto cria com nome padrão
        val novaLista = ShoppingList(
            id = UUID.randomUUID().toString(),
            titulo = "Lista ${System.currentTimeMillis()}" // nome único baseado no tempo
        )

        InMemoryStore.adicionarLista(novaLista)
        binding.root.showToast("Lista criada!")
        carregarListas() // atualiza a lista
    }

    private fun abrirDetalhesLista(lista: ShoppingList) {
        val intent = Intent(this, ListDetailActivity::class.java)
        intent.putExtra("LISTA_ID", lista.id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // atualiza a lista quando volta de outras telas
        carregarListas()
    }
}
