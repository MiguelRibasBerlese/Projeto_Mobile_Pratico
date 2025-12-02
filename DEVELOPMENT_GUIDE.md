# Guia de Desenvolvimento - Lista de Compras

## 🚀 Quick Start

### 1. Configuração Inicial
```bash
# Clone o repositório
git clone <repository-url>
cd Projeto_Mobile_Pratico

# Abrir no Android Studio
# File -> Open -> Selecionar a pasta do projeto
```

### 2. Firebase Setup
```bash
# 1. Criar projeto Firebase
# 2. Adicionar app Android
# 3. Download google-services.json para app/
# 4. Configurar Auth + Firestore no console
```

### 3. Primeira Execução
```bash
# Build + Run
./gradlew assembleDebug
# ou Ctrl+F9 no Android Studio
```

## 📁 Estrutura do Projeto

```
app/src/main/java/com/example/projetopratico_mobile1/
├── data/
│   ├── auth/
│   │   └── AuthRepository.kt
│   ├── firebase/
│   │   ├── FirestoreListRepository.kt
│   │   └── FirestoreItemRepository.kt
│   ├── models/
│   │   ├── ShoppingList.kt
│   │   ├── Item.kt
│   │   └── Categoria.kt
│   ├── repo/
│   │   ├── ListRepository.kt (interface)
│   │   ├── ItemRepository.kt (interface)
│   │   ├── InMemoryListRepository.kt
│   │   ├── InMemoryItemRepository.kt
│   │   └── RepoProvider.kt
│   └── InMemoryStore.kt
├── ui/
│   ├── home/
│   │   ├── HomeActivity.kt
│   │   ├── HomeViewModel.kt
│   │   ├── ListViewModelFactory.kt
│   │   └── ListaComprasAdapter.kt
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── AuthViewModel.kt
│   ├── listform/
│   │   └── ListFormActivity.kt
│   ├── listdetail/
│   │   ├── ListDetailActivity.kt
│   │   ├── ItemListViewModel.kt
│   │   ├── ItemListViewModelFactory.kt
│   │   ├── ItensAdapter.kt
│   │   └── AdapterDataConverter.kt
│   └── itemform/
│       ├── ItemFormActivity.kt
│       └── ItemViewModel.kt
└── util/
    ├── LocalImageStore.kt
    ├── Extensions.kt
    └── Validators.kt
```

## 🏗️ Padrões de Desenvolvimento

### Repository Pattern
```kotlin
// 1. Definir interface
interface ListRepository {
    fun observeLists(): Flow<List<ShoppingList>>
    suspend fun create(title: String, imageUri: String?): ShoppingList
}

// 2. Implementar para cada data source
class FirestoreListRepository : ListRepository { ... }
class InMemoryListRepository : ListRepository { ... }

// 3. Provider escolhe implementação
object RepoProvider {
    fun provideListRepository(context: Context): ListRepository
}
```

### MVVM Pattern
```kotlin
// 1. ViewModel processa lógica de negócio
class HomeViewModel(private val repository: ListRepository) : ViewModel() {
    val uiState: StateFlow<ListUiState> = repository.observeLists()
        .map { lists -> ListUiState(lists) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())
}

// 2. Activity observa mudanças de estado
class HomeActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels { ListViewModelFactory(repository) }
    
    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.lists)
                }
            }
        }
    }
}
```

### Flow + StateFlow
```kotlin
// Combinar múltiplas fontes de dados
val uiState = combine(
    repository.observeLists(),
    _searchQuery,
    _filterCriteria
) { lists, query, filter ->
    UiState(
        lists = lists.filter { it.matches(query, filter) },
        isLoading = false
    )
}.stateIn(scope, started, initial)

// Coleta segura com lifecycle
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.collect { value -> updateUI(value) }
    }
}
```

## 🔧 Como Adicionar Novas Features

### 1. Nova Tela (Activity)
```kotlin
// 1. Criar layout XML
app/src/main/res/layout/activity_nova_tela.xml

// 2. Criar Activity com ViewBinding
class NovaTelaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNovaTelaBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaTelaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

// 3. Registrar no AndroidManifest.xml
<activity android:name=".ui.novatela.NovaTelaActivity" />

// 4. Navegação de outras telas
startActivity(Intent(this, NovaTelaActivity::class.java))
```

### 2. Novo Repository
```kotlin
// 1. Definir interface
interface NovoRepository {
    fun observeData(): Flow<List<DataModel>>
    suspend fun save(data: DataModel)
}

// 2. Implementação Firestore
class FirestoreNovoRepository : NovoRepository {
    override fun observeData(): Flow<List<DataModel>> = callbackFlow {
        val listener = firestore.collection("novos")
            .addSnapshotListener { snapshot, error ->
                val data = snapshot?.documents?.map { /* convert */ } ?: emptyList()
                trySend(data)
            }
        awaitClose { listener.remove() }
    }
}

// 3. Implementação InMemory
class InMemoryNovoRepository : NovoRepository { ... }

// 4. Adicionar ao RepoProvider
fun provideNovoRepository(): NovoRepository {
    return if (FirebaseAuth.getInstance().currentUser != null) {
        FirestoreNovoRepository()
    } else {
        InMemoryNovoRepository()
    }
}
```

