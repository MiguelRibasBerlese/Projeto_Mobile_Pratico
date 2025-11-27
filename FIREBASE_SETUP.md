# API Reference - Firebase Configuration

## 🔧 Configuração do Firebase

### **1. Console Firebase Setup**

1. **Criar Projeto Firebase:**
   - Acesse [Firebase Console](https://console.firebase.google.com/)
   - "Adicionar projeto" → Nome: "Lista de Compras"
   - Ativar Google Analytics (opcional)

2. **Adicionar App Android:**
   - "Adicionar app" → Android
   - Package name: `com.example.projetopratico_mobile1`
   - App nickname: "Lista Compras Android"
   - Download `google-services.json`

3. **Configurar Authentication:**
   - Authentication → "Começar"
   - Sign-in method → "Email/Password" → Ativar
   - Users → Permitir criação de contas

4. **Configurar Firestore:**
   - Firestore Database → "Criar banco de dados"  
   - Modo: "Começar no modo de teste" (por 30 dias)
   - Local: us-central1 (ou mais próximo)

### **2. Estrutura do Firestore**

#### **Collections Schema:**
```
🗃️ lists/
  📄 {listId}/
    - title: string
    - ownerUid: string
    - createdAt: timestamp (opcional)
    
    🗃️ items/
      📄 {itemId}/
        - name: string
        - quantity: number
        - unit: string  
        - category: string
        - purchased: boolean
        - createdAt: timestamp (opcional)
```

#### **Security Rules (Firestore):**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Regras para listas - apenas o dono pode acessar
    match /lists/{listId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.ownerUid;
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.ownerUid;
      
      // Regras para itens - mesmo dono da lista pai
      match /items/{itemId} {
        allow read, write: if request.auth != null && 
          request.auth.uid == get(/databases/$(database)/documents/lists/$(listId)).data.ownerUid;
      }
    }
  }
}
```

### **3. Dependencies (build.gradle)**

#### **Project level (build.gradle):**
```kotlin
buildscript {
    dependencies {
        classpath "com.google.gms:google-services:4.4.2"
    }
}
```

#### **App level (app/build.gradle.kts):**
```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase BOM para versionamento consistente
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    
    // Firebase services
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Coroutines para Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-ktx:1.9.3")
}
```

## 🔑 Authentication API

### **AuthRepository Implementation:**
```kotlin
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    fun currentUid(): String? = auth.currentUser?.uid
    
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID null após cadastro")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID null após login")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun recover(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() = auth.signOut()
}
```

### **Common Auth Errors:**
```kotlin
when (exception.message) {
    "ERROR_INVALID_EMAIL" -> "Email inválido"
    "ERROR_WRONG_PASSWORD" -> "Senha incorreta"  
    "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado"
    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email já cadastrado"
    "ERROR_WEAK_PASSWORD" -> "Senha muito fraca (min 6 caracteres)"
    "ERROR_NETWORK_REQUEST_FAILED" -> "Sem conexão com a internet"
    else -> "Erro desconhecido: ${exception.message}"
}
```

## 🗄️ Firestore API

### **Lists Repository:**
```kotlin
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
            .orderBy("title") // Opcional: ordenação no servidor
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val lists = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ShoppingList(
                            id = doc.id,
                            titulo = doc.getString("title") ?: "",
                            imagemUri = null // Local apenas
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
        
        // Gerar ID único para usar com LocalImageStore
        val id = UUID.randomUUID().toString()
        
        val firestoreData = mapOf(
            "title" to title,
            "ownerUid" to currentUser.uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        
        // Salvar no Firestore com ID específico
        listsCollection.document(id).set(firestoreData).await()
        
        // Salvar imagem localmente se fornecida
        imageUri?.let { 
            LocalImageStore.saveFromContentUri(context, id, it) 
        }
        
        return ShoppingList(id = id, titulo = title, imagemUri = imageUri)
    }
    
    override suspend fun delete(listId: String) {
        // Deletar documento e subcoleção (items são deletadas automaticamente pelas regras)
        listsCollection.document(listId).delete().await()
        
        // Remover imagem local
        LocalImageStore.deleteForList(context, listId)
    }
}
```

### **Items Repository:**
```kotlin
class FirestoreItemRepository(
    private val firestore: FirebaseFirestore
) : ItemRepository {
    
    override fun observeItems(listId: String): Flow<List<Item>> = callbackFlow {
        val itemsCollection = firestore
            .collection("lists")
            .document(listId)
            .collection("items")
            
        val listener = itemsCollection
            .orderBy("name") // Ordenação alfabética no servidor
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Item(
                            id = doc.id,
                            nome = doc.getString("name") ?: "",
                            quantidade = doc.getDouble("quantity") ?: 0.0,
                            unidade = doc.getString("unit") ?: "",
                            categoria = Categoria.valueOf(
                                doc.getString("category")?.uppercase() ?: "OUTROS"
                            ),
                            comprado = doc.getBoolean("purchased") ?: false
                        )
                    } catch (e: Exception) {
                        null // Ignora documentos malformados
                    }
                } ?: emptyList()
                
                trySend(items)
            }
            
        awaitClose { listener.remove() }
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
```

## 💾 Local Storage API

### **LocalImageStore Implementation:**
```kotlin
object LocalImageStore {
    
