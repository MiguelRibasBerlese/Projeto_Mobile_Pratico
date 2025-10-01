package com.example.projetopratico_mobile1.data.models

import java.util.UUID

/**
 * Modelo de dados para uma lista de compras
 */
data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    var titulo: String,
    var imagemUri: String? = null,
    val itens: MutableList<Item> = mutableListOf()
)
