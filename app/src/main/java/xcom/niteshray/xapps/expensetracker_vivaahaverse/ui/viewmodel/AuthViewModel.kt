package xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.AuthRepository
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.Result
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.UserSession
import javax.inject.Inject

/**
 * UI State for authentication
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val user: UserSession? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

/**
 * ViewModel for authentication operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        isAuthenticated = user != null
                    )
                }
            }
        }
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        if (!validateEmail(email)) {
            _uiState.update { it.copy(error = "Invalid email address") }
            return
        }
        
        if (!validatePassword(password)) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            isAuthenticated = true,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Sign up with email and password
     */
    fun signUp(email: String, password: String, confirmPassword: String) {
        if (!validateEmail(email)) {
            _uiState.update { it.copy(error = "Invalid email address") }
            return
        }
        
        if (!validatePassword(password)) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.signUp(email, password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            isAuthenticated = true,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        authRepository.signOut()
        _uiState.update {
            AuthUiState(
                isAuthenticated = false,
                user = null
            )
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Validate email format
     */
    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate password strength
     */
    private fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }
}
