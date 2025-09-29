package com.example.projetopratico_mobile1.data.models

/**
 * Modelo de dados para uma lista de compras
 */
data class ShoppingList(
    val id: String,
    val titulo: String,
    val imagemUri: String? = null,
    val itens: MutableList<Item> = mutableListOf()
)
