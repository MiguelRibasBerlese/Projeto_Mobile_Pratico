package com.example.projetopratico_mobile1.data.models

/**
 * Modelo de dados para um item da lista de compras
 */
data class Item(
    val id: String,
    val nome: String,
    val quantidade: Double,
    val unidade: String,
    val categoria: Categoria,
    val comprado: Boolean = false
)
