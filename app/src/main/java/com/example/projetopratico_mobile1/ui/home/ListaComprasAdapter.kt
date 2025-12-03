package com.example.projetopratico_mobile1.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.RowListaBinding
import com.example.projetopratico_mobile1.util.LocalImageStore

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
            println("DEBUG: ListaComprasAdapter - BINDING lista: ${lista.id} - ${lista.titulo}")
            binding.txtTitulo.text = lista.titulo
            binding.txtQuantidadeItens.text = "${lista.itens.size} itens"

            // Configurar imagem da lista usando LocalImageStore
            println("DEBUG: ListaComprasAdapter - Verificando imagem para lista ${lista.id}")
            println("DEBUG: ListaComprasAdapter - imagemUri da lista: '${lista.imagemUri}'")

            if (LocalImageStore.exists(binding.root.context, lista.id)) {
                // Imagem existe localmente - carregar do arquivo
                println("DEBUG: ListaComprasAdapter - Imagem encontrada no LocalImageStore")
                val imageFile = LocalImageStore.fileForList(binding.root.context, lista.id)
                println("DEBUG: ListaComprasAdapter - Carregando imagem de: ${imageFile.absolutePath}")
                binding.imgLista.setImageURI(Uri.fromFile(imageFile))
                binding.imgLista.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                // Sem imagem - usar placeholder
                println("DEBUG: ListaComprasAdapter - Imagem NÃO encontrada no LocalImageStore, usando placeholder")
                binding.imgLista.setImageResource(android.R.drawable.ic_menu_gallery)
                binding.imgLista.scaleType = ImageView.ScaleType.CENTER_CROP
            }

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
            val result = oldItem.id == newItem.id
            println("DEBUG: ListaComprasAdapter - areItemsTheSame: ${oldItem.id} == ${newItem.id} = $result")
            return result
        }

        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            val result = oldItem.titulo == newItem.titulo && oldItem.itens.size == newItem.itens.size
            println("DEBUG: ListaComprasAdapter - areContentsTheSame: ${oldItem.titulo} = $result")
            return result
        }
    }
}
