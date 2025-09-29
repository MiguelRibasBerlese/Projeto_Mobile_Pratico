package com.example.projetopratico_mobile1.util

import android.view.View
import android.widget.Toast

/**
 * Extensão para exibir Toast de forma simples
 */
fun View.showToast(message: String) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
}
