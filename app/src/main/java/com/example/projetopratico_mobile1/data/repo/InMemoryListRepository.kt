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

    // StateFlow SIMPLES para observar mudanças nas listas
    private val _listsFlow = MutableStateFlow<List<ShoppingList>>(emptyList())

    init {
        println("DEBUG: InMemoryListRepository - Inicializando repositório SINGLETON")
        updateFlow()
    }

    private fun updateFlow() {
        // SIMPLIFICADO: Sempre pegar direto do InMemoryStore sem ordenação
        val allLists = InMemoryStore.listas.toList() // Cópia simples da lista
        println("DEBUG: InMemoryListRepository - updateFlow() com ${allLists.size} listas")
        allLists.forEach { lista ->
            println("DEBUG: updateFlow - Lista: ${lista.id} - ${lista.titulo}")
        }
        _listsFlow.value = allLists
    }

    override fun observeLists(): Flow<List<ShoppingList>> {
        println("DEBUG: InMemoryListRepository - observeLists() chamado")
        updateFlow() // Sempre atualizar antes de retornar
        return _listsFlow
    }

    override suspend fun create(title: String, imageUri: String?): ShoppingList {
        val newList = ShoppingList(
            id = UUID.randomUUID().toString(),
            titulo = title,
            imagemUri = imageUri,
            itens = mutableListOf()
        )

        println("DEBUG: InMemoryListRepository - create() iniciado: ${newList.titulo}")
        println("DEBUG: InMemoryListRepository - imageUri fornecida: '$imageUri'")

        // CORREÇÃO: Salvar imagem no LocalImageStore se imageUri foi fornecida
        if (!imageUri.isNullOrBlank()) {
            try {
                // Não temos contexto aqui, então vamos fazer isso no ListFormActivity
                println("DEBUG: InMemoryListRepository - Imagem será processada no Activity")
            } catch (e: Exception) {
                println("DEBUG: InMemoryListRepository - Erro ao salvar imagem: ${e.message}")
            }
        }

        // Adicionar ao store
        InMemoryStore.adicionarLista(newList)

        // Atualizar Flow IMEDIATAMENTE
        updateFlow()

        println("DEBUG: InMemoryListRepository - create() finalizado, Flow atualizado")
        return newList
    }

    override suspend fun update(list: ShoppingList) {
        println("DEBUG: InMemoryListRepository - update() iniciado: ${list.titulo}")
        InMemoryStore.atualizarLista(list)
        updateFlow()
        println("DEBUG: InMemoryListRepository - update() finalizado")
    }

    override suspend fun delete(listId: String) {
        println("DEBUG: InMemoryListRepository - delete() iniciado: $listId")
        InMemoryStore.removerLista(listId)
        updateFlow()
        println("DEBUG: InMemoryListRepository - delete() finalizado")
    }

    override suspend fun getById(listId: String): ShoppingList? {
        return InMemoryStore.buscarLista(listId)
    }

    // Método de debug para identificar instância
    fun getInstanceId(): String {
        return "InMemoryListRepository@${hashCode()}"
    }
}

