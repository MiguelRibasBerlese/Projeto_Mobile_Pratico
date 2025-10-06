package com.example.projetopratico_mobile1.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.RowListaBinding

/**
 * Adapter para mostrar as listas de compras com ações
 */
class ListaComprasAdapter(
    private val aoClicarLista: (ShoppingList) -> Unit,
    private val aoEditarLista: (ShoppingList) -> Unit,
    private val aoExcluirLista: (ShoppingList) -> Unit
) : ListAdapter<ShoppingList, ListaComprasAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: RowListaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lista: ShoppingList) {
            binding.txtTitulo.text = lista.titulo
            binding.txtQuantidadeItens.text = "${lista.itens.size} itens"

            // configura cliques
            binding.root.setOnClickListener {
                aoClicarLista(lista)
            }

            binding.btnEditar.setOnClickListener {
                aoEditarLista(lista)
            }

            binding.btnExcluir.setOnClickListener {
                aoExcluirLista(lista)
            }
        }
    }

    // DiffUtil para o RecyclerView não ficar piscando
    companion object DiffCallback : DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            return oldItem == newItem
        }
    }
}
