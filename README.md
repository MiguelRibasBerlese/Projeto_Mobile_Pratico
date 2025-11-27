# Lista de Compras - App Android

App Android para gerenciar listas de compras com autenticação, persistência na nuvem e armazenamento local de imagens.

## 📱 Objetivo

Aplicativo completo para criar e gerenciar listas de compras, oferecendo:
- **Autenticação**: Login/registro com Firebase Auth
- **Listas persistentes**: Salvas no Firestore (nuvem)
- **Imagens locais**: Fotos das listas armazenadas no dispositivo
- **Itens organizados**: Por categoria com busca em tempo real
- **Sincronização**: Dados acessíveis em qualquer dispositivo logado
- **Modo offline**: Funciona sem login (dados temporários)

## 🚀 Como Rodar

1. Clone o repositório
2. Abra no Android Studio
3. Configure o Firebase:
   - Adicione seu `google-services.json` em `app/`
   - Configure Authentication (Email/Password) no console
   - Configure Firestore Database
4. Sync do Gradle
5. Execute no emulador ou dispositivo

**Requisitos:**
- Android Studio Hedgehog ou superior
- SDK mínimo: API 24 (Android 7.0)
- SDK alvo: API 34 (Android 14)
- Projeto Firebase configurado

## 🏗️ Arquitetura

**MVVM + Repository Pattern:**
- **Activities + ViewBinding:** UI reativa e type-safe
- **ViewModels + StateFlow:** Gestão de estado com lifecycle safety
- **Repository Pattern:** Abstração de dados (Firestore + InMemory)
- **Firebase Auth:** Autenticação de usuários
- **Firestore:** Persistência de listas e itens na nuvem
- **Local Storage:** Imagens das listas no dispositivo
- **Coroutines + Flow:** Programação assíncrona e reativa

**Dependências principais:**
```kotlin
// Firebase
firebase-auth-ktx
firebase-firestore-ktx
// Android Architecture
lifecycle-viewmodel-ktx
lifecycle-runtime-ktx
// Coroutines
kotlinx-coroutines-android
kotlinx-coroutines-play-services
```

## 📱 Telas e Fluxos

### 1. Login/Registro (LoginActivity)
- **Login**: Email/senha com Firebase Auth
- **Registro**: Criação de conta nova
- **Recuperação**: Reset de senha por email
- **Validação**: Campos obrigatórios + feedback de erros
- **Auth Guard**: Bloqueia acesso sem login

### 2. Home - Listas (HomeActivity)
- **MVVM**: HomeViewModel + ListRepository (Firestore/InMemory)
- **Dados reais**: Listas sincronizadas do Firestore
- **Busca em tempo real**: Filtro por título (case-insensitive)
- **Imagens locais**: Carregadas do dispositivo ou placeholder
- **Actions**: Criar, editar, excluir listas
- **Empty state**: Quando não há listas
- **Logout**: Opção no menu para sair

### 3. Formulário de Lista (ListFormActivity)
- **Criar/editar**: Listas com nome obrigatório
- **Seleção de imagem**: GetContent() do dispositivo
- **Armazenamento**: Imagem salva localmente via LocalImageStore
- **Persistência**: Lista salva no Firestore com ownerUid
- **Preview**: Imagem mantida durante rotação

### 4. Detalhes da Lista (ListDetailActivity)
- **MVVM**: ItemListViewModel + ItemRepository
- **Agrupamento inteligente**: Por categoria + seção "Comprados"
- **Busca local**: Filtra itens por nome em tempo real
- **Toggle purchased**: Move itens entre seções instantaneamente
- **Sincronização**: Itens persistidos em lists/{listId}/items
- **Ordenação**: Alfabética dentro de cada categoria

### 5. Formulário de Item (ItemFormActivity)
- **Campos**: Nome, quantidade, unidade, categoria
- **Unidade dropdown**: Dropdown real (não botão)
- **Validação**: Nome e quantidade obrigatórios
- **Categorias**: Alimentos, Bebidas, Higiene, Limpeza, Outros