### 3. Novo ViewModel
```kotlin
// 1. Definir UI State
data class NovoUiState(
    val items: List<DataModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// 2. Criar ViewModel
class NovoViewModel(private val repository: NovoRepository) : ViewModel() {
    
    private val _uiState = repository.observeData()
        .map { data -> NovoUiState(items = data) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NovoUiState())
    
    val uiState: StateFlow<NovoUiState> = _uiState
    
    fun saveData(data: DataModel) {
        viewModelScope.launch {
            repository.save(data)
        }
    }
}

// 3. Criar Factory
class NovoViewModelFactory(private val repository: NovoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NovoViewModel(repository) as T
    }
}
```

## 🧪 Testing Strategy

### Unit Tests - ViewModels
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Mock
    private lateinit var mockRepository: ListRepository
    
    @Test
    fun `uiState emits lists from repository`() = runTest {
        // Given
        val testLists = listOf(ShoppingList("1", "Lista Teste"))
        whenever(mockRepository.observeLists()).thenReturn(flowOf(testLists))
        
        // When
        val viewModel = HomeViewModel(mockRepository)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(testLists, state.allLists)
    }
}
```

### Integration Tests - Repository
```kotlin
@Test
fun `FirestoreListRepository creates list successfully`() = runTest {
    // Given
    val repository = FirestoreListRepository(context, mockAuth, mockFirestore)
    val title = "Test List"
    
    // When
    val result = repository.create(title, null)
    
    // Then
    assertEquals(title, result.titulo)
    verify(mockFirestore).collection("lists")
}
```

## 🎨 UI Guidelines

### ViewBinding Setup
```kotlin
// 1. Ativar no build.gradle.kts
android {
    buildFeatures {
        viewBinding = true
    }
}

// 2. Usar em Activities
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Acessar views type-safe
        binding.recyclerView.adapter = adapter
        binding.fabAdd.setOnClickListener { }
    }
}
```

### RecyclerView + DiffUtil
```kotlin
class MeuAdapter : ListAdapter<DataModel, ViewHolder>(DiffCallback) {
    
    object DiffCallback : DiffUtil.ItemCallback<DataModel>() {
        override fun areItemsTheSame(oldItem: DataModel, newItem: DataModel) = 
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DataModel, newItem: DataModel) = 
            oldItem == newItem
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DataModel) {
            binding.txtTitle.text = item.title
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
```

## 🔍 Debugging Tips

### Firebase Debugging
```kotlin
// 1. Verificar autenticação
Log.d("Auth", "Current user: ${FirebaseAuth.getInstance().currentUser?.uid}")

// 2. Verificar regras Firestore
// Console Firebase > Firestore > Regras

// 3. Debug listeners
val listener = collection.addSnapshotListener { snapshot, error ->
    Log.d("Firestore", "Documents: ${snapshot?.documents?.size}")
    error?.let { Log.e("Firestore", "Error: $it") }
}
```

### Flow Debugging
```kotlin
repository.observeLists()
    .onEach { lists -> Log.d("Flow", "Lists received: ${lists.size}") }
    .catch { e -> Log.e("Flow", "Error in flow", e) }
    .collect { lists -> updateUI(lists) }
```

### Layout Inspector
```
Tools > Layout Inspector
- Conectar dispositivo/emulador  
- Ver hierarquia de views
- Debug constraints e margins
```

## 📝 Code Style

### Naming Conventions
```kotlin
// Classes: PascalCase
class HomeViewModel

// Functions/Variables: camelCase  
fun createList()
val isLoading = false

// Constants: UPPER_SNAKE_CASE
const val DEFAULT_TIMEOUT = 5000L

// Resources: snake_case
R.string.app_name
R.layout.activity_home
```

### Comments
```kotlin
/**
 * Repository para gerenciar listas de compras
 * Escolhe entre Firestore (online) ou InMemory (offline)
 */
class ListRepository {
    
    // Observa mudanças nas listas do usuário atual
    fun observeLists(): Flow<List<ShoppingList>> {
        // TODO: Implementar cache local
        return firestore.collection("lists")
    }
}
```

### Error Handling
```kotlin
// Repository level - propagar erros estruturados
suspend fun createList(title: String): Result<ShoppingList> {
    return try {
        val list = firestore.collection("lists").add(data).await()
        Result.success(list.toShoppingList())
    } catch (e: FirebaseException) {
        Result.failure(NetworkException("Erro de conexão"))
    } catch (e: Exception) {
        Result.failure(UnknownException("Erro inesperado"))
    }
}

// ViewModel level - converter para UI state
fun createList(title: String) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        repository.createList(title)
            .onSuccess { list -> /* success state */ }
            .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
    }
}
```

## 🚀 Deployment

### Debug Build
```bash
./gradlew assembleDebug
# APK em: app/build/outputs/apk/debug/
```

### Release Build
```bash
# 1. Configurar signing config no build.gradle
android {
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword "password"
            keyAlias "alias"
            keyPassword "password"
        }
    }
}

# 2. Build release
./gradlew assembleRelease
```

### Firebase App Distribution
```bash
# 1. Instalar CLI
npm install -g firebase-tools

# 2. Upload APK
firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
    --app 1:123456789:android:abc123 \
    --groups "testers" \
    --release-notes "Nova versão com Firestore"
```
