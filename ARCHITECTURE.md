# Arquitetura do Projeto - Lista de Compras

## 📐 Visão Geral da Arquitetura

Este projeto segue o padrão **MVVM (Model-View-ViewModel)** com **Repository Pattern** e **Clean Architecture** principles.

```
┌─────────────────┬─────────────────┬─────────────────┐
│       UI        │    DOMAIN       │      DATA       │
├─────────────────┼─────────────────┼─────────────────┤
│  Activities     │   ViewModels    │  Repositories   │
│  Fragments      │   Use Cases     │  Data Sources   │
│  Adapters       │   States        │  APIs/DB        │
└─────────────────┴─────────────────┴─────────────────┘
```

## 🏗️ Camadas da Aplicação

### **1. UI Layer (Presentation)**
**Responsabilidade**: Exibir dados e capturar interações do usuário

**Componentes:**
- `Activities`: Hospedam fragments e gerenciam navegação
- `ViewBinding`: Type-safe access às views
- `Adapters`: RecyclerView com DiffUtil para performance
- `States`: Data classes representando estado da UI

**Exemplo:**
```kotlin
class HomeActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels { 
        ListViewModelFactory(RepoProvider.provideListRepository(this))
    }
    
    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.filteredLists)
                    atualizarEmptyState(state.allLists.isEmpty())
                }
            }
        }
    }
}
```

### **2. Domain Layer (Business Logic)**
**Responsabilidade**: Lógica de negócio e regras da aplicação

**Componentes:**
- `ViewModels`: Processam dados e expõem estados via StateFlow
- `Use Cases`: Operações específicas de negócio (futuramente)
- `Models`: Entidades de domínio (ShoppingList, Item, etc.)

**Exemplo:**
```kotlin
class HomeViewModel(
    private val repository: ListRepository
) : ViewModel() {
    
    private val _query = MutableStateFlow("")
    
    val uiState: StateFlow<ListUiState> = combine(
        repository.observeLists(),
        _query
    ) { lists, query ->
        ListUiState(
            allLists = lists,
            filteredLists = lists.filter { it.titulo.contains(query, true) },
            query = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())
}
```

### **3. Data Layer (Data Access)**
**Responsabilidade**: Acesso a dados locais e remotos

**Componentes:**
- `Repositories`: Abstração de fontes de dados
- `Data Sources`: Implementações específicas (Firestore, InMemory, Local)
- `RepoProvider`: Factory que escolhe implementação baseada no contexto

**Exemplo:**
```kotlin
interface ListRepository {
    fun observeLists(): Flow<List<ShoppingList>>
    suspend fun create(title: String, imageUri: String?): ShoppingList
    suspend fun delete(listId: String)
}

class FirestoreListRepository(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ListRepository {
    
    override fun observeLists(): Flow<List<ShoppingList>> = callbackFlow {
        val listener = firestore.collection("lists")
            .whereEqualTo("ownerUid", auth.currentUser?.uid)
            .addSnapshotListener { snapshot, error ->
                val lists = snapshot?.documents?.map { /* mapping */ } ?: emptyList()
                trySend(lists)
            }
        awaitClose { listener.remove() }
    }
}
```

## 🔄 Fluxo de Dados (Data Flow)

### **Fluxo Reativo (Observable Pattern):**
```
User Input → ViewModel → Repository → Data Source
    ↓           ↓           ↓            ↓
   UI ←─── StateFlow ←─── Flow ←──── Firestore
```

### **Exemplo Completo - Busca de Listas:**

1. **User digita no SearchView** → `onQueryTextChange`
2. **Activity chama ViewModel** → `viewModel.setQuery(newText)`
3. **ViewModel atualiza MutableStateFlow** → `_query.value = newText`
4. **combine() recomputa automaticamente** → Filtra listas
5. **StateFlow emite novo estado** → `ListUiState` atualizado
6. **Activity coleta com lifecycle safety** → `repeatOnLifecycle`
7. **Adapter recebe nova lista** → `adapter.submitList()`
8. **DiffUtil otimiza atualizações** → UI atualizada sem flicker

## 🏛️ Repository Pattern Implementation

### **Abstração (Contracts):**
```kotlin
interface ListRepository {
    fun observeLists(): Flow<List<ShoppingList>>
    suspend fun create(title: String, imageUri: String?): ShoppingList
    suspend fun update(list: ShoppingList)
    suspend fun delete(listId: String)
    suspend fun getById(listId: String): ShoppingList?
}

interface ItemRepository {
    fun observeItems(listId: String): Flow<List<Item>>
    suspend fun addItem(listId: String, item: Item)
    suspend fun updateItem(listId: String, item: Item)
    suspend fun removeItem(listId: String, itemId: String)
    suspend fun togglePurchased(listId: String, itemId: String, purchased: Boolean)
}
```

