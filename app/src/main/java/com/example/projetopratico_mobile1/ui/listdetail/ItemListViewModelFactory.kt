package com.example.projetopratico_mobile1.ui.listdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projetopratico_mobile1.data.repo.ItemRepository

/**
 * Factory para criar ItemListViewModel com dependências
 * Necessário porque o ViewModel tem construtor com parâmetros
 */
class ItemListViewModelFactory(
    private val repository: ItemRepository,
    private val listId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemListViewModel::class.java)) {
            return ItemListViewModel(repository, listId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
