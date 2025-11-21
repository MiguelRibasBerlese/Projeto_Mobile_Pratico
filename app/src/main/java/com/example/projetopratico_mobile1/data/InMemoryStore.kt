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

    // Função para criar dados de exemplo para testes
    fun criarDadosExemplo() {
        if (listas.isEmpty()) {
            val lista1 = ShoppingList(
                id = "lista1",
                titulo = "Lista de Compras - Supermercado",
                imagemUri = null,
                itens = mutableListOf(
                    Item("item1", "Arroz", 2.0, "kg", Categoria.ALIMENTOS, false),
                    Item("item2", "Feijão", 1.0, "kg", Categoria.ALIMENTOS, false),
                    Item("item3", "Leite", 3.0, "Uni", Categoria.BEBIDAS, false),
                    Item("item4", "Sabonete", 2.0, "Uni", Categoria.HIGIENE, false)
                )
            )

            val lista2 = ShoppingList(
                id = "lista2",
                titulo = "Produtos de Limpeza",
                imagemUri = null,
                itens = mutableListOf(
                    Item("item5", "Detergente", 1.0, "Uni", Categoria.LIMPEZA, false),
                    Item("item6", "Desinfetante", 500.0, "g", Categoria.LIMPEZA, false)
                )
            )

            listas.addAll(listOf(lista1, lista2))
        }
    }
}
