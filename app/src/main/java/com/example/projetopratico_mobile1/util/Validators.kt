package com.example.projetopratico_mobile1.util

import android.util.Patterns

/**
 * Objeto com validadores para uso geral no app
 */
object Validators {
    /**
     * Valida se o email está em formato válido
     */
    fun isEmailValid(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    /**
     * Verifica se todos os textos fornecidos não estão em branco
     */
    fun notBlank(vararg texts: String) = texts.all { it.trim().isNotEmpty() }
}
