package com.example.projetopratico_mobile1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projetopratico_mobile1.data.models.ShoppingList

/**
 * ViewModel para gerenciar dados da HomeActivity
 */
class HomeViewModel : ViewModel() {

    private val _shoppingLists = MutableLiveData<List<ShoppingList>>()
    val shoppingLists: LiveData<List<ShoppingList>> = _shoppingLists

    // TODO: Implementar carregamento das listas do InMemoryStore
    // TODO: Implementar métodos para adicionar/remover listas
    // TODO: Implementar filtragem/busca se necessário

    fun loadShoppingLists() {
        // TODO: Carregar listas do usuário atual do InMemoryStore
    }

    fun addShoppingList(lista: ShoppingList) {
        // TODO: Adicionar nova lista ao InMemoryStore
        // TODO: Atualizar LiveData
    }
}
