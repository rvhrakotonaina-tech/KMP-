package com.example.moneytracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import com.example.moneytracker.ui.screen.LoansDebtsScreen
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flowOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.moneytracker.R
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.ui.navigation.Route
import com.example.moneytracker.ui.screen.ChatScreen
import com.example.moneytracker.ui.screen.DashboardScreen
import com.example.moneytracker.ui.screen.SettingsScreen
import com.example.moneytracker.ui.screen.TransactionDetailScreen
import androidx.compose.ui.platform.LocalContext
import com.example.moneytracker.ui.screen.OnboardingScreen
import com.example.moneytracker.ui.screen.TransactionListScreen
import com.example.moneytracker.ui.screen.UpdateDialog

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MoneyTrackerApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val isSampleDataActive by viewModel.isSampleDataActive.collectAsStateWithLifecycle()
    val availableUpdate by viewModel.availableUpdate.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(context)
    }

    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            onGetStarted = { viewModel.completeOnboarding() },
            onExploreSampleData = { viewModel.loadSampleData() }
        )
        return
    }

    val backStack = rememberNavBackStack(Route.Dashboard as NavKey)
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    
    val navigator = rememberListDetailPaneScaffoldNavigator<Route>()

    val currentKey = backStack.lastOrNull()

    // Sync navigator with backstack
    LaunchedEffect(currentKey) {
        if (currentKey is Route.TransactionDetail) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, currentKey)
        } else {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    val isDashboard = currentKey is Route.Dashboard
    val isTransactions = currentKey is Route.TransactionList || currentKey is Route.TransactionDetail
    val isLoansDebts = currentKey is Route.LoansDebts
    val isSettings = currentKey is Route.Settings

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        val navigateToTopLevel = { target: Route ->
            backStack.clear()
            backStack.add(target)
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationRailItem(
                        selected = isDashboard,
                        onClick = {
                            if (!isDashboard) {
                                navigateToTopLevel(Route.Dashboard)
                            }
                        },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = stringResource(R.string.dashboard)) },
                        label = { NavLabel(stringResource(R.string.dashboard)) }
                    )
                    NavigationRailItem(
                        selected = isTransactions,
                        onClick = {
                            if (!isTransactions) {
                                navigateToTopLevel(Route.TransactionList)
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = stringResource(R.string.transactions)) },
                        label = { NavLabel(stringResource(R.string.transactions)) }
                    )
                    NavigationRailItem(
                        selected = isLoansDebts,
                        onClick = {
                            if (!isLoansDebts) {
                                navigateToTopLevel(Route.LoansDebts)
                            }
                        },
                        icon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = stringResource(R.string.loans_and_debts)) },
                        label = { NavLabel(stringResource(R.string.loans_and_debts)) }
                    )
                    NavigationRailItem(
                        selected = isSettings,
                        onClick = {
                            if (!isSettings) {
                                navigateToTopLevel(Route.Settings)
                            }
                        },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings)) },
                        label = { NavLabel(stringResource(R.string.settings)) }
                    )
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = if (currentKey == Route.Chat) {
                    WindowInsets(0, 0, 0, 0)
                } else {
                    ScaffoldDefaults.contentWindowInsets
                },
                bottomBar = {
                    if (!useNavRail && currentKey != Route.Chat) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationBarItem(
                                selected = isDashboard,
                                onClick = {
                                    if (!isDashboard) {
                                        navigateToTopLevel(Route.Dashboard)
                                    }
                                },
                                icon = { Icon(Icons.Rounded.Analytics, contentDescription = stringResource(R.string.dashboard)) },
                                label = { NavLabel(stringResource(R.string.dashboard)) }
                            )
                            NavigationBarItem(
                                selected = isTransactions,
                                onClick = {
                                    if (!isTransactions) {
                                        navigateToTopLevel(Route.TransactionList)
                                    }
                                },
                                icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = stringResource(R.string.transactions)) },
                                label = { NavLabel(stringResource(R.string.transactions)) }
                            )
                            NavigationBarItem(
                                selected = isLoansDebts,
                                onClick = {
                                    if (!isLoansDebts) {
                                        navigateToTopLevel(Route.LoansDebts)
                                    }
                                },
                                icon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = stringResource(R.string.loans_and_debts)) },
                                label = { NavLabel(stringResource(R.string.loans_and_debts)) }
                            )
                            NavigationBarItem(
                                selected = isSettings,
                                onClick = {
                                    if (!isSettings) {
                                        navigateToTopLevel(Route.Settings)
                                    }
                                },
                                icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings)) },
                                label = { NavLabel(stringResource(R.string.settings)) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    if (isDashboard) {
                        val advice by viewModel.dashboardAdvice.collectAsStateWithLifecycle()
                        val isAiEnabled by viewModel.isAiEnabled.collectAsStateWithLifecycle()
                        val netBalance by viewModel.netBalance.collectAsStateWithLifecycle()
                        val loansDebts by viewModel.loansDebtsWithRepayments.collectAsStateWithLifecycle()
                        
                        DashboardScreen(
                            transactions = transactions, 
                            currencySymbol = currencySymbol,
                            isAiEnabled = isAiEnabled,
                            advice = advice,
                            netBalance = netBalance,
                            loansDebts = loansDebts,
                            isSampleDataActive = isSampleDataActive,
                            onClearSampleData = { viewModel.clearSampleData() },
                            onChatClick = { backStack.add(Route.Chat) }
                        )
                    } else if (isLoansDebts) {
                        LoansDebtsScreen(viewModel = viewModel)
                    } else if (isSettings) {
                        SettingsScreen(viewModel = viewModel)
                    } else if (currentKey is Route.Chat) {
                        ChatScreen(
                            viewModel = viewModel,
                            onBackClick = { backStack.removeAt(backStack.size - 1) }
                        )
                    } else {
                        ListDetailPaneScaffold(
                            directive = navigator.scaffoldDirective,
                            value = navigator.scaffoldValue,
                            listPane = {
                                AnimatedPane {
                                    TransactionListScreen(
                                        viewModel = viewModel,
                                        onTransactionClick = { transaction ->
                                            if (backStack.lastOrNull() != Route.TransactionDetail(transaction.id)) {
                                                backStack.add(Route.TransactionDetail(transaction.id))
                                            }
                                        }
                                    )
                                }
                            },
                            detailPane = {
                                AnimatedPane {
                                    val detailRoute = if (currentKey is Route.TransactionDetail) currentKey else null
                                    
                                    val selectedTransaction by remember(detailRoute) {
                                        if (detailRoute != null) {
                                            viewModel.getTransactionByIdFlow(detailRoute.transactionId)
                                        } else {
                                            flowOf(null)
                                        }
                                    }.collectAsStateWithLifecycle(initialValue = null)
                                    
                                    TransactionDetailScreen(transaction = selectedTransaction, currencySymbol = currencySymbol)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    availableUpdate?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDismiss = { viewModel.dismissUpdate() },
            onUpdate = { url -> viewModel.onUpdateTapped(context, url) }
        )
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.size - 1)
    }
}

@Composable
private fun NavLabel(text: String) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
    }
}
