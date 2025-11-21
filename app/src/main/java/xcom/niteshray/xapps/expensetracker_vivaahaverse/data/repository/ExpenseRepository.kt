package xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository

import retrofit2.Response
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.*
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.remote.ExpenseApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result wrapper for API operations
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Repository for expense operations
 * Handles communication between the app and the backend API
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val apiService: ExpenseApiService,
    private val authRepository: AuthRepository
) {
    
    /**
     * Get the current user ID
     */
    private fun getUserId(): String? {
        return authRepository.getCurrentUserId()
    }
    
    /**
     * Create a new expense
     */
    suspend fun createExpense(expense: ExpenseCreateRequest): Result<Expense> {
        return try {
            val userId = getUserId() ?: return Result.Error("Not authenticated")
            val response = apiService.createExpense(expense)
            handleResponse(response)
        } catch (e: Exception) {
            Result.Error("Failed to create expense: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Get a specific expense
     */
    suspend fun getExpense(expenseId: String): Result<Expense> {
        return try {
            val userId = getUserId() ?: return Result.Error("Not authenticated")
            val response = apiService.getExpense(expenseId, userId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.Error("Failed to get expense: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Update an existing expense
     */
    suspend fun updateExpense(
        expenseId: String,
        expense: ExpenseUpdateRequest
    ): Result<Expense> {
        return try {
            val userId = getUserId() ?: return Result.Error("Not authenticated")
            val response = apiService.updateExpense(expenseId, userId, expense)
            handleResponse(response)
        } catch (e: Exception) {
            Result.Error("Failed to update expense: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Delete an expense
     */
    suspend fun deleteExpense(expenseId: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.Error("Not authenticated")
            val response = apiService.deleteExpense(expenseId, userId)
            if (response.isSuccessful) {
                Result.Success(true)
            } else {
                Result.Error("Failed to delete expense: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Failed to delete expense: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * List all expenses with optional filters
     */
    suspend fun listExpenses(
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<Expense>> {
        return try {
            val userId = getUserId() ?: return Result.Error("Not authenticated")
            val response = apiService.listExpenses(userId, category, startDate, endDate)
            handleResponse(response)
        } catch (e: Exception) {
            Result.Error("Failed to list expenses: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Get dashboard summary
     */
    suspend fun getDashboardSummary(): Result<DashboardSummary> {
        return try {
            val userId = getUserId() ?: return Result.Error("Not authenticated")
            val response = apiService.getDashboardSummary(userId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.Error("Failed to get dashboard summary: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Handle API response and convert to Result
     */
    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it)
            } ?: Result.Error("Empty response from server")
        } else {
            val errorMessage = try {
                response.errorBody()?.string() ?: "Unknown error"
            } catch (e: Exception) {
                "Failed to parse error: ${response.message()}"
            }
            Result.Error(errorMessage)
        }
    }
}
