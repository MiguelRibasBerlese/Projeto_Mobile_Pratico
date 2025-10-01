package com.example.projetopratico_mobile1.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetopratico_mobile1.R
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.databinding.RowListaBinding

/**
 * Adapter para exibir listas de compras no RecyclerView
 */
class ListaComprasAdapter(
    private val onClick: (ShoppingList) -> Unit
) : ListAdapter<ShoppingList, ListaComprasAdapter.VH>(DIFF) {

    inner class VH(val binding: RowListaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = RowListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(inf)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.txtTitulo.text = item.titulo

        if (item.imagemUri != null) {
            holder.binding.imgThumb.setImageURI(Uri.parse(item.imagemUri))
        } else {
            holder.binding.imgThumb.setImageResource(R.drawable.ic_placeholder)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ShoppingList>() {
            override fun areItemsTheSame(a: ShoppingList, b: ShoppingList) = a.id == b.id
            override fun areContentsTheSame(a: ShoppingList, b: ShoppingList) = a == b
        }
    }
}
