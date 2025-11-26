package com.example.projetopratico_mobile1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.repo.ListRepository

/**
 * UI state para a tela de listas
 */
data class ListUiState(
    val allLists: List<ShoppingList> = emptyList(),
    val filteredLists: List<ShoppingList> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel para gerenciar dados da HomeActivity usando MVVM
 */
class ListViewModel(
    private val repository: ListRepository
) : ViewModel() {

    // Estado da busca controlado internamente
    private val _query = MutableStateFlow("")

    // Combina listas observadas com query para filtrar
    private val _uiState = combine(
        repository.observeLists(),
        _query
    ) { lists, query ->
        val filtered = if (query.isEmpty()) {
            lists
        } else {
            lists.filter {
                it.titulo.contains(query, ignoreCase = true)
            }
        }

        ListUiState(
            allLists = lists,
            filteredLists = filtered,
            query = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState()
    )

    val uiState: StateFlow<ListUiState> = _uiState

    /**
     * Atualiza a query de busca
     */
    fun setQuery(query: String) {
        _query.value = query
    }

    /**
     * Cria nova lista
     */
    fun createList(title: String, imageUri: String? = null) {
        viewModelScope.launch {
            repository.create(title, imageUri)
        }
    }

    /**
     * Remove uma lista
     */
    fun deleteList(listId: String) {
        viewModelScope.launch {
            repository.delete(listId)
        }
    }
}
