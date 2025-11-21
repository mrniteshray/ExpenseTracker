package xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.screens.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.Expense
import xcom.niteshray.xapps.expensetracker_vivaahaverse.data.model.ExpenseCategory
import xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.viewmodel.AuthViewModel
import xcom.niteshray.xapps.expensetracker_vivaahaverse.ui.viewmodel.ExpenseListViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCategoryFilter by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Reload expenses when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Expenses",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showCategoryFilter = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Dashboard",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add expense", modifier = Modifier.size(28.dp))
            }
        },
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.expenses.isEmpty() -> {
                    EmptyStateMessage(
                        modifier = Modifier.align(Alignment.Center),
                        hasFilter = uiState.selectedCategory != null
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Show filter chip if active
                        if (uiState.selectedCategory != null) {
                            item {
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.filterByCategory(null) },
                                    label = { 
                                        Text(
                                            uiState.selectedCategory!!,
                                            fontWeight = FontWeight.SemiBold
                                        ) 
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            "Clear filter",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = true,
                                        borderColor = MaterialTheme.colorScheme.primary,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 1.dp
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                        
                        items(
                            items = uiState.expenses,
                            key = { it.id }
                        ) { expense ->
                            ExpenseItem(
                                expense = expense,
                                onEdit = { onNavigateToEditExpense(expense.id) },
                                onDelete = { expenseToDelete = expense }
                            )
                        }
                        
                        // Bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Category filter dialog
    if (showCategoryFilter) {
        CategoryFilterDialog(
            onDismiss = { showCategoryFilter = false },
            onCategorySelected = { category ->
                viewModel.filterByCategory(category)
                showCategoryFilter = false
            }
        )
    }
    
    // Delete confirmation dialog
    expenseToDelete?.let { expense ->
        DeleteConfirmationDialog(
            expenseDescription = expense.description,
            onConfirm = {
                viewModel.deleteExpense(expense.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                authViewModel.signOut()
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val categoryColor = getCategoryColor(expense.category)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryEmoji(expense.category),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${String.format("%.2f", expense.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryColor
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Edit",
                                    fontWeight = FontWeight.Medium
                                ) 
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    "Edit",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Delete",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                ) 
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(
    modifier: Modifier = Modifier,
    hasFilter: Boolean = false
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasFilter) Icons.Default.FilterList else Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (hasFilter) "No Matching Expenses" else "No Expenses Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = if (hasFilter) 
                "Try adjusting your filter to see more results" 
            else 
                "Tap the + button to add your first expense",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryFilterDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.FilterList,
                "Filter",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { 
            Text(
                "Filter by Category",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledTonalButton(
                    onClick = { onCategorySelected(null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "All Categories",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExpenseCategory.getAllCategories().forEach { category ->
                    OutlinedButton(
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(getCategoryColor(category))
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getCategoryEmoji(category),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                category,
                                fontWeight = FontWeight.Medium,
                                color = getCategoryColor(category)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun DeleteConfirmationDialog(
    expenseDescription: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                "Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { 
            Text(
                "Delete Expense?",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = { 
            Text(
                "Are you sure you want to delete \"$expenseDescription\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Delete",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category.lowercase()) {
        "food" -> MaterialTheme.colorScheme.tertiary
        "transport" -> MaterialTheme.colorScheme.primary
        "entertainment" -> MaterialTheme.colorScheme.secondary
        "shopping" -> androidx.compose.ui.graphics.Color(0xFFFF9500)
        "bills" -> androidx.compose.ui.graphics.Color(0xFF0A84FF)
        "healthcare" -> androidx.compose.ui.graphics.Color(0xFFFF3B30)
        "education" -> androidx.compose.ui.graphics.Color(0xFF5856D6)
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food" -> "🍔"
        "transport" -> "🚗"
        "entertainment" -> "🎬"
        "shopping" -> "🛍️"
        "bills" -> "💳"
        "healthcare" -> "🏥"
        "education" -> "📚"
        "other" -> "📦"
        else -> "💰"
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                "Logout",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { 
            Text(
                "Logout?",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = { 
            Text(
                "Are you sure you want to logout? You'll need to sign in again to access your expenses.",
                style = MaterialTheme.typography.bodyLarge
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Logout",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}