package com.example.projetopratico_mobile1.ui.listdetail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.ActivityListDetailBinding
import com.example.projetopratico_mobile1.ui.itemform.ItemFormActivity
import com.example.projetopratico_mobile1.util.showToast

/**
 * Tela que mostra os itens de uma lista específica
 */
class ListDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListDetailBinding
    private lateinit var adapter: ItensAdapter
    private var listaAtual: ShoppingList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarLista()
        configurarRecyclerView()
        configurarFab()
        atualizarItens()
    }

    private fun carregarLista() {
        val listaId = intent.getStringExtra("LISTA_ID")
        if (listaId != null) {
            listaAtual = InMemoryStore.buscarLista(listaId)
            // define o título da tela
            title = listaAtual?.titulo ?: "Lista"
        }
    }

    private fun configurarRecyclerView() {
        adapter = ItensAdapter(
            aoClicarItem = { item ->
                // TODO: depois implementar edição do item
                binding.root.showToast("Clicou no item: ${item.nome}")
            },
            aoMarcarItem = { item, marcado ->
                marcarItemComprado(item, marcado)
            }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun configurarFab() {
        binding.fabAddItem.setOnClickListener {
            abrirFormularioItem()
        }
    }

    private fun atualizarItens() {
        val lista = listaAtual
        if (lista == null) {
            binding.root.showToast("Erro: lista não encontrada")
            finish()
            return
        }

        // organiza os itens por categoria para mostrar com headers
        val itensOrganizados = organizarItensPorCategoria(lista.itens)
        adapter.submitList(itensOrganizados)

        if (lista.itens.isEmpty()) {
            binding.root.showToast("Lista vazia. Toque no + para adicionar itens!")
        }
    }

    private fun organizarItensPorCategoria(itens: List<Item>): List<Any> {
        val resultado = mutableListOf<Any>()

        // agrupa itens por categoria
        val itensPorCategoria = itens.groupBy { it.categoria }

        // para cada categoria, adiciona header + itens
        itensPorCategoria.forEach { (categoria, itensCategoria) ->
            resultado.add(categoria.name) // header
            resultado.addAll(itensCategoria) // itens da categoria
        }

        return resultado
    }

    private fun marcarItemComprado(item: Item, marcado: Boolean) {
        // TODO: implementar mudança do estado do item
        // por enquanto só mostra feedback
        val status = if (marcado) "comprado" else "não comprado"
        binding.root.showToast("${item.nome} marcado como $status")
    }

    private fun abrirFormularioItem() {
        val intent = Intent(this, ItemFormActivity::class.java)
        intent.putExtra("LISTA_ID", listaAtual?.id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // recarrega a lista quando volta do formulário
        carregarLista()
        atualizarItens()
    }
}
