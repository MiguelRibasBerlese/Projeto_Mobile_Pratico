package com.example.projetopratico_mobile1.ui.listdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.databinding.RowItemBinding
import com.example.projetopratico_mobile1.databinding.RowItemHeaderBinding

/**
 * Adapter para mostrar itens com headers de categoria
 * Tem 2 tipos: header (String) e item (Item)
 */
class ItensAdapter(
    private val aoClicarItem: (Item) -> Unit,
    private val aoMarcarItem: (Item, Boolean) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val TIPO_HEADER = 0
        private const val TIPO_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is String -> TIPO_HEADER
            is Item -> TIPO_ITEM
            else -> throw IllegalArgumentException("Tipo desconhecido")
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
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as String)
            is ItemViewHolder -> holder.bind(getItem(position) as Item)
        }
    }

    // ViewHolder para os headers de categoria
    inner class HeaderViewHolder(
        private val binding: RowItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(textoHeader: String) {
            binding.txtHeader.text = textoHeader
        }
    }

    // ViewHolder para os itens
    inner class ItemViewHolder(
        private val binding: RowItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            // mostra nome, quantidade e unidade
            binding.txtNome.text = "${item.nome} - ${item.quantidade} ${item.unidade}"
            binding.chkComprado.isChecked = item.comprado

            // TODO: colocar ícone da categoria na imgCat

            // configura os cliques
            binding.root.setOnClickListener {
                aoClicarItem(item)
            }

            binding.chkComprado.setOnCheckedChangeListener { _, marcado ->
                aoMarcarItem(item, marcado)
            }
        }
    }

    // DiffUtil básico para o RecyclerView funcionar direito
    companion object DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is String && newItem is String -> oldItem == newItem
                oldItem is Item && newItem is Item -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
