package com.example.projetopratico_mobile1.data.repo

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.projetopratico_mobile1.data.firebase.FirestoreListRepository
import com.example.projetopratico_mobile1.data.firebase.FirestoreItemRepository

/**
 * Provedor de repositórios baseado no estado de autenticação
 * Se logado: usa Firestore + local storage
 * Se não logado: usa InMemory (dados temporários)
 */
object RepoProvider {

    // CORREÇÃO: Singletons dos repositórios para evitar instâncias diferentes
    private val inMemoryListRepository = InMemoryListRepository()
    private val inMemoryItemRepository = InMemoryItemRepository()

    /**
     * Fornece o repositório de listas apropriado
     */
    fun provideListRepository(context: Context): ListRepository {
        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null // Firebase não configurado
        }

        return if (currentUser != null) {
            // Usuário logado - usar Firestore com storage local para imagens
            FirestoreListRepository(
                context = context.applicationContext,
                auth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance()
            )
        } else {
            // Não logado - usar dados em memória (Fase 1) - SINGLETON!
            println("DEBUG: RepoProvider - Retornando InMemoryListRepository SINGLETON")
            inMemoryListRepository
        }
    }

    /**
     * Fornece o repositório de itens apropriado
     */
    fun provideItemRepository(): ItemRepository {
        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null // Firebase não configurado
        }

        return if (currentUser != null) {
            // Usuário logado - usar Firestore
            FirestoreItemRepository(FirebaseFirestore.getInstance())
        } else {
            // Não logado - usar dados em memória (Fase 1) - SINGLETON!
            println("DEBUG: RepoProvider - Retornando InMemoryItemRepository SINGLETON")
            inMemoryItemRepository
        }
    }
}
