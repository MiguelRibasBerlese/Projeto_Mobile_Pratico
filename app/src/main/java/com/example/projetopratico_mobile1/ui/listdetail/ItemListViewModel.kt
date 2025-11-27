package com.example.projetopratico_mobile1.ui.listdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.data.repo.ItemRepository

/**
 * Estrutura para itens agrupados por categoria
 */
data class GroupedItems(
    val byCategory: Map<Categoria, List<Item>>,
    val purchased: List<Item>
)

/**
 * UI state para a tela de detalhes da lista com busca e agrupamento
 */
data class ItemListUiState(
    val allItems: List<Item> = emptyList(),
    val groupedItems: GroupedItems = GroupedItems(emptyMap(), emptyList()),
    val query: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel para gerenciar itens de uma lista específica com busca e agrupamento
 * Diferente do ItemViewModel do form - este é para exibir a lista com filtros
 */
class ItemListViewModel(
    private val repository: ItemRepository,
    private val listId: String
) : ViewModel() {

    // Estado da busca controlado internamente
    private val _query = MutableStateFlow("")

    // Combina itens observados com query para filtrar e agrupar
    private val _uiState = combine(
        repository.observeItems(listId),
        _query
    ) { items, query ->
        // Filtra por nome se tem query
        val filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.nome.contains(query, ignoreCase = true)
            }
        }

        // Separa comprados dos não comprados
        val (purchased, notPurchased) = filteredItems.partition { it.comprado }

        // Agrupa não comprados por categoria
        val groupedByCategory = notPurchased
            .groupBy { item ->
                // Se categoria for COMPRADOS ou vazia, usar OUTROS
                when {
                    item.categoria == Categoria.COMPRADOS -> Categoria.OUTROS
                    else -> item.categoria
                }
            }
            .toSortedMap(compareBy { it.nome }) // ordena categorias por nome

        ItemListUiState(
            allItems = items,
            groupedItems = GroupedItems(
                byCategory = groupedByCategory,
                purchased = purchased.sortedBy { it.nome }
            ),
            query = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemListUiState(isLoading = true)
    )

    val uiState: StateFlow<ItemListUiState> = _uiState

    /**
     * Atualiza a query de busca
     */
    fun setQuery(query: String) {
        _query.value = query
    }

    /**
     * Adiciona item à lista
     */
    fun addItem(item: Item) {
        viewModelScope.launch {
            repository.addItem(listId, item)
        }
    }

    /**
     * Toggle do status de comprado - atualiza em tempo real
     */
    fun togglePurchased(item: Item) {
        viewModelScope.launch {
            repository.togglePurchased(listId, item.id, !item.comprado)
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
     * Atualiza um item existente
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(listId, item)
        }
    }
}
