package com.example.projetopratico_mobile1.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.Item

/**
 * Implementação InMemory do repositório de itens
 * Mantém flows por listId para observar mudanças nos itens
 */
class InMemoryItemRepository : ItemRepository {

    // Map para manter um flow por lista
    private val _itemFlowsByList = mutableMapOf<String, MutableStateFlow<List<Item>>>()

    /**
     * Obtém ou cria o flow para uma lista específica
     */
    private fun getOrCreateFlow(listId: String): MutableStateFlow<List<Item>> {
        return _itemFlowsByList.getOrPut(listId) {
            val lista = InMemoryStore.buscarLista(listId)
            MutableStateFlow(lista?.itens?.toList() ?: emptyList())
        }
    }

    override fun observeItems(listId: String): Flow<List<Item>> {
        return getOrCreateFlow(listId).asStateFlow()
    }

    override suspend fun addItem(listId: String, item: Item) {
        val lista = InMemoryStore.buscarLista(listId) ?: return
        lista.itens.add(item)

        // Atualiza o flow específico desta lista
        getOrCreateFlow(listId).value = lista.itens.toList()
    }

    override suspend fun updateItem(listId: String, item: Item) {
        val lista = InMemoryStore.buscarLista(listId) ?: return
        val index = lista.itens.indexOfFirst { it.id == item.id }
        if (index != -1) {
            lista.itens[index] = item
            // Atualiza o flow
            getOrCreateFlow(listId).value = lista.itens.toList()
        }
    }

    override suspend fun removeItem(listId: String, itemId: String) {
        val lista = InMemoryStore.buscarLista(listId) ?: return
        lista.itens.removeAll { it.id == itemId }

        // Atualiza o flow
        getOrCreateFlow(listId).value = lista.itens.toList()
    }

    override suspend fun togglePurchased(listId: String, itemId: String, purchased: Boolean) {
        val lista = InMemoryStore.buscarLista(listId) ?: return
        val index = lista.itens.indexOfFirst { it.id == itemId }
        if (index != -1) {
            lista.itens[index] = lista.itens[index].copy(comprado = purchased)
            // Atualiza o flow
            getOrCreateFlow(listId).value = lista.itens.toList()
        }
    }
}
