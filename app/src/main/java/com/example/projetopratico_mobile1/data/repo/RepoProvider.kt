package com.example.projetopratico_mobile1.data.repo

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.projetopratico_mobile1.data.firebase.FirestoreListRepository

/**
 * Provedor de repositórios baseado no estado de autenticação
 * Se logado: usa Firestore + local storage
 * Se não logado: usa InMemory (dados temporários)
 */
object RepoProvider {

    /**
     * Fornece o repositório de listas apropriado
     */
    fun provideListRepository(context: Context): ListRepository {
        val currentUser = FirebaseAuth.getInstance().currentUser

        return if (currentUser != null) {
            // Usuário logado - usar Firestore com storage local para imagens
            FirestoreListRepository(
                context = context.applicationContext,
                auth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance()
            )
        } else {
            // Não logado - usar dados em memória (Fase 1)
            InMemoryListRepository()
        }
    }
}