### **Implementações:**
```kotlin
// Para usuários logados - dados na nuvem
class FirestoreListRepository : ListRepository
class FirestoreItemRepository : ItemRepository

// Para usuários offline - dados temporários
class InMemoryListRepository : ListRepository  
class InMemoryItemRepository : ItemRepository
```

### **Provider (Factory):**
```kotlin
object RepoProvider {
    fun provideListRepository(context: Context): ListRepository {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            FirestoreListRepository(context, auth, firestore)
        } else {
            InMemoryListRepository()
        }
    }
}
```

## 🔧 Dependency Injection (Manual)

### **ViewModel Factories:**
```kotlin
class ListViewModelFactory(
    private val repository: ListRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
```

### **Injeção nas Activities:**
```kotlin
private val viewModel: HomeViewModel by viewModels {
    ListViewModelFactory(RepoProvider.provideListRepository(this))
}
```

## ⚡ Performance Optimizations

### **1. StateFlow + stateIn:**
```kotlin
val uiState: StateFlow<UiState> = repository.observeData()
    .map { /* transformation */ }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Para após 5s sem observers
        initialValue = UiState()
    )
```

### **2. DiffUtil + ListAdapter:**
```kotlin
class ListaComprasAdapter : ListAdapter<ShoppingList, ViewHolder>(DiffCallback) {
    object DiffCallback : DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = 
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = 
            oldItem == newItem
    }
}
```

### **3. Lifecycle Safety:**
```kotlin
// ❌ Vazamento de memória
viewModel.uiState.collect { }

// ✅ Coleta segura
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { }
    }
}
```

## 🔒 Error Handling Strategy

### **Repository Level:**
```kotlin
override suspend fun create(title: String, imageUri: String?): ShoppingList {
    return try {
        val firestoreData = mapOf("title" to title, "ownerUid" to auth.currentUser?.uid)
        val docRef = firestore.collection("lists").add(firestoreData).await()
        
        // Salvar imagem local se fornecida
        imageUri?.let { LocalImageStore.saveFromContentUri(context, docRef.id, it) }
        
        ShoppingList(id = docRef.id, titulo = title, imagemUri = imageUri)
    } catch (e: Exception) {
        throw Exception("Erro ao criar lista: ${e.message}")
    }
}
```

### **ViewModel Level:**
```kotlin
fun createList(title: String, imageUri: String?) {
    viewModelScope.launch {
        try {
            repository.create(title, imageUri)
            // Estado atualizado automaticamente via Flow
        } catch (e: Exception) {
            _errorState.value = "Erro ao criar lista"
        }
    }
}
```

## 🌐 Offline-First Strategy

### **Repository Selection:**
```kotlin
object RepoProvider {
    fun provideListRepository(context: Context): ListRepository {
        val isOnline = NetworkUtils.isConnected(context)
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        
        return when {
            isLoggedIn && isOnline -> FirestoreListRepository(...)
            else -> InMemoryListRepository() // Fallback para offline
        }
    }
}
```

### **Data Synchronization:**
```kotlin
// Futura implementação: Room + Firestore sync
class HybridRepository : ListRepository {
    private val local = RoomRepository()
    private val remote = FirestoreRepository()
    
    override fun observeLists(): Flow<List<ShoppingList>> {
        return combine(local.observeLists(), remote.observeLists()) { localLists, remoteLists ->
            // Merge strategy: remote wins, local for offline
            if (NetworkUtils.isConnected()) remoteLists else localLists
        }
    }
}
```

## 📊 Testing Strategy (Future)

### **Unit Tests - ViewModels:**
```kotlin
@Test
fun `setQuery filters lists correctly`() = runTest {
    val mockRepo = MockListRepository()
    val viewModel = HomeViewModel(mockRepo)
    
    viewModel.setQuery("test")
    
    val state = viewModel.uiState.first()
    assertEquals("test", state.query)
    assertTrue(state.filteredLists.all { it.titulo.contains("test", true) })
}
```

### **Integration Tests - Repository:**
```kotlin
@Test
fun `FirestoreListRepository creates list successfully`() = runTest {
    val repository = FirestoreListRepository(context, mockAuth, mockFirestore)
    
    val result = repository.create("Test List", null)
    
    assertEquals("Test List", result.titulo)
    verify(mockFirestore).collection("lists").add(any())
}
```
