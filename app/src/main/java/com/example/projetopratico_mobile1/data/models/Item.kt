package com.example.projetopratico_mobile1.data.models

import java.util.UUID

/**
 * Modelo de dados para um item da lista de compras
 */
data class Item(
    val id: String = UUID.randomUUID().toString(),
    var nome: String,
    var quantidade: Double = 1.0,
    var unidade: String = "",
    var categoria: Categoria = Categoria.OUTROS,
    var comprado: Boolean = false
)
