# 📱 Lista de Compras - App Android

Aplicativo Android completo para gerenciar listas de compras com autenticação Firebase, persistência na nuvem e isolamento de dados por usuário.

## 🎯 Funcionalidades Principais

### 🔐 **Sistema de Autenticação**
- ✅ Login/cadastro com validação de email
- ✅ Isolamento completo de dados entre usuários
- ✅ Modo offline (dados em RAM) e online (Firebase)
- ✅ Auth Guard protegendo telas principais
- ✅ Logout com limpeza de sessão

### 📋 **Gerenciamento de Listas**
- ✅ CRUD completo: criar, visualizar, editar, excluir
- ✅ Imagens personalizadas salvas localmente
- ✅ Busca em tempo real por nome
- ✅ Sincronização com Firestore
- ✅ Interface reativa com StateFlow

### 🛒 **Gerenciamento de Itens**
- ✅ Adição de itens com categoria, quantidade e unidade
- ✅ Organização automática por categoria
- ✅ Seção separada para itens comprados
- ✅ Toggle instantâneo comprado/não comprado
- ✅ Busca e filtros por nome

### 🏗️ **Arquitetura Técnica**
- ✅ MVVM + Repository Pattern
- ✅ ViewBinding type-safe
- ✅ Lifecycle safety com repeatOnLifecycle
- ✅ Coroutines + Flow para programação reativa
- ✅ Dependency Injection manual
- ✅ Single Activity per Feature

## 🚀 Setup do Projeto

### Requisitos
- **Android Studio**: Hedgehog+ (2023.2.1)
- **SDK mínimo**: API 24 (Android 7.0)
- **SDK target**: API 34 (Android 14)
- **Kotlin**: 1.9.0+
- **Gradle**: 8.0+

### Instalação
1. **Clone o repositório**
   ```bash
   git clone <repository-url>
   cd ProjetoPratico_Mobile12
   ```

