package com.example.projetopratico_mobile1.data.models

import java.util.UUID

/**
 * Modelo de dados para usuário
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var email: String,
    var password: String
)
