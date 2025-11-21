package xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.Expense
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.ExpenseCreateRequest
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.ExpenseUpdateRequest
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.AuthRepository
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.ExpenseRepository
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.Result
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * UI State for add/edit expense
 */
data class ExpenseFormUiState(
    val isEditMode: Boolean = false,
    val expenseId: String? = null,
    val amount: String = "",
    val description: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val category: String = "Food",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val isSaved: Boolean = false
)

/**
 * ViewModel for adding and editing expenses
 */
@HiltViewModel
class ExpenseFormViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val expenseId: String? = savedStateHandle["expenseId"]
    
    private val _uiState = MutableStateFlow(
        ExpenseFormUiState(
            isEditMode = expenseId != null,
            expenseId = expenseId
        )
    )
    val uiState: StateFlow<ExpenseFormUiState> = _uiState.asStateFlow()
    
    init {
        if (expenseId != null) {
            loadExpense(expenseId)
        }
    }
    
    /**
     * Load expense for editing
     */
    private fun loadExpense(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = expenseRepository.getExpense(id)) {
                is Result.Success -> {
                    val expense = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            amount = expense.amount.toString(),
                            description = expense.description,
                            date = LocalDateTime.parse(expense.date, DateTimeFormatter.ISO_DATE_TIME),
                            category = expense.category,
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
     * Update amount field
     */
    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, validationErrors = it.validationErrors - "amount") }
    }
    
    /**
     * Update description field
     */
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, validationErrors = it.validationErrors - "description") }
    }
    
    /**
     * Update date field
     */
    fun updateDate(date: LocalDateTime) {
        _uiState.update { it.copy(date = date) }
    }
    
    /**
     * Update category field
     */
    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }
    
    /**
     * Validate form
     */
    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        val state = _uiState.value
        
        // Validate amount
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            errors["amount"] = "Please enter a valid amount greater than 0"
        }
        
        // Validate description
        if (state.description.isBlank()) {
            errors["description"] = "Description is required"
        } else if (state.description.length > 500) {
            errors["description"] = "Description is too long (max 500 characters)"
        }
        
        _uiState.update { it.copy(validationErrors = errors) }
        return errors.isEmpty()
    }
    
    /**
     * Save expense (create or update)
     */
    fun saveExpense() {
        if (!validateForm()) {
            return
        }
        
        val userId = authRepository.getCurrentUserId() ?: run {
            _uiState.update { it.copy(error = "User not authenticated") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val state = _uiState.value
            val amount = state.amount.toDouble()
            val dateString = state.date.format(DateTimeFormatter.ISO_DATE_TIME)
            
            val result = if (state.isEditMode && state.expenseId != null) {
                // Update existing expense
                val updateRequest = ExpenseUpdateRequest(
                    amount = amount,
                    description = state.description,
                    date = dateString,
                    category = state.category
                )
                expenseRepository.updateExpense(state.expenseId, updateRequest)
            } else {
                // Create new expense
                val createRequest = ExpenseCreateRequest(
                    amount = amount,
                    description = state.description,
                    date = dateString,
                    category = state.category,
                    userId = userId
                )
                expenseRepository.createExpense(createRequest)
            }
            
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Reset saved state
     */
    fun resetSavedState() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
