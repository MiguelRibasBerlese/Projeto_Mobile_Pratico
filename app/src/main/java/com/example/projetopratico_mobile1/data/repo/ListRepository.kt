package com.example.projetopratico_mobile1.data.repo

import kotlinx.coroutines.flow.Flow
import com.example.projetopratico_mobile1.data.models.ShoppingList

/**
 * Contrato para repositório de listas de compras
 */
interface ListRepository {

    /**
     * Observa todas as listas do usuário
     */
    fun observeLists(): Flow<List<ShoppingList>>

    /**
     * Cria uma nova lista
     */
    suspend fun create(title: String, imageUri: String? = null): ShoppingList

    /**
     * Atualiza uma lista existente
     */
    suspend fun update(list: ShoppingList)

    /**
     * Remove uma lista
     */
    suspend fun delete(listId: String)

    /**
     * Busca lista por ID
     */
    suspend fun getById(listId: String): ShoppingList?
}
