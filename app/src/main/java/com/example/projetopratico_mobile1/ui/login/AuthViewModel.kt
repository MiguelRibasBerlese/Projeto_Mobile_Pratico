package com.example.projetopratico_mobile1.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.projetopratico_mobile1.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State do Auth - loading, error e UID do usuário logado
 */
data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val uid: String? = null
)

/**
 * ViewModel para autenticação - gerencia estado de login/cadastro
 */
class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Verifica se já tem usuário logado na inicialização
        val currentUid = repo.currentUid()
        if (currentUid != null) {
            _state.value = AuthState(uid = currentUid)
        }
    }

    /**
     * Login com email/senha
     */
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState(error = "Email e senha são obrigatórios")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)

            repo.signIn(email, password)
                .onSuccess { uid ->
                    _state.value = AuthState(uid = uid)
                }
                .onFailure { exception ->
                    _state.value = AuthState(error = getErrorMessage(exception))
                }
        }
    }

    /**
     * Cadastro com email/senha
     */
    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState(error = "Email e senha são obrigatórios")
            return
        }

        if (password.length < 6) {
            _state.value = AuthState(error = "Senha deve ter pelo menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)

            repo.signUp(email, password)
                .onSuccess { uid ->
                    _state.value = AuthState(uid = uid)
                }
                .onFailure { exception ->
                    _state.value = AuthState(error = getErrorMessage(exception))
                }
        }
    }

    /**
     * Recuperar senha por email
     */
    fun recover(email: String) {
        if (email.isBlank()) {
            _state.value = AuthState(error = "Email é obrigatório")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)

            repo.recover(email)
                .onSuccess {
                    _state.value = AuthState(error = "Email de recuperação enviado!")
                }
                .onFailure { exception ->
                    _state.value = AuthState(error = getErrorMessage(exception))
                }
        }
    }

    /**
     * Logout
     */
    fun signOut() {
        repo.signOut()
        _state.value = AuthState()
    }

    /**
     * Limpar erro atual
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * Converte exceções do Firebase em mensagens user-friendly
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Email inválido"
            "The password is invalid or the user does not have a password." -> "Senha incorreta"
            "There is no user record corresponding to this identifier." -> "Usuário não encontrado"
            "The email address is already in use by another account." -> "Este email já está em uso"
            "The password is too weak." -> "Senha muito fraca"
            else -> exception.message ?: "Erro desconhecido"
        }
    }
}

/**
 * Factory para criar AuthViewModel com dependência
 */
class AuthVMFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
