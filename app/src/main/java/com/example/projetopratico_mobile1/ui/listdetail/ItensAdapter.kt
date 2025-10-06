package com.example.projetopratico_mobile1.ui.listdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.databinding.RowItemBinding
import com.example.projetopratico_mobile1.databinding.RowItemHeaderBinding

// sealed class para representar diferentes tipos de rows
sealed class RowItem {
    data class Header(val categoria: Categoria) : RowItem()
    data class Produto(val item: Item) : RowItem()
}

/**
 * Adapter para mostrar itens com headers de categoria
 * Usa sealed class para tipos de row bem definidos
 */
class ItensAdapter(
    private val aoClicarItem: (Item) -> Unit,
    private val aoToggleComprado: (Item) -> Unit
) : ListAdapter<RowItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val TIPO_HEADER = 0
        private const val TIPO_ITEM = 1

        // DiffUtil para o RecyclerView funcionar direito
        object DiffCallback : DiffUtil.ItemCallback<RowItem>() {
            override fun areItemsTheSame(oldItem: RowItem, newItem: RowItem): Boolean {
                return when {
                    oldItem is RowItem.Header && newItem is RowItem.Header ->
                        oldItem.categoria == newItem.categoria
                    oldItem is RowItem.Produto && newItem is RowItem.Produto ->
                        oldItem.item.id == newItem.item.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: RowItem, newItem: RowItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RowItem.Header -> TIPO_HEADER
            is RowItem.Produto -> TIPO_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TIPO_HEADER -> {
                val binding = RowItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            TIPO_ITEM -> {
                val binding = RowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("ViewType desconhecido: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is RowItem.Header -> (holder as HeaderViewHolder).bind(item.categoria)
            is RowItem.Produto -> (holder as ItemViewHolder).bind(item.item)
        }
    }

    // ViewHolder para os headers de categoria
    inner class HeaderViewHolder(
        private val binding: RowItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoria: Categoria) {
            binding.txtHeader.text = categoria.nome
        }
    }

    // ViewHolder para os itens
    inner class ItemViewHolder(
        private val binding: RowItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            // mostra nome, quantidade e unidade
            val unidadeText = if (item.unidade.isBlank()) "–" else item.unidade
            binding.txtNome.text = "${item.nome} - ${item.quantidade} $unidadeText"
            binding.txtCategoria.text = item.categoria.nome
            binding.chkComprado.isChecked = item.comprado

            // mapeia categoria para ícone
            val iconeRes = when (item.categoria) {
                Categoria.ALIMENTOS -> android.R.drawable.ic_menu_myplaces
                Categoria.BEBIDAS -> android.R.drawable.ic_dialog_email
                Categoria.HIGIENE -> android.R.drawable.ic_menu_preferences
                Categoria.LIMPEZA -> android.R.drawable.ic_menu_crop
                Categoria.OUTROS -> android.R.drawable.ic_menu_info_details
                Categoria.COMPRADOS -> android.R.drawable.ic_menu_agenda
            }
            binding.imgCat.setImageResource(iconeRes)

            // configura os cliques
            binding.root.setOnClickListener {
                aoClicarItem(item)
            }

            binding.chkComprado.setOnCheckedChangeListener { _, _ ->
                aoToggleComprado(item)
            }
        }
    }
}
