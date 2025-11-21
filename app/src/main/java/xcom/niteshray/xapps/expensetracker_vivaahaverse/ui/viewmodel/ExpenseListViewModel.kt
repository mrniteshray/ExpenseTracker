package xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.Expense
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.ExpenseRepository
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.Result
import javax.inject.Inject

/**
 * UI State for expense list
 */
data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel for managing expense list
 */
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()
    
    init {
        loadExpenses()
    }
    
    /**
     * Load all expenses
     */
    fun loadExpenses(category: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = expenseRepository.listExpenses(category = category)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            expenses = result.data,
                            isLoading = false,
                            selectedCategory = category,
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
     * Refresh expenses
     */
    fun refreshExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            
            when (val result = expenseRepository.listExpenses(category = _uiState.value.selectedCategory)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            expenses = result.data,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Delete an expense
     */
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            when (val result = expenseRepository.deleteExpense(expenseId)) {
                is Result.Success -> {
                    // Remove from local list
                    _uiState.update {
                        it.copy(
                            expenses = it.expenses.filter { expense -> expense.id != expenseId }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(error = result.message)
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Filter by category
     */
    fun filterByCategory(category: String?) {
        loadExpenses(category)
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
