package xcom.niteshray.xapps.expensetracker_vivaahaverse.data.remote

import retrofit2.Response
import retrofit2.http.*
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.*

/**
 * Retrofit API service for expense tracking
 */
interface ExpenseApiService {
    
    // Auth endpoints
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    // Expense endpoints
    @POST("expenses")
    suspend fun createExpense(@Body expense: ExpenseCreateRequest): Response<Expense>
    
    @GET("expenses/{id}")
    suspend fun getExpense(
        @Path("id") expenseId: String,
        @Query("user_id") userId: String
    ): Response<Expense>
    
    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") expenseId: String,
        @Query("user_id") userId: String,
        @Body expense: ExpenseUpdateRequest
    ): Response<Expense>
    
    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") expenseId: String,
        @Query("user_id") userId: String
    ): Response<ApiResponse<Any>>
    
    @GET("expenses")
    suspend fun listExpenses(
        @Query("user_id") userId: String,
        @Query("category") category: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<List<Expense>>
    
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Query("user_id") userId: String
    ): Response<DashboardSummary>
}
