package com.example.projetopratico_mobile1.data

import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.models.User

/**
 * Guarda todos os dados do app na memória
 * Como é um trabalho simples, não precisa de banco de dados
 */
object InMemoryStore {

    // lista de usuários cadastrados
    private val usuarios = mutableListOf<User>()

    // listas de compras do usuário logado
    private val listas = mutableListOf<ShoppingList>()

    // usuário que está logado no momento
    var usuarioLogado: User? = null
        private set

    // pega todos os usuários (só pra debug se precisar)
    fun getUsuarios(): List<User> = usuarios.toList()

    // pega as listas do usuário logado
    fun getListas(): List<ShoppingList> = listas.toList()

    // adiciona um novo usuário
    fun adicionarUsuario(usuario: User) {
        usuarios.add(usuario)
    }

    // verifica se já existe usuário com esse email
    fun existeEmail(email: String): Boolean {
        return usuarios.any { it.email == email }
    }

    // faz login do usuário
    fun fazerLogin(email: String, senha: String): Boolean {
        val usuario = usuarios.find { it.email == email && it.password == senha }
        if (usuario != null) {
            usuarioLogado = usuario
            return true
        }
        return false
    }

    // faz logout
    fun logout() {
        usuarioLogado = null
        listas.clear() // limpa as listas quando desloga
    }

    // adiciona uma nova lista
    fun adicionarLista(lista: ShoppingList) {
        listas.add(lista)
    }

    // busca uma lista pelo ID
    fun buscarLista(id: String): ShoppingList? {
        return listas.find { it.id == id }
    }
}