    /**
     * Arquivo para imagem da lista no diretório interno
     * Caminho: /data/data/com.example.projetopratico_mobile1/files/images/{listId}.img
     */
    fun fileForList(context: Context, listId: String): File {
        val imagesDir = File(context.applicationContext.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return File(imagesDir, "${listId}.img")
    }
    
    /**
     * Salva imagem do content:// URI para arquivo local
     * Não precisa de permissões - usa diretório interno
     */
    fun saveFromContentUri(context: Context, listId: String, contentUriString: String): Boolean {
        return try {
            val uri = Uri.parse(contentUriString)
            val inputStream = context.applicationContext.contentResolver.openInputStream(uri)
            
            if (inputStream == null) return false
            
            val targetFile = fileForList(context.applicationContext, listId)
            val outputStream = FileOutputStream(targetFile)
            
            // Copia em chunks de 8KB para não sobrecarregar memória
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            inputStream.close()
            outputStream.close()
            
            true
        } catch (e: SecurityException) {
            // URI permission expirou
            false
        } catch (e: Exception) {
            // IOException, etc.
            false
        }
    }
    
    /**
     * Verifica se existe imagem para a lista
     */
    fun exists(context: Context, listId: String): Boolean {
        return fileForList(context.applicationContext, listId).exists()
    }
    
    /**
     * Remove imagem da lista (ao excluir lista)
     */
    fun deleteForList(context: Context, listId: String) {
        try {
            fileForList(context.applicationContext, listId).delete()
        } catch (e: Exception) {
            // Ignorar erros de deleção
        }
    }
}
```

### **Image Selection (GetContent):**
```kotlin
class ListFormActivity : AppCompatActivity() {
    
    // Launcher usando Activity Result API (não deprecado)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Tentar obter permissão persistente (nem todos providers suportam)
            try {
                contentResolver.takePersistableUriPermission(
                    it, 
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Provider não suporta permissão persistente, mas ainda podemos copiar
            }
            
            selectedImageUri = it.toString()
            binding.imgLista.setImageURI(it)
            binding.imgLista.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }
    
    private fun selecionarImagem() {
        pickImage.launch("image/*")
    }
}
```

## 🔄 Flow Patterns

### **Combining Flows:**
```kotlin
// Combinar busca com dados para filtrar
val uiState: StateFlow<ItemListUiState> = combine(
    repository.observeItems(listId),
    _query
) { items, query ->
    // Filtrar por nome
    val filteredItems = if (query.isEmpty()) {
        items
    } else {
        items.filter { it.nome.contains(query, ignoreCase = true) }
    }
    
    // Agrupar por categoria e separar comprados
    val (purchased, notPurchased) = filteredItems.partition { it.comprado }
    val groupedByCategory = notPurchased.groupBy { it.categoria }
    
    ItemListUiState(
        allItems = items,
        groupedItems = GroupedItems(groupedByCategory, purchased),
        query = query
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ItemListUiState())
```

### **Error Handling in Flows:**
```kotlin
fun observeItemsWithErrorHandling(listId: String): Flow<List<Item>> {
    return repository.observeItems(listId)
        .catch { e ->
            // Log error e emit lista vazia como fallback
            Log.e("ItemRepo", "Erro ao observar itens", e)
            emit(emptyList())
        }
        .onEach { items ->
            Log.d("ItemRepo", "Recebidos ${items.size} itens")
        }
}
```

## 📱 UI State Management

### **State Classes:**
```kotlin
data class ListUiState(
    val allLists: List<ShoppingList> = emptyList(),
    val filteredLists: List<ShoppingList> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ItemListUiState(
    val allItems: List<Item> = emptyList(),
    val groupedItems: GroupedItems = GroupedItems(emptyMap(), emptyList()),
    val query: String = "",
    val isLoading: Boolean = false
)

data class GroupedItems(
    val byCategory: Map<Categoria, List<Item>>,
    val purchased: List<Item>
)
```

### **Lifecycle-Safe Collection:**
```kotlin
// ❌ Perigoso - pode vazar memória
viewModel.uiState.collect { state ->
    updateUI(state)
}

// ✅ Seguro - para quando activity/fragment não está ativo
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }
}
```

## 🚨 Troubleshooting

### **Common Issues:**

1. **"google-services.json not found"**
   ```
   Solução: Colocar arquivo na pasta app/ (não app/src/)
   ```

2. **"Default FirebaseApp is not initialized"**
   ```kotlin
   // Verificar se plugin está aplicado em app/build.gradle.kts
   plugins {
       id("com.google.gms.google-services")
   }
   ```

3. **"Permission denied" no Firestore**
   ```javascript
   // Verificar regras de segurança no console
   // Garantir que ownerUid está sendo setado corretamente
   ```

4. **"Cannot create an instance of ViewModel"**
   ```kotlin
   // Usar Factory para ViewModels com dependências
   private val viewModel: HomeViewModel by viewModels {
       ListViewModelFactory(repository)
   }
   ```

5. **Images não aparecem após reiniciar app**
   ```kotlin
   // Verificar se LocalImageStore.saveFromContentUri() foi chamado
   // Verificar se arquivo existe antes de carregar
   ```
