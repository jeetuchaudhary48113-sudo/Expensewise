package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Expense
import com.example.ui.ExpenseViewModel
import com.example.ui.components.ExpenseFormDialog
import com.example.ui.screens.AllExpensesScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.collectLatest

enum class Screen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    ALL_EXPENSES("Expenses", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_expenses"),
    REPORTS("Reports", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_reports"),
    BUDGETS("Budgets", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_budgets"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themePreference = themeMode) {
                ExpenseWiseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExpenseWiseApp(viewModel: ExpenseViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    val allCategories by viewModel.allCategories.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showExpenseForm by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }

    // Collect snackbar events
    LaunchedEffect(Unit) {
        viewModel.snackBarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle back button: if not on DASHBOARD, go back to DASHBOARD
    BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
        currentScreen = Screen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                Screen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingExpense = null
                    showExpenseForm = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("main_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAllExpenses = { currentScreen = Screen.ALL_EXPENSES },
                    onNavigateToReports = { currentScreen = Screen.REPORTS },
                    onOpenAddExpense = {
                        editingExpense = null
                        showExpenseForm = true
                    },
                    onEditExpense = { exp ->
                        editingExpense = exp
                        showExpenseForm = true
                    },
                    onDeleteExpense = { exp ->
                        deletingExpense = exp
                    }
                )
                Screen.ALL_EXPENSES -> AllExpensesScreen(
                    viewModel = viewModel,
                    onEditExpense = { exp ->
                        editingExpense = exp
                        showExpenseForm = true
                    },
                    onDeleteExpense = { exp ->
                        deletingExpense = exp
                    },
                    onAddExpense = {
                        editingExpense = null
                        showExpenseForm = true
                    }
                )
                Screen.REPORTS -> ReportsScreen(
                    viewModel = viewModel,
                    onEditExpense = { exp: Expense ->
                        editingExpense = exp
                        showExpenseForm = true
                    },
                    onDeleteExpense = { exp: Expense ->
                        deletingExpense = exp
                    }
                )
                Screen.BUDGETS -> BudgetScreen(viewModel = viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Expense Form Dialog (Add / Edit)
    if (showExpenseForm) {
        ExpenseFormDialog(
            initialExpense = editingExpense,
            categories = allCategories,
            currencySymbol = currencySymbol,
            onDismiss = {
                showExpenseForm = false
                editingExpense = null
            },
            onSave = { expense ->
                if (editingExpense == null) {
                    viewModel.addExpense(expense)
                } else {
                    viewModel.updateExpense(expense)
                }
                showExpenseForm = false
                editingExpense = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingExpense != null) {
        val exp = deletingExpense!!
        AlertDialog(
            onDismissRequest = { deletingExpense = null },
            title = { Text("Delete Expense?") },
            text = {
                Text(
                    "Are you sure you want to delete '${exp.title}' (${CurrencyFormatter.format(exp.amount, currencySymbol)})? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(exp)
                        deletingExpense = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingExpense = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