2. **Configure o Firebase** (Opcional - funciona offline)
   
   #### Console Firebase:
   - Crie projeto no [Firebase Console](https://console.firebase.google.com)
   - "Adicionar app" → Android → Package: `com.example.projetopratico_mobile1` 
   - Download `google-services.json` → coloque em `app/`
   
   #### Authentication:
   - Authentication → Sign-in method → "Email/Password" → Ativar
   - Users → Permitir criação de contas
   
   #### Firestore Database:
   - Firestore Database → "Criar banco de dados"
   - Modo: "Começar no modo de teste" (30 dias)
   - Localização: us-central1 (ou região mais próxima)

3. **Build e Execute**
   ```bash
   ./gradlew assembleDebug
   # ou execute via Android Studio (Ctrl+F9)
   ```

### Credenciais de Teste
- **Email**: `demo@demo.com`
- **Senha**: `123`

## 🏗️ Arquitetura do Sistema

### Padrão MVVM + Repository
```
┌─────────────────┬─────────────────┬─────────────────┐
│       UI        │   VIEWMODELS    │   REPOSITORIES  │
├─────────────────┼─────────────────┼─────────────────┤
│  LoginActivity  │  AuthViewModel  │ AuthRepository  │
│  HomeActivity   │  HomeViewModel  │ ListRepository  │
│ ListFormActivity│ ItemViewModel   │ ItemRepository  │
│ListDetailActivity│               │ LocalImageStore │
└─────────────────┴─────────────────┴─────────────────┘
              ↕                           ↕
        ViewBinding                 RepoProvider
      (type-safe UI)           (Firebase/InMemory)
```

### Componentes Principais

#### **1. UI Layer (Activities + ViewBinding)**
- **LoginActivity**: Autenticação com validação
- **HomeActivity**: Lista de compras com busca
- **ListFormActivity**: Criar/editar listas + imagens
- **ListDetailActivity**: Itens agrupados por categoria
- **ItemFormActivity**: Adicionar/editar itens

#### **2. ViewModel Layer (StateFlow + LiveData)**
```kotlin
class HomeViewModel(private val repository: ListRepository) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeLists(),
        searchQuery
    ) { lists, query -> /* filtrar e mapear */ }
}
```

#### **3. Repository Layer (Abstração de Dados)**
```kotlin
interface ListRepository {
    fun observeLists(): Flow<List<ShoppingList>>
    suspend fun create(title: String, imageUri: String?): ShoppingList
}

// Implementações:
FirestoreListRepository  // → Firebase Firestore
InMemoryListRepository   // → Dados em RAM
```

#### **4. Data Layer**
- **Firebase Firestore**: Persistência na nuvem
- **InMemoryStore**: Dados temporários (modo offline)
- **LocalImageStore**: Imagens no storage interno
- **AuthManager**: Gerenciamento de usuários

### Tecnologias e Dependências
```gradle
// Firebase
implementation 'com.google.firebase:firebase-auth-ktx:22.3.0'
implementation 'com.google.firebase:firebase-firestore-ktx:24.10.0'

// Android Architecture Components
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.6'

// Coroutines & Flow
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3'
```

## 📱 Funcionalidades Detalhadas

### 🔐 Sistema de Autenticação
```kotlin
// Dual Authentication: Local + Firebase
AuthManager.signIn(email, password)    // → Dados locais
FirebaseAuth.signIn(email, password)   // → Firebase Auth

// Isolamento por usuário
InMemoryStore: Map<userId, List<ShoppingList>>
Firestore: lists/{listId} WHERE ownerUid == currentUser.uid
```

#### Características:
- ✅ **Login híbrido**: AuthManager (local) + Firebase Auth (nuvem)
- ✅ **Isolamento total**: Cada usuário vê apenas suas listas
- ✅ **Validação robusta**: Email válido + senha obrigatória
- ✅ **Auth Guard**: Proteção automática das telas principais
- ✅ **Usuários demo**: `demo@demo.com` / `123` para testes

### 📋 Gerenciamento de Listas

#### HomeActivity - Tela Principal
```kotlin
class HomeActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels {
        ListViewModelFactory(RepoProvider.provideListRepository(this))
    }
    
    // Observação reativa com lifecycle safety
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.filteredLists)
            }
        }
    }
}
```

#### Características:
- ✅ **CRUD completo**: Criar, visualizar, editar, excluir
- ✅ **Busca instantânea**: Filtro por nome em tempo real
- ✅ **Imagens personalizadas**: Seleção do dispositivo + preview
- ✅ **Persistência híbrida**: Local (InMemory) + Nuvem (Firestore)
- ✅ **Empty state**: UX quando não há listas

### 🛒 Gerenciamento de Itens

#### ListDetailActivity - Itens da Lista
```kotlin
// Agrupamento inteligente por categoria
data class GroupedItems(
    val byCategory: Map<Categoria, List<Item>>,  // Não comprados
    val purchased: List<Item>                    // Seção separada
)

// RecyclerView com headers dinâmicos
sealed class RowItem {
    data class Header(val categoria: Categoria) : RowItem()
    data class Produto(val item: Item) : RowItem()
}
```

#### Características:
- ✅ **Categorização automática**: 5 categorias (Alimentos, Bebidas, etc.)
- ✅ **Toggle instantâneo**: Comprado/não comprado com animação
- ✅ **Organização visual**: Headers por categoria + seção comprados
- ✅ **Busca local**: Filtro por nome mantendo agrupamento
- ✅ **Sincronização**: Firestore subcollection `lists/{id}/items`

### 🖼️ Sistema de Imagens
```kotlin
object LocalImageStore {
    fun saveFromContentUri(context: Context, listId: String, uri: String): Boolean
    fun fileForList(context: Context, listId: String): File
    fun exists(context: Context, listId: String): Boolean
}
```

#### Características:
- ✅ **Storage interno**: Sem necessidade de permissões
- ✅ **Identificação única**: Uma imagem por lista (listId)
- ✅ **Fallback graceful**: Placeholder quando não há imagem
- ✅ **Performance**: Cache automático + loading assíncrono

## ⚙️ Detalhes de Implementação

### Estrutura de Dados (Firestore)
```
/users/{userId}
  - name: string
  - email: string
  
/lists/{listId}
  - title: string
  - ownerUid: string
  - createdAt: timestamp
  
/lists/{listId}/items/{itemId}
  - name: string
  - quantity: double
  - unit: string
  - category: string
  - purchased: boolean
```

### Repository Pattern - Dual Mode
```kotlin
object RepoProvider {
    fun provideListRepository(context: Context): ListRepository {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            FirestoreListRepository(context, FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        } else {
            InMemoryListRepository()  // Modo offline
        }
    }
}
```

### StateFlow + Lifecycle Safety
```kotlin
// ViewModel com combine para busca reativa
private val _uiState = combine(
    repository.observeLists(),
    _query
) { lists, query ->
    val filtered = lists.filter { it.titulo.contains(query, ignoreCase = true) }
    HomeUiState(allLists = lists, filteredLists = filtered, query = query)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

// Activity com coleta segura
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            adapter.submitList(state.filteredLists)
            updateEmptyState(state.filteredLists.isEmpty())
        }
    }
}
```

### Agrupamento de Itens por Categoria
```kotlin
data class GroupedItems(
    val byCategory: Map<Categoria, List<Item>>,
    val purchased: List<Item>
)

// Conversion para RecyclerView com headers
fun convertToRowItems(groupedItems: GroupedItems): List<RowItem> {
    val result = mutableListOf<RowItem>()
    
    // Adicionar categorias com items não comprados
    groupedItems.byCategory.forEach { (categoria, items) ->
        if (items.isNotEmpty()) {
            result.add(RowItem.Header(categoria))
            items.forEach { result.add(RowItem.Produto(it)) }
        }
    }
    
    // Adicionar seção de comprados
    if (groupedItems.purchased.isNotEmpty()) {
        result.add(RowItem.Header(Categoria.COMPRADOS))
        groupedItems.purchased.forEach { result.add(RowItem.Produto(it)) }
    }
    
    return result
}
```

## 🧪 Testes e Validação

### Fluxo de Teste Principal
1. **Autenticação**
   ```
   Login com: demo@demo.com / 123
   Ou cadastre novo usuário com email único
   ```

2. **Criação de Lista**
   ```
   HomeActivity → FAB(+) → Digite nome → Selecione imagem → Salvar
   Resultado: Lista aparece na tela principal com imagem
   ```

3. **Adição de Itens**
   ```
   Clique na lista → FAB(+) → Preencha dados → Salvar
   Resultado: Item aparece agrupado por categoria
   ```

4. **Toggle Comprado**
   ```
   Clique no checkbox do item
   Resultado: Item move para seção "Comprados" instantaneamente
   ```

5. **Busca e Filtros**
   ```
   Digite na SearchView (listas ou itens)
   Resultado: Filtro em tempo real preservando layout
   ```

### Cenários de Teste
- ✅ **Usuário novo**: Inicia com lista vazia (sem dados de outros)
- ✅ **Isolamento**: Troca de usuário mostra apenas listas próprias  
- ✅ **Offline**: Funciona sem Firebase configurado
- ✅ **Online**: Sincroniza com Firestore quando logado
- ✅ **Persistência**: Imagens mantidas após restart do app
- ✅ **Performance**: Busca instantânea sem lag

## 📚 Documentação Técnica

### Principais Classes e Responsabilidades

#### **Data Layer**
- `InMemoryStore`: Singleton para dados temporários
- `AuthManager`: Gerenciamento de usuários locais  
- `LocalImageStore`: Persistência de imagens
- `RepoProvider`: Factory para escolha de repositório

#### **Repository Layer** 
- `ListRepository / ItemRepository`: Interfaces de abstração
- `FirestoreListRepository / FirestoreItemRepository`: Implementação Firebase
- `InMemoryListRepository / InMemoryItemRepository`: Implementação local

#### **ViewModel Layer**
- `HomeViewModel`: Estado de listas + busca reativa
- `ItemListViewModel`: Estado de itens + agrupamento
- `AuthViewModel`: Estado de autenticação
- Factories: Injeção de dependência manual

#### **UI Layer**
- Activities com ViewBinding type-safe
- RecyclerView.Adapters com DiffUtil
- StateFlow observers com lifecycle safety

### Estrutura de Pastas
```
app/src/main/java/com/example/projetopratico_mobile1/
├── data/
│   ├── auth/           # AuthManager, AuthRepository
│   ├── firebase/       # Firestore repositories  
│   ├── models/         # ShoppingList, Item, User, Categoria
│   ├── repo/           # Repository interfaces + RepoProvider
│   └── InMemoryStore.kt
├── ui/
│   ├── home/           # HomeActivity + ViewModel + Adapter
│   ├── listdetail/     # ListDetailActivity + agrupamento
│   ├── listform/       # ListFormActivity + imagem
│   ├── itemform/       # ItemFormActivity + validação
│   └── login/          # LoginActivity + AuthViewModel
└── util/               # LocalImageStore, Validators, Extensions
```

## 🎯 Status Final do Projeto

### ✅ **Funcionalidades Completas**
- Sistema de autenticação dual (local + Firebase)
- CRUD completo de listas com imagens
- CRUD completo de itens com categorização  
- Busca em tempo real para listas e itens
- Isolamento total de dados por usuário
- Interface reativa com StateFlow
- Armazenamento híbrido (local + nuvem)

### 🏆 **Qualidade Técnica** 
- Arquitetura MVVM robusta
- Repository Pattern bem implementado
- Lifecycle safety em todas as telas
- Zero APIs deprecadas
- Performance otimizada com DiffUtil
- Tratamento completo de erros

### 📱 **Pronto para Produção**
- App estável e testado
- Código limpo e documentado  
- Estrutura escalável
- UX polida com empty states
- Compatibilidade offline/online

## 📈 Histórico de Desenvolvimento

### v2.1.1 (2025-12-03) - 🎯 CORREÇÕES FINAIS
- ✅ **Isolamento de usuários**: Dados completamente separados por usuário
- ✅ **Sistema de imagens**: LocalImageStore funcionando corretamente  
- ✅ **Botão cadastrar**: Fluxo de registro completamente funcional
- ✅ **Singleton pattern**: Repositórios compartilhados entre Activities
- ✅ **Auth híbrido**: AuthManager (local) + Firebase Auth (nuvem)

### v2.1.0 (2024-12-01) - 🎯 CONFORMIDADE COMPLETA  
- ✅ **MVVM + Repository**: Arquitetura robusta implementada
- ✅ **Firebase Integration**: Auth + Firestore funcionais
- ✅ **ViewBinding**: Type-safe em todas as Activities
- ✅ **APIs Modernas**: GetContent(), ExposedDropdownMenu
- ✅ **Busca Real-time**: Filtros instantâneos com StateFlow

### v2.0.0 (2024-11-15) - 🚀 RELEASE INICIAL
- ✅ **Base Architecture**: MVVM + Repository Pattern  
- ✅ **Authentication**: Sistema de login/cadastro
- ✅ **CRUD Completo**: Listas e itens totalmente funcionais
- ✅ **Local Storage**: Imagens armazenadas internamente
- ✅ **Categorização**: Agrupamento inteligente de itens
- Busca reativa e agrupamento inteligente
- Toggle purchased em tempo real
- Suporte offline (modo InMemory)

## 🔧 Evoluções Futuras

### **Fase 3 - Melhorias UX:**
- **Push Notifications**: Lembretes de compras
- **Offline-first**: Room + sync quando conectar
- **Compartilhamento**: Listas colaborativas entre usuários
- **Temas**: Dark mode + customização

### **Fase 4 - Features Avançadas:**  
- **Geolocalização**: Sugestões baseadas em proximidade
- **OCR**: Escanear listas em papel
- **Analytics**: Histórico de compras e insights
- **Backup**: Export/import para Google Drive

### **Fase 5 - Arquitetura:**
- **Modularização**: Feature modules
- **Compose**: Migração da UI
- **KMM**: Compartilhar lógica com iOS
- **CI/CD**: Pipeline automatizado

## 📊 Métricas de Qualidade

- **Cobertura de testes**: A implementar
- **Performance**: DiffUtil + Flow otimizado
- **Crashlytics**: A integrar
- **Acessibilidade**: Content descriptions implementados
- **Segurança**: Rules do Firestore configuradas

## 📚 Documentação Adicional

- **[ARCHITECTURE.md](./ARCHITECTURE.md)**: Detalhes da arquitetura MVVM + Repository
- **[FIREBASE_SETUP.md](./FIREBASE_SETUP.md)**: Configuração Firebase + API reference
- **[DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)**: Guia de desenvolvimento + padrões
- **[CHANGELOG.md](./CHANGELOG.md)**: Histórico de versões e mudanças

### 🎯 Para Desenvolvedores
```bash
# Setup inicial
git clone <repo>
cd Projeto_Mobile_Pratico
# Configure Firebase (ver FIREBASE_SETUP.md)
./gradlew assembleDebug
```

### 📱 Para Usuários
1. **Login/Registro**: Firebase Auth com email/senha
2. **Criar listas**: Com título + imagem opcional
3. **Adicionar itens**: Por categoria com toggle comprado
4. **Buscar**: Listas e itens em tempo real
5. **Sincronização**: Dados na nuvem + offline funcional
