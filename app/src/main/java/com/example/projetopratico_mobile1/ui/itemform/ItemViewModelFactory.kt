package com.example.projetopratico_mobile1.ui.itemform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projetopratico_mobile1.data.repo.ItemRepository

/**
 * Factory para criar ItemViewModel com dependências
 */
class ItemViewModelFactory(
    private val repository: ItemRepository,
    private val listId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            return ItemViewModel(repository, listId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
