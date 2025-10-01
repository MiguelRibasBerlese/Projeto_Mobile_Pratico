package com.example.projetopratico_mobile1.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    private val adapter = ListaComprasAdapter { lista -> abrirDetalhe(lista) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.adapter = adapter
        atualizarLista()
        // FAB para criar lista virá no C6
    }

    override fun onResume() {
        super.onResume()
        atualizarLista()
    }

    private fun atualizarLista() {
        val ordenadas = InMemoryStore.listas.sortedBy { it.titulo.lowercase() }
        adapter.submitList(ordenadas)
    }

    private fun abrirDetalhe(lista: ShoppingList) {
        val i = Intent(this, ListDetailActivity::class.java)
        i.putExtra("listaId", lista.id)
        startActivity(i)
    }
}
