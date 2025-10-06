package com.example.projetopratico_mobile1.util

import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.ui.listdetail.RowItem

/**
 * Funções úteis para agrupar itens por categoria
 */
object GroupingUtils {

    /**
     * Agrupa itens por categoria, ordena A-Z dentro de cada categoria
     * e monta lista combinada com headers + itens
     */
    fun buildGroupedData(itens: List<Item>): List<RowItem> {
        val result = mutableListOf<RowItem>()

        // agrupa por categoria e ordena
        val grouped = itens.groupBy { it.categoria }

        // processa na ordem do enum Categoria (exceto COMPRADOS)
        Categoria.values().filter { it != Categoria.COMPRADOS }.forEach { categoria ->
            val itensCategoria = grouped[categoria]?.sortedBy { it.nome }
            if (!itensCategoria.isNullOrEmpty()) {
                // adiciona header da categoria
                result.add(RowItem.Header(categoria))
                // adiciona itens ordenados
                itensCategoria.forEach { item ->
                    result.add(RowItem.Produto(item))
                }
            }
        }

        return result
    }

    /**
     * Separa itens não comprados e comprados com headers
     */
    fun buildDataWithComprados(itens: List<Item>): List<RowItem> {
        val result = mutableListOf<RowItem>()

        val naoComprados = itens.filter { !it.comprado }
        val comprados = itens.filter { it.comprado }

        // adiciona não comprados agrupados por categoria
        result.addAll(buildGroupedData(naoComprados))

        // se tem comprados, adiciona seção separada
        if (comprados.isNotEmpty()) {
            result.add(RowItem.Header(Categoria.COMPRADOS)) // categoria especial
            comprados.sortedBy { it.nome }.forEach { item ->
                result.add(RowItem.Produto(item))
            }
        }

        return result
    }
}
