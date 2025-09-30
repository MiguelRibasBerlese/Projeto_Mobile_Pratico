package com.example.projetopratico_mobile1.ui.itemform

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.databinding.ActivityItemFormBinding
import com.example.projetopratico_mobile1.util.Validators
import com.example.projetopratico_mobile1.util.showToast
import java.util.UUID

/**
 * Tela para adicionar ou editar um item da lista
 */
class ItemFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemFormBinding
    private var listaId: String? = null
    private var itemId: String? = null // null = novo item, preenchido = editando

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pegarDadosIntent()
        configurarSpinner()
        configurarBotaoSalvar()

        // TODO: se for edição, preencher campos com dados do item
    }

    private fun pegarDadosIntent() {
        listaId = intent.getStringExtra("LISTA_ID")
        itemId = intent.getStringExtra("ITEM_ID") // pode ser null se for novo item
    }

    private fun configurarSpinner() {
        // cria lista com nomes das categorias para o spinner
        val categorias = Categoria.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.seletorCategoria.adapter = adapter
    }

    private fun configurarBotaoSalvar() {
        binding.btnSalvar.setOnClickListener {
            tentarSalvarItem()
        }
    }

    private fun tentarSalvarItem() {
        val nome = binding.campoNome.text.toString()
        val quantidadeText = binding.campoQuantidade.text.toString()
        val unidade = binding.campoUnidade.text.toString()
        val categoriaSelecionada = binding.seletorCategoria.selectedItem as String

        // valida os campos antes de salvar
        if (!validarCampos(nome, quantidadeText, unidade)) {
            return
        }

        val quantidade = quantidadeText.toDouble()
        val categoria = Categoria.valueOf(categoriaSelecionada)

        // cria o item
        val item = Item(
            id = UUID.randomUUID().toString(),
            nome = nome,
            quantidade = quantidade,
            unidade = unidade,
            categoria = categoria
        )

        // adiciona o item na lista
        val lista = InMemoryStore.listas.find { it.id == listaId }
        if (lista != null) {
            lista.itens.add(item)
            binding.root.showToast(getString(R.string.item_salvo))
            finish() // volta para a tela anterior
        } else {
            binding.root.showToast("Erro: lista não encontrada")
        }
    }

    private fun validarCampos(nome: String, quantidade: String, unidade: String): Boolean {
        // verifica se os campos obrigatórios foram preenchidos
        if (!Validators.notBlank(nome, quantidade, unidade)) {
            binding.root.showToast(getString(R.string.erro_campo_vazio))
            return false
        }

        // verifica se a quantidade é um número válido
        try {
            val qtde = quantidade.toDouble()
            if (qtde <= 0) {
                binding.root.showToast("Quantidade deve ser maior que zero")
                return false
            }
        } catch (e: NumberFormatException) {
            binding.root.showToast("Quantidade inválida")
            return false
        }

        return true
    }
}