## ✅ Requisitos Funcionais Implementados

**🔐 Autenticação (Firebase Auth):**
- ✅ Login com email/senha
- ✅ Registro de novos usuários  
- ✅ Recuperação de senha por email
- ✅ Auth Guard: bloqueia acesso sem login
- ✅ Logout com limpeza de sessão

**📋 Gerenciar Listas (Firestore + Local):**
- ✅ CRUD completo: criar, visualizar, editar, excluir
- ✅ **Imagens locais**: seleção, preview, persistência no device
- ✅ **Persistência na nuvem**: listas sincronizadas via Firestore  
- ✅ **Busca em tempo real**: filtro por título (case-insensitive)
- ✅ **MVVM + Repository**: padrão arquitetural completo
- ✅ Ordenação A-Z automática

**🛒 Gerenciar Itens (Firestore):**
- ✅ CRUD de itens por lista em subcoleção Firestore
- ✅ Campos: nome, quantidade, unidade, categoria, comprado
- ✅ **Dropdown de unidade**: não mais botão, UI correta
- ✅ Validação robusta + tratamento de erros
- ✅ Sincronização em tempo real via Flow

**🏷️ Organização por Categoria:**
- ✅ 5 categorias: Alimentos, Bebidas, Higiene, Limpeza, Outros
- ✅ **Agrupamento inteligente**: headers visuais por categoria
- ✅ **Seção "Comprados"**: separada do resto
- ✅ Ícones distintos para cada categoria

**✅ Marcar Comprados (Real-time):**
- ✅ **Toggle em tempo real**: item move entre seções instantaneamente
- ✅ **Persistência imediata**: estado salvo no Firestore
- ✅ **UI responsiva**: sem reload manual, tudo via Flow

**🔍 Busca Avançada:**
- ✅ **Busca em listas**: por título, mantém imagens
- ✅ **Busca em itens**: por nome, preserva agrupamento
- ✅ **Filtros reativos**: resultado em tempo real
- ✅ Case-insensitive e mantém ordenação

**📱 Recursos Técnicos:**
- ✅ **Modo offline**: InMemory quando não logado  
- ✅ **Lifecycle safety**: repeatOnLifecycle + StateFlow
- ✅ **Sem APIs deprecadas**: GetContent, tasks.await()
- ✅ **Tratamento de rotação**: estado preservado
- ✅ **Empty states**: UX quando não há dados

## 🛠️ Implementação Técnica

### **Arquitetura MVVM + Repository:**

**Repository Pattern:**
```kotlin
// Abstração que escolhe entre Firestore ou InMemory baseado no auth
object RepoProvider {
    fun provideListRepository(context: Context): ListRepository
    fun provideItemRepository(): ItemRepository
}

// Implementações
class FirestoreListRepository : ListRepository  // Firestore + LocalImageStore
class InMemoryListRepository : ListRepository  // RAM apenas
```

**ViewModels com StateFlow:**
```kotlin
class HomeViewModel(repository: ListRepository) : ViewModel() {
    val uiState: StateFlow<ListUiState> = combine(
        repository.observeLists(),
        _query
    ) { lists, query -> /* filtra e mapeia */ }
    
    fun setQuery(query: String) // busca reativa
    fun createList(title: String, imageUri: String?)
}
```

**Firestore Collections:**
```
/lists/{listId}
  - title: String
  - ownerUid: String
  
/lists/{listId}/items/{itemId}
  - name: String
  - quantity: Double
  - unit: String  
  - category: String
  - purchased: Boolean
```

**Local Image Storage:**
```kotlin
object LocalImageStore {
    // Salva imagens no diretório interno (sem permissões)
    fun saveFromContentUri(context: Context, listId: String, uri: String): Boolean
    fun exists(context: Context, listId: String): Boolean
    fun fileForList(context: Context, listId: String): File
}
```

