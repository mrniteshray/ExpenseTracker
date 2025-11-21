package xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing an expense
 */
@Serializable
data class Expense(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("amount")
    val amount: Double,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("date")
    val date: String, // ISO 8601 format
    
    @SerialName("category")
    val category: String,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request model for creating an expense
 */
@Serializable
data class ExpenseCreateRequest(
    @SerialName("amount")
    val amount: Double,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("date")
    val date: String, // ISO 8601 format
    
    @SerialName("category")
    val category: String,
    
    @SerialName("user_id")
    val userId: String
)

/**
 * Request model for updating an expense
 */
@Serializable
data class ExpenseUpdateRequest(
    @SerialName("amount")
    val amount: Double? = null,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("date")
    val date: String? = null,
    
    @SerialName("category")
    val category: String? = null
)

/**
 * Expense categories enum
 */
enum class ExpenseCategory(val displayName: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    UTILITIES("Utilities"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    OTHER("Other");
    
    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
        
        fun getAllCategories(): List<String> {
            return entries.map { it.displayName }
        }
    }
}
