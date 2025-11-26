package com.example.projetopratico_mobile1.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.projetopratico_mobile1.data.InMemoryStore
import com.example.projetopratico_mobile1.data.models.ShoppingList
import java.util.UUID

/**
 * Implementação InMemory do repositório de listas
 * Delega para o InMemoryStore e mantém flows para observação
 */
class InMemoryListRepository : ListRepository {

    // StateFlow para observar mudanças nas listas
    private val _listsFlow = MutableStateFlow(InMemoryStore.listasView)

    init {
        // Sincroniza o flow inicial com o estado atual do store
        refreshFlow()
    }

    private fun refreshFlow() {
        _listsFlow.value = InMemoryStore.listasView
    }

    override fun observeLists(): Flow<List<ShoppingList>> {
        // Sempre atualiza o flow antes de retornar para garantir sincronia
        refreshFlow()
        return _listsFlow.asStateFlow()
    }

    override suspend fun create(title: String, imageUri: String?): ShoppingList {
        val newList = ShoppingList(
            id = UUID.randomUUID().toString(),
            titulo = title,
            imagemUri = imageUri,
            itens = mutableListOf()
        )

        InMemoryStore.adicionarLista(newList)
        // Atualiza o flow com a nova lista ordenada
        refreshFlow()

        return newList
    }

    override suspend fun update(list: ShoppingList) {
        InMemoryStore.atualizarLista(list)
        // Atualiza o flow
        refreshFlow()
    }

    override suspend fun delete(listId: String) {
        InMemoryStore.removerLista(listId)
        // Atualiza o flow
        refreshFlow()
    }

    override suspend fun getById(listId: String): ShoppingList? {
        return InMemoryStore.buscarLista(listId)
    }
}

