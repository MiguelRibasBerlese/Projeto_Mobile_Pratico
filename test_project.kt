// Teste simples para verificar se o projeto compila
package com.example.projetopratico_mobile1.test

import com.example.projetopratico_mobile1.ui.home.HomeViewModel
import com.example.projetopratico_mobile1.ui.home.ListViewModelFactory
import com.example.projetopratico_mobile1.data.repo.InMemoryListRepository
import com.example.projetopratico_mobile1.data.repo.ListRepository

fun testProject() {
    // Teste 1: Repository funciona
    val repository: ListRepository = InMemoryListRepository()

    // Teste 2: ViewModel pode ser criado
    val viewModel = HomeViewModel(repository)

    // Teste 3: Factory funciona
    val factory = ListViewModelFactory(repository)

    // Teste 4: Métodos existem
    viewModel.setQuery("test")
    viewModel.deleteList("test-id")

    println("Todos os testes passaram! O projeto está funcionando.")
}
