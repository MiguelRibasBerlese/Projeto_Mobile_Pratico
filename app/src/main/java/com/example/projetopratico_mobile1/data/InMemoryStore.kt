package com.example.projetopratico_mobile1.data

import com.example.projetopratico_mobile1.data.models.User
import com.example.projetopratico_mobile1.data.models.ShoppingList

/**
 * Guarda todos os dados do app na memória
 * Como é um trabalho simples, não precisa de banco de dados
 */
object InMemoryStore {
    val users = mutableListOf<User>()
    val listas = mutableListOf<ShoppingList>()
    var currentUser: User? = null

    // Opcional: visão somente leitura ordenada
    val listasView: List<ShoppingList> get() = listas.sortedBy { it.titulo }

    // adiciona nova lista
    fun adicionarLista(lista: ShoppingList) {
        listas.add(lista)
    }

    // busca lista por ID
    fun buscarLista(id: String): ShoppingList? {
        return listas.find { it.id == id }
    }

    // remove lista e todos seus itens
    fun removerLista(id: String) {
        listas.removeAll { it.id == id }
    }

    // atualiza lista existente
    fun atualizarLista(lista: ShoppingList) {
        val index = listas.indexOfFirst { it.id == lista.id }
        if (index != -1) {
            listas[index] = lista
        }
    }
}
