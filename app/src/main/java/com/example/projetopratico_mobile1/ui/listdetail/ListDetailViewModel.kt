package com.example.projetopratico_mobile1.ui.listdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.ShoppingList

/**
 * ViewModel para gerenciar dados da ListDetailActivity
 */
class ListDetailViewModel : ViewModel() {

    private val _shoppingList = MutableLiveData<ShoppingList>()
    val shoppingList: LiveData<ShoppingList> = _shoppingList

    private val _items = MutableLiveData<List<Any>>() // Lista mista de headers (String) e itens (Item)
    val items: LiveData<List<Any>> = _items

    // TODO: Implementar carregamento da lista específica do InMemoryStore
    // TODO: Implementar métodos para adicionar/remover/editar itens
    // TODO: Implementar toggle de item comprado
    // TODO: Implementar agrupamento por categoria (headers)

    fun loadShoppingList(listId: String) {
        // TODO: Carregar lista específica do InMemoryStore
        // TODO: Organizar itens com headers por categoria
    }

    fun toggleItemComprado(item: Item) {
        // TODO: Atualizar estado do item no InMemoryStore
        // TODO: Atualizar LiveData
    }

    fun addItem(item: Item) {
        // TODO: Adicionar item à lista no InMemoryStore
        // TODO: Atualizar LiveData
    }
}
