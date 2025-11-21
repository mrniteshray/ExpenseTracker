package xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.CategorySummary
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.DashboardSummary
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.ExpenseRepository
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.repository.Result
import javax.inject.Inject

/**
 * UI State for dashboard
 */
data class DashboardUiState(
    val overallTotal: Double = 0.0,
    val totalCount: Int = 0,
    val categoryBreakdown: List<CategorySummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel for dashboard and analytics
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    /**
     * Load dashboard summary data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = expenseRepository.getDashboardSummary()) {
                is Result.Success -> {
                    val summary = result.data
                    _uiState.update {
                        it.copy(
                            overallTotal = summary.overallTotal,
                            totalCount = summary.totalCount,
                            categoryBreakdown = summary.perCategory,
                            isLoading = false,
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
     * Refresh dashboard data
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            
            when (val result = expenseRepository.getDashboardSummary()) {
                is Result.Success -> {
                    val summary = result.data
                    _uiState.update {
                        it.copy(
                            overallTotal = summary.overallTotal,
                            totalCount = summary.totalCount,
                            categoryBreakdown = summary.perCategory,
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
     * Get category percentage
     */
    fun getCategoryPercentage(categoryTotal: Double): Float {
        val total = _uiState.value.overallTotal
        return if (total > 0) {
            ((categoryTotal / total) * 100).toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
