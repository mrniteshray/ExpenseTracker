package xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sign up request
 */
@Serializable
data class SignUpRequest(
    @SerialName("email")
    val email: String,
    
    @SerialName("password")
    val password: String
)

/**
 * Login request
 */
@Serializable
data class LoginRequest(
    @SerialName("email")
    val email: String,
    
    @SerialName("password")
    val password: String
)

/**
 * Auth response
 */
@Serializable
data class AuthResponse(
    @SerialName("uid")
    val uid: String,
    
    @SerialName("email")
    val email: String,
    
    @SerialName("id_token")
    val idToken: String
)

/**
 * API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("data")
    val data: T? = null
)

/**
 * Category summary for dashboard
 */
@Serializable
data class CategorySummary(
    @SerialName("category")
    val category: String,
    
    @SerialName("total_amount")
    val totalAmount: Double,
    
    @SerialName("count")
    val count: Int
)

/**
 * Dashboard summary response
 */
@Serializable
data class DashboardSummary(
    @SerialName("overall_total")
    val overallTotal: Double,
    
    @SerialName("total_count")
    val totalCount: Int,
    
    @SerialName("per_category")
    val perCategory: List<CategorySummary>
)

/**
 * Error response from API
 */
@Serializable
data class ErrorResponse(
    @SerialName("detail")
    val detail: String
)
