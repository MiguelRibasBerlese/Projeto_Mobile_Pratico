package com.example.projetopratico_mobile1.util

import android.view.View
import android.widget.Toast

fun View.showToast(msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
