package com.example.projetopratico_mobile1.data.models

/**
 * Enum das categorias disponíveis para os itens
 */
enum class Categoria(val nome: String) {
    ALIMENTOS("Alimentos"),
    BEBIDAS("Bebidas"),
    HIGIENE("Higiene"),
    LIMPEZA("Limpeza"),
    OUTROS("Outros"),
    COMPRADOS("Comprados") // categoria especial para seção de comprados
}
