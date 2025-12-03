package com.example.projetopratico_mobile1.data

import com.example.projetopratico_mobile1.data.models.User
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria

/**
 * Guarda todos os dados do app na memória
 * Como é um trabalho simples, não precisa de banco de dados
 */
object InMemoryStore {
    val users = mutableListOf<User>()

    // CORREÇÃO: Listas agora são organizadas por usuário (userId -> listas)
    private val userLists = mutableMapOf<String, MutableList<ShoppingList>>()
    var currentUser: User? = null

    // CORREÇÃO: Retorna apenas as listas do usuário atual
    val listasView: List<ShoppingList> get() {
        val currentUserId = currentUser?.id
        if (currentUserId == null) {
            println("DEBUG: InMemoryStore.listasView - Nenhum usuário logado, retornando lista vazia")
            return emptyList()
        }

        val userSpecificLists = userLists[currentUserId] ?: mutableListOf()
        val sortedListas = userSpecificLists.sortedBy { it.titulo }

        println("DEBUG: InMemoryStore.listasView - Usuário: $currentUserId")
        println("DEBUG: InMemoryStore.listasView - Listas do usuário: ${userSpecificLists.size}, ordenadas: ${sortedListas.size}")
        sortedListas.forEach { l -> println("  LISTA DO USUÁRIO: ${l.id} - ${l.titulo}") }

        return sortedListas
    }

    // CORREÇÃO: Compatibilidade - retorna listas do usuário atual
    val listas: MutableList<ShoppingList> get() {
        val currentUserId = currentUser?.id ?: return mutableListOf()
        return userLists.getOrPut(currentUserId) { mutableListOf() }
    }

    // CORREÇÃO: adiciona nova lista apenas para o usuário atual
    fun adicionarLista(lista: ShoppingList) {
        val currentUserId = currentUser?.id
        if (currentUserId == null) {
            println("DEBUG: InMemoryStore - ERRO: Nenhum usuário logado para adicionar lista")
            return
        }

        val userSpecificLists = userLists.getOrPut(currentUserId) { mutableListOf() }
        userSpecificLists.add(lista)

        println("DEBUG: InMemoryStore - Lista adicionada para usuário $currentUserId: ${lista.id} - ${lista.titulo}")
        println("DEBUG: InMemoryStore - Total de listas do usuário: ${userSpecificLists.size}")
        userSpecificLists.forEach { l ->
            println("DEBUG: Lista do usuário $currentUserId: ${l.id} - ${l.titulo}")
        }
    }

    // CORREÇÃO: busca lista apenas nas listas do usuário atual
    fun buscarLista(id: String): ShoppingList? {
        val currentUserId = currentUser?.id
        if (currentUserId == null) {
            println("DEBUG: InMemoryStore - ERRO: Nenhum usuário logado para buscar lista")
            return null
        }

        return userLists[currentUserId]?.find { it.id == id }
    }

    // CORREÇÃO: remove lista apenas das listas do usuário atual
    fun removerLista(id: String) {
        val currentUserId = currentUser?.id
        if (currentUserId == null) {
            println("DEBUG: InMemoryStore - ERRO: Nenhum usuário logado para remover lista")
            return
        }

        val userSpecificLists = userLists[currentUserId]
        if (userSpecificLists != null) {
            val removedCount = userSpecificLists.removeAll { it.id == id }
            println("DEBUG: InMemoryStore - Lista removida do usuário $currentUserId: $id, removidas: $removedCount")
            println("DEBUG: InMemoryStore - Total de listas do usuário após remoção: ${userSpecificLists.size}")
        }
    }

    // CORREÇÃO: atualiza lista apenas nas listas do usuário atual
    fun atualizarLista(lista: ShoppingList) {
        val currentUserId = currentUser?.id
        if (currentUserId == null) {
            println("DEBUG: InMemoryStore - ERRO: Nenhum usuário logado para atualizar lista")
            return
        }

        val userSpecificLists = userLists[currentUserId]
        if (userSpecificLists != null) {
            val index = userSpecificLists.indexOfFirst { it.id == lista.id }
            if (index != -1) {
                userSpecificLists[index] = lista
                println("DEBUG: InMemoryStore - Lista atualizada para usuário $currentUserId: ${lista.titulo}")
            }
        }
    }

    // CORREÇÃO: Não criar dados de exemplo automaticamente
    // Cada usuário inicia com lista vazia
    fun criarDadosExemplo() {
        val currentUserId = currentUser?.id
        if (currentUserId == null) {
            println("DEBUG: InMemoryStore - criarDadosExemplo: Nenhum usuário logado, não criando dados")
            return
        }

        println("DEBUG: InMemoryStore - criarDadosExemplo: Usuário $currentUserId inicia sem listas padrão")
        println("DEBUG: InMemoryStore - CORREÇÃO: Listas padrão removidas - cada usuário inicia com lista vazia")

        // Garantir que o usuário tem uma entrada no mapa, mas vazia
        userLists.getOrPut(currentUserId) { mutableListOf() }
    }

    // Método opcional para criar dados de exemplo (apenas se explicitamente solicitado)
    fun criarDadosExemploForDemo(userId: String? = null) {
        val targetUserId = userId ?: currentUser?.id
        if (targetUserId == null) {
            println("DEBUG: InMemoryStore - criarDadosExemploForDemo: Nenhum usuário especificado")
            return
        }

        val userSpecificLists = userLists.getOrPut(targetUserId) { mutableListOf() }

        if (userSpecificLists.isEmpty()) {
            println("DEBUG: InMemoryStore - Criando dados DEMO para usuário $targetUserId")

            val lista1 = ShoppingList(
                id = "demo_lista1_$targetUserId",
                titulo = "Lista Demo - Supermercado",
                imagemUri = null,
                itens = mutableListOf(
                    Item("item1", "Arroz", 2.0, "kg", Categoria.ALIMENTOS, false),
                    Item("item2", "Feijão", 1.0, "kg", Categoria.ALIMENTOS, false)
                )
            )

            userSpecificLists.add(lista1)
            println("DEBUG: InMemoryStore - Lista demo criada para usuário $targetUserId")
        }
    }
}
