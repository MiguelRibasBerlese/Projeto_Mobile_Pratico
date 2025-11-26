# Lista de Compras - App Android

App Android simples para gerenciar listas de compras com itens organizados por categoria.

## 📱 Objetivo

Aplicativo para criar e gerenciar listas de compras, permitindo:
- Criar múltiplas listas com imagens opcionais
- Adicionar itens organizados por categoria
- Marcar itens como comprados
- Buscar listas e itens
- Interface simples e intuitiva

## 🚀 Como Rodar

1. Clone o repositório
2. Abra no Android Studio
3. Sync do Gradle
4. Execute no emulador ou dispositivo

**Requisitos:**
- Android Studio Arctic Fox ou superior
- SDK mínimo: API 24 (Android 7.0)
- SDK alvo: API 34 (Android 14)

## 🏗️ Arquitetura

**Padrão simples para trabalho acadêmico:**
- **Activities + Intents:** Navegação entre telas
- **ViewBinding:** Acesso seguro às views
- **RecyclerView + DiffUtil:** Listas eficientes
- **InMemoryStore (object):** Dados em RAM apenas
- **ViewModels básicos:** Preservação de estado na rotação

**Sem frameworks complexos:** Room, Retrofit, Navigation Component, DataStore, DI.

## 📱 Telas e Fluxos

### 1. Login/Registro
- Login simples com validação
- Cadastro de novos usuários
- Dados salvos apenas em memória

### 2. Home (Listas)
- Lista todas as listas criadas (A-Z)
- FAB para criar nova lista
- SearchView para filtrar listas
- Ações: visualizar, editar, excluir
- Empty state quando não há listas

### 3. Formulário de Lista
- Criar/editar lista
- Nome obrigatório
- Imagem opcional via seletor de arquivos
- Validação simples

### 4. Detalhes da Lista (Itens)
- Itens agrupados por categoria
- Seção "Comprados" separada no final
- SearchView para filtrar itens não comprados
- FAB para adicionar item
- Toggle para marcar/desmarcar comprado

### 5. Formulário de Item
- Nome, quantidade, unidade, categoria
- Validação: nome e quantidade obrigatórios
- Spinner para selecionar categoria
- Edição de itens existentes

## ✅ Requisitos Funcionais Atendidos

**RF-001 - Gerenciar Listas:**
- ✅ Criar, visualizar, editar, excluir listas
- ✅ Imagem opcional nas listas
- ✅ Ordenação A-Z por título

**RF-002 - Gerenciar Itens:**
- ✅ Adicionar itens com nome, quantidade, unidade, categoria
- ✅ Editar e excluir itens
- ✅ Validação de campos obrigatórios

**RF-003 - Organizar por Categoria:**
- ✅ 5 categorias: Alimentos, Bebidas, Higiene, Limpeza, Outros
- ✅ Agrupamento automático nos detalhes da lista
- ✅ Ícones visuais para cada categoria

**RF-004 - Marcar Comprados:**
- ✅ Toggle individual para cada item
- ✅ Seção "Comprados" separada
- ✅ Feedback visual (Snackbar)

**RF-005 - Buscar:**
- ✅ Busca em listas por título (case-insensitive)
- ✅ Busca em itens por nome (apenas não comprados)
- ✅ Filtros mantêm ordenação e agrupamento

## 🛠️ Implementação Técnica

**Dados em Memória:**
```kotlin
object InMemoryStore {
    val users = mutableListOf<User>()
    val listas = mutableListOf<ShoppingList>()
    var currentUser: User? = null
}
```

**Agrupamento de Itens:**
```kotlin
// Agrupa por categoria, depois por comprados
fun buildDataWithComprados(itens: List<Item>): List<RowItem>
```

**DiffUtil nos Adapters:**
```kotlin
// Evita flickering nas listas
object DiffCallback : DiffUtil.ItemCallback<T>()
```

**ViewBinding em todas as Activities:**
```kotlin
private lateinit var binding: ActivityHomeBinding
binding = ActivityHomeBinding.inflate(layoutInflater)
```

## 🎥 Roteiro de Vídeo (≤ 5 min)

### 1. Introdução (30s)
- Apresentação do app Lista de Compras
- Objetivos: organizar compras por categoria
- Tecnologias: Android Kotlin, Activities, ViewBinding

### 2. Demonstração do Fluxo (3min)
- Login simples
- Criar lista com imagem
- Adicionar itens por categoria
- Marcar como comprado
- Buscar listas e itens
- Editar/excluir funcionalidades

### 3. Código Principal (1min)
- InMemoryStore para dados
- ItensAdapter com sealed class RowItem
- Agrupamento por categoria em GroupingUtils
- DiffUtil para performance

### 4. Requisitos da Rubrica (20s)
- 5 RFs implementados
- Interface intuitiva
- Dados em memória (sem persistência)
- Navegação por Activities + Intents

### 5. Fechamento (10s)
- App funcional para gerenciar compras
- Código organizado e documentado
- Pronto para uso acadêmico

**Link do Vídeo:** [link-do-video]

## 📝 Notas de Desenvolvimento

- Comentários em PT-BR para facilitar compreensão
- Validações simples com feedback via Toast/Snackbar
- Empty states para melhor UX
- Tratamento básico de rotação com ViewModels
- Sem overengineering - foco na simplicidade

## 🔧 Próximos Passos (Possíveis Melhorias)

- Persistência com Room Database
- Sincronização com API REST
- Notificações de lembrete
- Compartilhamento de listas
- Backup e restauração
- Temas claro/escuro
