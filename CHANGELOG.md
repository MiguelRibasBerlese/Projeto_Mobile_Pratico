# Changelog - Lista de Compras

Todas as mudanças importantes do projeto são documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto segue [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Planejado
- Testes unitários para ViewModels
- Testes de integração para Repositories  
- CI/CD pipeline
- Crashlytics integration

## [2.0.0] - 2025-01-26

### ✨ Added
- **Arquitetura MVVM completa** com Repository Pattern
- **Firebase Authentication** (email/senha, recuperação, logout)
- **Persistência Firestore** para listas e itens na nuvem
- **Armazenamento local** de imagens das listas (sem Firebase Storage)
- **Busca em tempo real** por listas e itens (case-insensitive)
- **Agrupamento inteligente** de itens por categoria + seção comprados
- **Toggle purchased** em tempo real com sincronização instantânea
- **Modo offline** funcional (InMemory quando não logado)
- **Lifecycle safety** com repeatOnLifecycle + StateFlow
- **Auth Guard** bloqueando acesso sem autenticação

### 🔄 Changed
- **HomeActivity** migrada de InMemoryStore para MVVM + Repository
- **ListDetailActivity** refatorada com ItemListViewModel + Flow reativo
- **Dropdown de unidade** em vez de botão (correção UX)
- **Adapter pattern** otimizado com DiffUtil + sealed classes
- **Image selection** usando GetContent() (substitui APIs deprecadas)
- **Error handling** defensivo em todos os repositórios

### 🚫 Removed
- APIs deprecadas (onActivityResult, startActivityForResult, resources.getColor)
- Dependência direta de InMemoryStore nas Activities
- Código manual de reload de listas (substituído por Flow automático)

### 🔧 Technical Changes
- **Dependencies**: Firebase BOM 33.7.0, Coroutines 1.7.3, Lifecycle 2.8.6
- **Package structure**: Organizados por feature (auth/, firebase/, listdetail/)  
- **Repository abstractions**: Interfaces limpas com implementações Firestore/InMemory
- **ViewModelFactory**: Injeção manual de dependências
- **Flow operators**: combine(), stateIn(), callbackFlow para reatividade

### 📋 Commits Breakdown

#### Commit 5 - MVVM Foundation
- ListViewModel + HomeViewModel com StateFlow
- ListRepository interface + InMemoryListRepository  
- ListViewModelFactory para injeção de dependências
- HomeActivity integrada com lifecycle-safe Flow collection
- Busca reativa via combine(observeLists, query)

#### Commit 6 - Firebase Authentication
- AuthRepository com signup/signin/recover usando tasks.await()
- AuthViewModel + AuthVMFactory para LoginActivity
- Auth Guard na HomeActivity (onStart)
- LoginActivity completa com validação e navegação
- Logout functionality com flags CLEAR_TASK

#### Commit 7 - Firestore Lists + Local Images
- FirestoreListRepository persistindo title/ownerUid na nuvem
- LocalImageStore salvando imagens no diretório interno (sem permissões)
- RepoProvider escolhendo repo baseado no auth state
- ListaComprasAdapter atualizado para arquivo local em vez de content:// URI
- GetContent() substituindo startActivityForResult

#### Commit 8 - Firestore Items + Search/Grouping  
- FirestoreItemRepository com subcoleção lists/{listId}/items
- ItemListViewModel com busca + agrupamento por categoria
- AdapterDataConverter para RowItem (Header/Produto)
- Toggle purchased em tempo real via Flow
- ListDetailActivity completamente migrada para MVVM

## [1.0.0] - 2024-12-01 (Fase 1 - Baseline)

### ✨ Added
- **Estrutura base** do projeto Android com ViewBinding
- **InMemoryStore** para dados temporários em RAM
- **Models**: ShoppingList, Item, Categoria, User
- **Activities**: Home, ListForm, ListDetail, ItemForm, Login básico
- **Adapters**: ListaComprasAdapter, ItensAdapter com DiffUtil
- **UI Components**: SearchView, FAB, RecyclerView, Empty States

### 📱 Features Implementadas
- **RF-001**: CRUD de listas com imagem opcional
- **RF-002**: CRUD de itens com nome/quantidade/unidade/categoria
- **RF-003**: Agrupamento por categoria (5 categorias + ícones)
- **RF-004**: Toggle de item comprado com seção separada
- **RF-005**: Busca por título de lista e nome de item

### 🏗️ Architecture
- **Pattern**: Activities + Intents (navegação simples)
- **Data**: Singleton InMemoryStore (dados volateis)
- **UI**: ViewBinding + RecyclerView + DiffUtil
- **Validation**: Toast/Snackbar para feedback

### 📦 Dependencies
```kotlin
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("com.google.android.material:material:1.12.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
```

## [0.1.0] - 2024-11-15 (Setup Inicial)

### ✨ Added
- Projeto Android criado com Kotlin + ViewBinding
- Configuração Gradle com libs.versions.toml
- Estrutura básica de pastas
- README inicial
- .gitignore configurado

---

## 📈 Evolução da Arquitetura

### v1.0 → v2.0 Migration Path:

| Aspecto | v1.0 (Fase 1) | v2.0 (Fase 2) |
|---------|----------------|----------------|
| **Data** | InMemoryStore singleton | Repository Pattern + Firestore |
| **Auth** | Login básico mockado | Firebase Auth completo |
| **State** | Manual reload + LiveData | StateFlow + Flow reativo |
| **Images** | content:// URI direto | LocalImageStore + GetContent |
| **Navigation** | Intents simples | Intents + Auth Guard |
| **Lifecycle** | Basic ViewModel | repeatOnLifecycle safety |
| **Architecture** | MVP/MVC híbrido | MVVM puro |

### Breaking Changes v1 → v2:
- **InMemoryStore** não é mais acessado diretamente pelas Activities
- **Repository abstractions** requerem Factory pattern para ViewModels
- **Firebase setup** é obrigatório para funcionalidade completa
- **Flow/StateFlow** substitui LiveData/callback manual
- **Auth requirement** para persistência (offline fallback disponível)

### Migration Benefits:
- ✅ **Scalability**: Fácil adicionar novas features/data sources
- ✅ **Testability**: Repository interfaces permitem mocking  
- ✅ **Maintainability**: Separation of concerns clara
- ✅ **Performance**: Flow + DiffUtil + lifecycle optimization
- ✅ **Real-world ready**: Padrões de produção implementados

## 🎯 Roadmap

### v2.1 - Testing & Quality
- [ ] Unit tests para ViewModels (MockK + Coroutines Test)
- [ ] Integration tests para Repositories (Firebase Test SDK)
- [ ] UI tests com Espresso
- [ ] Detekt + ktlint para code quality
- [ ] GitHub Actions CI/CD

### v2.2 - UX Improvements  
- [ ] Dark theme support
- [ ] Swipe-to-delete gestures
- [ ] Pull-to-refresh
- [ ] Skeleton loading states
- [ ] Accessibility improvements

### v2.3 - Advanced Features
- [ ] Offline-first com Room + sync
- [ ] Push notifications (reminders)
- [ ] Lista colaborativa (share)
- [ ] Export/import (JSON/CSV)
- [ ] Analytics & Crashlytics

### v3.0 - Architecture Evolution
- [ ] Multi-module architecture
- [ ] Jetpack Compose migration
- [ ] Kotlin Multiplatform shared logic
- [ ] Dependency Injection (Hilt)
- [ ] Clean Architecture layers

---

## 📝 Contribution Guidelines

### Commit Message Convention:
```
type(scope): description

feat(auth): add Firebase Auth integration
fix(ui): resolve image loading on rotation  
docs(readme): update architecture section
refactor(repo): extract FirestoreRepository interface
test(vm): add HomeViewModel unit tests
```

### Branch Strategy:
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/auth-integration`: Feature branches
- `hotfix/critical-bug`: Emergency fixes

### Code Review Checklist:
- [ ] Null safety handled
- [ ] Lifecycle awareness implemented
- [ ] Error states covered  
- [ ] Performance considerations
- [ ] Documentation updated
