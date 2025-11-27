package com.example.projetopratico_mobile1.ui.listdetail

import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.data.models.Item

/**
 * Utilitário para converter dados agrupados em lista de RowItems para o adapter
 */
object AdapterDataConverter {

    /**
     * Converte dados agrupados em lista linear de RowItems
     * Ordem: categorias não compradas (com headers) + seção comprados
     */
    fun convertToRowItems(groupedItems: GroupedItems): List<RowItem> {
        val result = mutableListOf<RowItem>()

        // Adiciona seções de não comprados agrupadas por categoria
        groupedItems.byCategory.forEach { (categoria, items) ->
            if (items.isNotEmpty()) {
                // Header da categoria
                result.add(RowItem.Header(categoria))
                // Itens da categoria
                items.forEach { item ->
                    result.add(RowItem.Produto(item))
                }
            }
        }

        // Adiciona seção de comprados (se houver)
        if (groupedItems.purchased.isNotEmpty()) {
            result.add(RowItem.Header(Categoria.COMPRADOS))
            groupedItems.purchased.forEach { item ->
                result.add(RowItem.Produto(item))
            }
        }

        return result
    }
}
