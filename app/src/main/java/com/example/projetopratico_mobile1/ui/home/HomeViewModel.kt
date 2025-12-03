package com.example.projetopratico_mobile1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.repo.InMemoryListRepository
import com.example.projetopratico_mobile1.data.repo.ListRepository

/**
 * UI state para a tela de listas
 */
data class HomeUiState(
    val allLists: List<ShoppingList> = emptyList(),
    val filteredLists: List<ShoppingList> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel para gerenciar dados da HomeActivity usando MVVM
 */
class HomeViewModel(
    private val repository: ListRepository
) : ViewModel() {

    init {
        if (repository is InMemoryListRepository) {
            println("DEBUG: HomeViewModel - Usando repositório: ${repository.getInstanceId()}")
        }
    }

    // Estado da busca controlado internamente
    private val _query = MutableStateFlow("")

    // Combina listas observadas com query para filtrar
    private val _uiState = combine(
        repository.observeLists(),
        _query
    ) { lists, query ->
        println("DEBUG: HomeViewModel - COMBINE ACIONADO com ${lists.size} listas do repositório")
        lists.forEach { lista ->
            println("DEBUG: Lista RECEBIDA no ViewModel: ${lista.id} - ${lista.titulo}")
        }

        val filtered = if (query.isEmpty()) {
            lists
        } else {
            lists.filter {
                it.titulo.contains(query, ignoreCase = true)
            }
        }

        println("DEBUG: HomeViewModel - ${filtered.size} listas FILTRADAS (query: '$query')")
        filtered.forEach { lista ->
            println("DEBUG: Lista FILTRADA: ${lista.id} - ${lista.titulo}")
        }

        val newState = HomeUiState(
            allLists = lists,
            filteredLists = filtered,
            query = query
        )

        println("DEBUG: HomeViewModel - NOVO ESTADO criado com ${newState.filteredLists.size} listas filtradas")

        newState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    val uiState: StateFlow<HomeUiState> = _uiState

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
        println("DEBUG: HomeViewModel - createList() INICIADO com title='$title', imageUri='$imageUri'")
        viewModelScope.launch {
            try {
                println("DEBUG: HomeViewModel - Chamando repository.create()")
                val newList = repository.create(title, imageUri)
                println("DEBUG: HomeViewModel - repository.create() EXECUTADO com sucesso, ID: ${newList.id}")
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - ERRO no repository.create(): ${e.message}")
                e.printStackTrace()
            }
        }
        println("DEBUG: HomeViewModel - createList() FINALIZADO")
    }

    /**
     * Cria nova lista e retorna a lista criada (para uso com imagem)
     */
    suspend fun createListAndReturn(title: String, imageUri: String? = null): ShoppingList? {
        println("DEBUG: HomeViewModel - createListAndReturn() INICIADO com title='$title', imageUri='$imageUri'")
        return try {
            println("DEBUG: HomeViewModel - Chamando repository.create()")
            val newList = repository.create(title, imageUri)
            println("DEBUG: HomeViewModel - repository.create() EXECUTADO com sucesso, ID: ${newList.id}")
            newList
        } catch (e: Exception) {
            println("DEBUG: HomeViewModel - ERRO no repository.create(): ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Atualiza uma lista existente
     */
    fun updateList(list: ShoppingList) {
        viewModelScope.launch {
            repository.update(list)
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