**Lifecycle Safety:**
```kotlin
// Coleta segura de Flow sem vazamentos
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            adapter.submitList(state.filteredLists)
        }
    }
}
```

**Agrupamento de Itens:**
```kotlin
// Converte dados em estrutura para RecyclerView
data class GroupedItems(
    val byCategory: Map<Categoria, List<Item>>,
    val purchased: List<Item>
)

sealed class RowItem {
    data class Header(val categoria: Categoria) : RowItem()
    data class Produto(val item: Item) : RowItem()  
}
```

## 🎥 Roteiro de Demonstração (≤ 6 min)

### 1. Introdução (45s)
- **App completo**: Lista de Compras com autenticação e nuvem
- **Recursos**: Firebase Auth + Firestore + imagens locais
- **Arquitetura**: MVVM + Repository + Flow + Lifecycle safety
- **Compatibilidade**: Funciona online (Firestore) e offline (InMemory)

### 2. Fluxo de Autenticação (1min)
- Tela de login/registro
- Criação de conta nova
- Login com credenciais
- Auth Guard bloqueando acesso
- Logout e redirecionamento

### 3. Gestão de Listas (2min)  
- Criar lista com imagem do dispositivo
- Preview e persistência local da imagem
- Lista salva no Firestore (mostrar no console)
- Busca em tempo real por título
- Edição e exclusão de listas

### 4. Gestão de Itens (1.5min)
- Adicionar itens com categoria
- Dropdown de unidade (não botão)
- Toggle "comprado" em tempo real
- Agrupamento por categoria + seção comprados
- Busca local por nome de item

### 5. Código Destacado (45s)
- **Repository pattern**: RepoProvider escolhendo Firestore/InMemory
- **MVVM**: StateFlow + repeatOnLifecycle
- **Firestore**: observeItems com callbackFlow
- **LocalImageStore**: sem permissões, diretório interno

### 6. Fechamento (15s)
- App production-ready com persistência real
- Arquitetura moderna e escalável
- Código limpo e bem documentado

## 📝 Notas de Desenvolvimento

### **Padrões Aplicados:**
- **MVVM**: Separação clara de responsabilidades
- **Repository**: Abstração de fontes de dados  
- **Dependency Injection**: Via Factory pattern manual
- **Reactive Programming**: Flow + StateFlow + combine
- **Lifecycle Awareness**: repeatOnLifecycle para coletas seguras

### **Boas Práticas:**
- **Null Safety**: Tratamento defensivo em todo código
- **Error Handling**: Try/catch + fallbacks silenciosos
- **Performance**: DiffUtil + ListAdapter + stateIn
- **UX**: Loading states + empty states + feedback visual
- **Maintenance**: Comentários humanos + nomes descritivos

### **Decisões Técnicas:**
- **Sem Firebase Storage**: Imagens ficam locais (requisito do professor)
- **Firestore Subcollections**: lists/{id}/items para organização
- **GetContent vs SAF**: Evita permissões de runtime
- **Manual DI**: Sem Hilt/Dagger para simplicidade acadêmica

## 🚦 Status do Projeto

### **✅ Implementado (Commits 5-8):**
- **Commit 5**: MVVM + Repository + StateFlow
- **Commit 6**: Firebase Auth + Auth Guard
- **Commit 7**: Firestore para listas + LocalImageStore
- **Commit 8**: Firestore para itens + busca/agrupamento

### **🎯 Funcionalidades Completas:**
- Autenticação robusta com Firebase
- Persistência de listas na nuvem 
- Armazenamento local de imagens
- CRUD completo de itens com sincronização
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
cd ProjetoPratico_Mobile12
# Configure Firebase (ver FIREBASE_SETUP.md)
./gradlew assembleDebug
```

### 📱 Para Usuários
1. **Login/Registro**: Firebase Auth com email/senha
2. **Criar listas**: Com título + imagem opcional
3. **Adicionar itens**: Por categoria com toggle comprado
4. **Buscar**: Listas e itens em tempo real
5. **Sincronização**: Dados na nuvem + offline funcional
