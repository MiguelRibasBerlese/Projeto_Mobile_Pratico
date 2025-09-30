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
}
