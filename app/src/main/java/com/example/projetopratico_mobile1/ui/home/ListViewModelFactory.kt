package com.example.projetopratico_mobile1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projetopratico_mobile1.data.repo.ListRepository

/**
 * Factory para criar ListViewModel com dependência de repositório
 * Necessário porque o ViewModel tem construtor com parâmetros
 */
class ListViewModelFactory(
    private val repository: ListRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
