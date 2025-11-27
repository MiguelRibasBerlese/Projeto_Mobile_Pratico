package com.example.projetopratico_mobile1.data.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.projetopratico_mobile1.data.models.ShoppingList
import com.example.projetopratico_mobile1.data.repo.ListRepository
import com.example.projetopratico_mobile1.util.LocalImageStore
import java.util.UUID

/**
 * Implementação do repositório usando Firestore
 * Listas ficam na nuvem, imagens ficam locais no device
 */
class FirestoreListRepository(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ListRepository {

    private val listsCollection = firestore.collection("lists")

    override fun observeLists(): Flow<List<ShoppingList>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        val listener = listsCollection
            .whereEqualTo("ownerUid", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val lists = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val title = doc.getString("title") ?: ""

                        // Não salvamos imageUrl no Firestore - imagem fica só local
                        ShoppingList(
                            id = id,
                            titulo = title,
                            imagemUri = null // será preenchido pelo adapter consultando LocalImageStore
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(lists)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun create(title: String, imageUri: String?): ShoppingList {
        val currentUser = auth.currentUser ?: throw Exception("Usuário não logado")

        // Gerar ID único
        val id = UUID.randomUUID().toString()

        // Dados para Firestore (sem imageUrl)
        val firestoreData = mapOf(
            "title" to title,
            "ownerUid" to currentUser.uid
        )

        // Salvar no Firestore
        listsCollection.document(id).set(firestoreData).await()

        // Se tem imagem, salvar localmente
        if (!imageUri.isNullOrBlank()) {
            LocalImageStore.saveFromContentUri(context, id, imageUri)
        }

        return ShoppingList(
            id = id,
            titulo = title,
            imagemUri = imageUri
        )
    }

    override suspend fun update(list: ShoppingList) {
        val currentUser = auth.currentUser ?: throw Exception("Usuário não logado")

        // Atualizar no Firestore
        val firestoreData = mapOf(
            "title" to list.titulo,
            "ownerUid" to currentUser.uid
        )

        listsCollection.document(list.id).set(firestoreData).await()

        // Imagem local não precisa ser atualizada aqui - é gerenciada separadamente
    }

    override suspend fun delete(listId: String) {
        // Remover do Firestore
        listsCollection.document(listId).delete().await()

        // Remover imagem local
        LocalImageStore.deleteForList(context, listId)
    }

    override suspend fun getById(listId: String): ShoppingList? {
        return try {
            val doc = listsCollection.document(listId).get().await()
            if (doc.exists()) {
                val title = doc.getString("title") ?: ""
                ShoppingList(
                    id = doc.id,
                    titulo = title,
                    imagemUri = null
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
