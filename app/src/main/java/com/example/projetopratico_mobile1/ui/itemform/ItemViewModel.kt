package com.example.projetopratico_mobile1.ui.itemform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.repo.ItemRepository

/**
 * UI state para tela de itens
 */
data class ItemUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel para gerenciar itens de uma lista específica
 */
class ItemViewModel(
    private val repository: ItemRepository,
    private val listId: String
) : ViewModel() {

    private val _uiState = repository.observeItems(listId)
        .map { items ->
            ItemUiState(
                items = items,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ItemUiState(isLoading = true)
        )

    val uiState: StateFlow<ItemUiState> = _uiState

    /**
     * Adiciona item à lista
     */
    fun addItem(item: Item) {
        viewModelScope.launch {
            repository.addItem(listId, item)
        }
    }

    /**
     * Atualiza um item
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(listId, item)
        }
    }

    /**
     * Remove um item
     */
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            repository.removeItem(listId, itemId)
        }
    }

    /**
     * Alterna status de comprado
     */
    fun togglePurchased(itemId: String, purchased: Boolean) {
        viewModelScope.launch {
            repository.togglePurchased(listId, itemId, purchased)
        }
    }
}
