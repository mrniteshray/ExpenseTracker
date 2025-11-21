package xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.LoginRequest
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.SignUpRequest
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.remote.ExpenseApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User session data
 */
data class UserSession(
    val uid: String,
    val email: String,
    val idToken: String
)

/**
 * Repository for authentication operations using REST API
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ExpenseApiService,
    @ApplicationContext private val context: Context
) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableStateFlow<UserSession?>(loadSession())
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()
    
    /**
     * Load session from SharedPreferences
     */
    private fun loadSession(): UserSession? {
        val uid = prefs.getString("uid", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        val idToken = prefs.getString("id_token", null) ?: return null
        return UserSession(uid, email, idToken)
    }
    
    /**
     * Save session to SharedPreferences
     */
    private fun saveSession(session: UserSession) {
        prefs.edit()
            .putString("uid", session.uid)
            .putString("email", session.email)
            .putString("id_token", session.idToken)
            .apply()
        _currentUser.value = session
    }
    
    /**
     * Clear session from SharedPreferences
     */
    private fun clearSession() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }
    
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<UserSession> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val session = UserSession(
                    uid = authResponse.uid,
                    email = authResponse.email,
                    idToken = authResponse.idToken
                )
                saveSession(session)
                Result.Success(session)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.Error("Sign in failed: $errorBody")
            }
        } catch (e: Exception) {
            Result.Error("Sign in failed: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): Result<UserSession> {
        return try {
            val response = apiService.signUp(SignUpRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val session = UserSession(
                    uid = authResponse.uid,
                    email = authResponse.email,
                    idToken = authResponse.idToken
                )
                saveSession(session)
                Result.Success(session)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.Error("Sign up failed: $errorBody")
            }
        } catch (e: Exception) {
            Result.Error("Sign up failed: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        clearSession()
    }
    
    /**
     * Get current user synchronously
     */
    fun getCurrentUser(): UserSession? {
        return _currentUser.value
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.uid
    }
    
    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return _currentUser.value != null
    }
}
