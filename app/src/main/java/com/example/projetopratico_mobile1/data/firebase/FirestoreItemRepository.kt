package com.example.projetopratico_mobile1.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.projetopratico_mobile1.data.models.Item
import com.example.projetopratico_mobile1.data.models.Categoria
import com.example.projetopratico_mobile1.data.repo.ItemRepository

/**
 * Implementação Firestore do repositório de itens
 * Coleção: lists/{listId}/items
 * Sem Firebase Storage - apenas dados dos itens
 */
class FirestoreItemRepository(
    private val firestore: FirebaseFirestore
) : ItemRepository {

    override fun observeItems(listId: String): Flow<List<Item>> = callbackFlow {
        val itemsCollection = firestore
            .collection("lists")
            .document(listId)
            .collection("items")

        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val nome = doc.getString("name") ?: ""
                    val quantidade = doc.getDouble("quantity") ?: 0.0
                    val unidade = doc.getString("unit") ?: ""
                    val categoriaStr = doc.getString("category") ?: "OUTROS"
                    val comprado = doc.getBoolean("purchased") ?: false

                    // Parse categoria com fallback
                    val categoria = try {
                        Categoria.valueOf(categoriaStr.uppercase())
                    } catch (e: Exception) {
                        Categoria.OUTROS
                    }

                    Item(
                        id = id,
                        nome = nome,
                        quantidade = quantidade,
                        unidade = unidade,
                        categoria = categoria,
                        comprado = comprado
                    )
                } catch (e: Exception) {
                    null // ignorar doc malformado
                }
            }?.sortedBy { it.nome } ?: emptyList() // ordena por nome

            trySend(items)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addItem(listId: String, item: Item) {
        val itemsCollection = firestore
            .collection("lists")
            .document(listId)
            .collection("items")

        val firestoreData = mapOf(
            "name" to item.nome,
            "quantity" to item.quantidade,
            "unit" to item.unidade,
            "category" to item.categoria.name,
            "purchased" to item.comprado
        )

        // Se o item já tem ID, usar como document ID
        if (item.id.isNotBlank()) {
            itemsCollection.document(item.id).set(firestoreData).await()
        } else {
            itemsCollection.add(firestoreData).await()
        }
    }

    override suspend fun updateItem(listId: String, item: Item) {
        val firestoreData = mapOf(
            "name" to item.nome,
            "quantity" to item.quantidade,
            "unit" to item.unidade,
            "category" to item.categoria.name,
            "purchased" to item.comprado
        )

        firestore
            .collection("lists")
            .document(listId)
            .collection("items")
            .document(item.id)
            .set(firestoreData)
            .await()
    }

    override suspend fun removeItem(listId: String, itemId: String) {
        firestore
            .collection("lists")
            .document(listId)
            .collection("items")
            .document(itemId)
            .delete()
            .await()
    }

    override suspend fun togglePurchased(listId: String, itemId: String, purchased: Boolean) {
        firestore
            .collection("lists")
            .document(listId)
            .collection("items")
            .document(itemId)
            .update("purchased", purchased)
            .await()
    }
}
