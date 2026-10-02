package com.example.moneytracker.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.R
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import com.example.moneytracker.data.ai.AdviceResult
import com.example.moneytracker.ui.theme.*
import com.example.moneytracker.ui.theme.*
import com.example.moneytracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import com.example.moneytracker.data.model.LoanDebtType
import com.example.moneytracker.data.model.LoanDebtWithRepayments

@Composable
fun DashboardScreen(
    transactions: List<Transaction>,
    currencySymbol: String,
    isAiEnabled: Boolean,
    advice: AdviceResult,
    netBalance: Double = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } - transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
    loansDebts: List<LoanDebtWithRepayments> = emptyList(),
    isSampleDataActive: Boolean = false,
    onClearSampleData: () -> Unit = {},
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate Summary Totals
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val currentBalance = netBalance

    // Group Expenses by Category for Donut Chart
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val categoryTotals = expenseTransactions.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val scrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            if (isAiEnabled) {
                FloatingActionButton(
                    onClick = onChatClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = "AI Chat")
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Screen Title
            Text(
                text = stringResource(R.string.dashboard_analytics),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            // Sample Data Active Banner
            if (isSampleDataActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.sample_data_banner),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearSampleData) {
                            Text(stringResource(R.string.clear_sample_data), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Personalized Advice Section
            if (isAiEnabled) {
                AdviceSection(advice = advice)
            }

            // Loans & Debts Dashboard Summary Cards
            DashboardLoansDebtsSummary(loansDebts = loansDebts, currencySymbol = currencySymbol)

            // Summary Cards Section
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 500.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = stringResource(R.string.total_income),
                            amount = totalIncome,
                            currencySymbol = currencySymbol,
                            color = IncomeGreen,
                            icon = Icons.Rounded.ArrowUpward,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SummaryCard(
                            title = stringResource(R.string.total_expenses),
                            amount = totalExpense,
                            currencySymbol = currencySymbol,
                            color = ExpenseRed,
                            icon = Icons.Rounded.ArrowDownward,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SummaryCard(
                            title = stringResource(R.string.net_balance),
                            amount = currentBalance,
                            currencySymbol = currencySymbol,
                            color = BalanceCyan,
                            icon = Icons.Rounded.AccountBalanceWallet,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = stringResource(R.string.total_income),
                            amount = totalIncome,
                            currencySymbol = currencySymbol,
                            color = IncomeGreen,
                            icon = Icons.Rounded.ArrowUpward,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = stringResource(R.string.total_expenses),
                            amount = totalExpense,
                            currencySymbol = currencySymbol,
                            color = ExpenseRed,
                            icon = Icons.Rounded.ArrowDownward,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = stringResource(R.string.net_balance),
                            amount = currentBalance,
                            currencySymbol = currencySymbol,
                            color = BalanceCyan,
                            icon = Icons.Rounded.AccountBalanceWallet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Charts Section
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 720.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CategoryDonutChartCard(categoryTotals = categoryTotals, currencySymbol = currencySymbol)
                        MonthlyBarChartCard(transactions = transactions)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.8f)) {
                            CategoryDonutChartCard(categoryTotals = categoryTotals, currencySymbol = currencySymbol)
                        }
                        Box(modifier = Modifier.weight(1.2f)) {
                            MonthlyBarChartCard(transactions = transactions)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdviceSection(advice: AdviceResult) {
    if (advice.message.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.personalized_advice),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = advice.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun DashboardLoansDebtsSummary(
    loansDebts: List<LoanDebtWithRepayments>,
    currencySymbol: String
) {
    val pendingLoans = loansDebts.filter { it.loanDebt.type == LoanDebtType.LOAN && it.loanDebt.remainingAmount > 0 }
    val totalToReceive = pendingLoans.sumOf { it.loanDebt.remainingAmount }

    val pendingDebts = loansDebts.filter { it.loanDebt.type == LoanDebtType.DEBT && it.loanDebt.remainingAmount > 0 }
    val totalToRepay = pendingDebts.sumOf { it.loanDebt.remainingAmount }

    if (pendingLoans.isEmpty() && pendingDebts.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (pendingLoans.isNotEmpty()) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LoanYellow.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(LoanYellow, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.loans),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    pendingLoans.take(2).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                item.loanDebt.person,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$currencySymbol${String.format(Locale.getDefault(), "%.2f", item.loanDebt.remainingAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = LoanYellow.copy(alpha = 0.3f))

                    Text(
                        text = "${stringResource(R.string.total_to_receive)}: $currencySymbol${String.format(Locale.getDefault(), "%.2f", totalToReceive)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LoanYellow
                    )
                }
            }
        }

        if (pendingDebts.isNotEmpty()) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DebtRed.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(DebtRed, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.debts),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    pendingDebts.take(2).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                item.loanDebt.person,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$currencySymbol${String.format(Locale.getDefault(), "%.2f", item.loanDebt.remainingAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = DebtRed.copy(alpha = 0.3f))

                    Text(
                        text = "${stringResource(R.string.total_to_repay)}: $currencySymbol${String.format(Locale.getDefault(), "%.2f", totalToRepay)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DebtRed
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = String.format("%s%,.2f", currencySymbol, amount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private data class YearMonthKey(val year: Int, val month: Int) : Comparable<YearMonthKey> {
    override fun compareTo(other: YearMonthKey): Int {
        return if (this.year != other.year) {
            this.year.compareTo(other.year)
        } else {
            this.month.compareTo(other.month)
        }
    }
}

private fun getYearMonthKey(dateMillis: Long): YearMonthKey {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMillis
    return YearMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
}

@Composable
fun MonthlyBarChartCard(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_chart_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Extract distinct YearMonthKeys present in actual transactions
            val txMonths = transactions.map { getYearMonthKey(it.date) }
                .distinct()
                .sorted()

            // Select up to the 3 most recent months that actually contain transaction data
            val selectedMonths = txMonths.takeLast(3)

            val chartData = selectedMonths.map { ym ->
                val tempCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, ym.year)
                    set(Calendar.MONTH, ym.month)
                    set(Calendar.DAY_OF_MONTH, 15)
                }
                val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(tempCal.time)

                val monthTxs = transactions.filter { tx ->
                    getYearMonthKey(tx.date) == ym
                }

                val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                MonthData(monthName, income, expense)
            }

            val totalVolume = chartData.sumOf { it.income + it.expense }

            if (totalVolume == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_chart_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxVal = chartData.flatMap { listOf(it.income, it.expense) }.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
                val containerHeight = 180.dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(containerHeight)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartData.forEach { data ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxHeight(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val incomeHeightRatio = if (data.income > 0.0) {
                                        (data.income / maxVal).toFloat().coerceIn(0.04f, 1f)
                                    } else 0f

                                    if (incomeHeightRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .fillMaxHeight(incomeHeightRatio)
                                                .background(IncomeGreen, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }

                                    val expenseHeightRatio = if (data.expense > 0.0) {
                                        (data.expense / maxVal).toFloat().coerceIn(0.04f, 1f)
                                    } else 0f

                                    if (expenseHeightRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .fillMaxHeight(expenseHeightRatio)
                                                .background(ExpenseRed, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = data.month,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp).background(IncomeGreen, RoundedCornerShape(3.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.income), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(24.dp))
                    Box(modifier = Modifier.size(12.dp).background(ExpenseRed, RoundedCornerShape(3.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.expense), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

data class MonthData(val month: String, val income: Double, val expense: Double)

@Composable
fun CategoryDonutChartCard(
    categoryTotals: Map<String, Double>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.category_chart_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            val totalExpense = categoryTotals.values.sum()

            if (totalExpense == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_expense_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val colors = listOf(
                    Color(0xFFF59E0B), Color(0xFF3B82F6), Color(0xFFEC4899), Color(0xFF10B981), Color(0xFF8B5CF6)
                )

                val chartItems = categoryTotals.entries.mapIndexed { index, entry ->
                    CategoryChartItem(
                        category = entry.key,
                        amount = entry.value,
                        percentage = (entry.value / totalExpense * 100).toFloat(),
                        color = colors[index % colors.size]
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(8.dp)
                    ) {
                        var startAngle = -90f
                        chartItems.forEach { item ->
                            val sweepAngle = (item.percentage / 100f) * 360f
                            drawArc(
                                color = item.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chartItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(10.dp).background(item.color, RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = String.format("%s%,.2f (%.1f%%)", currencySymbol, item.amount, item.percentage),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CategoryChartItem(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

@Preview(name = "Compact Dashboard", device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenCompactPreview() {
    val sampleTransactions = listOf(
        Transaction(amount = 1200.0, type = TransactionType.INCOME, category = "Salary", date = System.currentTimeMillis(), note = ""),
        Transaction(amount = 200.0, type = TransactionType.INCOME, category = "Freelance", date = System.currentTimeMillis(), note = ""),
        Transaction(amount = 150.0, type = TransactionType.EXPENSE, category = "Food", date = System.currentTimeMillis(), note = ""),
        Transaction(amount = 65.0, type = TransactionType.EXPENSE, category = "Transport", date = System.currentTimeMillis(), note = ""),
        Transaction(amount = 80.0, type = TransactionType.EXPENSE, category = "Entertainment", date = System.currentTimeMillis(), note = "")
    )
    MoneyTrackerTheme(darkTheme = true) {
        Surface {
            DashboardScreen(
                transactions = sampleTransactions, 
                currencySymbol = "$",
                isAiEnabled = true,
                advice = AdviceResult("Your spending on Food is high."),
                onChatClick = {}
            )
        }
    }
}
