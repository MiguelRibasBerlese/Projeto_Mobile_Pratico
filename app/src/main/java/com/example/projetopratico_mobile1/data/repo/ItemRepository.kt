package com.example.projetopratico_mobile1.data.repo

import kotlinx.coroutines.flow.Flow
import com.example.projetopratico_mobile1.data.models.Item

/**
 * Contrato para repositório de itens de lista
 */
interface ItemRepository {

    /**
     * Observa todos os itens de uma lista específica
     */
    fun observeItems(listId: String): Flow<List<Item>>

    /**
     * Adiciona um item à lista
     */
    suspend fun addItem(listId: String, item: Item)

    /**
     * Atualiza um item existente
     */
    suspend fun updateItem(listId: String, item: Item)

    /**
     * Remove um item
     */
    suspend fun removeItem(listId: String, itemId: String)

    /**
     * Alterna status de comprado do item
     */
    suspend fun togglePurchased(listId: String, itemId: String, purchased: Boolean)
}
