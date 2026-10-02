package com.example.moneytracker.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.moneytracker.R
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import com.example.moneytracker.ui.MainViewModel
import com.example.moneytracker.ui.ToastEvent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionListScreen(
    viewModel: MainViewModel,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.pagedTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val startDate by viewModel.startDateFilter.collectAsStateWithLifecycle()
    val endDate by viewModel.endDateFilter.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val pageSize by viewModel.pageSize.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collect { event ->
            val text = when (event) {
                is ToastEvent.Resource -> context.getString(event.resId, *event.args.toTypedArray())
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    var showEditDialog by remember { mutableStateOf<Transaction?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.transaction_management),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Horizontal Add Form
        AddTransactionForm(
            onAdd = { viewModel.addTransaction(it) },
            categories = categories
        )

        HorizontalDivider()

        // 2. Filters Section
        FilterSection(
            searchQuery = searchQuery,
            onSearchChange = { viewModel.onSearchQueryChange(it) },
            onFilterClick = { showFilterDialog = true }
        )

        // 3. Transaction Table
        TransactionTable(
            transactions = transactions,
            currencySymbol = currencySymbol,
            onEdit = { showEditDialog = it },
            onDelete = { viewModel.deleteTransaction(it) },
            onRowClick = onTransactionClick
        )

        // 4. Pagination Controls
        PaginationControls(
            currentPage = currentPage,
            totalCount = totalCount,
            pageSize = pageSize,
            onNext = { viewModel.nextPage() },
            onPrev = { viewModel.prevPage() }
        )
    }

    if (showEditDialog != null) {
        CompositionLocalProvider(
            LocalConfiguration provides LocalConfiguration.current,
            LocalContext provides LocalContext.current
        ) {
            EditTransactionDialog(
                transaction = showEditDialog!!,
                onDismiss = { showEditDialog = null },
                onConfirm = {
                    viewModel.updateTransaction(it)
                    showEditDialog = null
                },
                categories = categories
            )
        }
    }

    if (showFilterDialog) {
        CompositionLocalProvider(
            LocalConfiguration provides LocalConfiguration.current,
            LocalContext provides LocalContext.current
        ) {
            FilterDialog(
                typeFilter = typeFilter,
                onTypeChange = { viewModel.onTypeFilterChange(it) },
                categoryFilter = categoryFilter,
                onCategoryChange = { viewModel.onCategoryFilterChange(it) },
                startDate = startDate,
                endDate = endDate,
                onDateRangeChange = { start, end -> viewModel.onDateRangeChange(start, end) },
                categories = categories,
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionForm(
    onAdd: (Transaction) -> Unit,
    categories: List<String>
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.add_new_transaction),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Transaction Type Dropdown (Expense/Income)
            var typeExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (type == TransactionType.INCOME) stringResource(R.string.income) else stringResource(R.string.expense),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.transaction_type)) },
                    trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { typeExpanded = true }
                )
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.expense)) },
                        onClick = {
                            type = TransactionType.EXPENSE
                            typeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.income)) },
                        onClick = {
                            type = TransactionType.INCOME
                            typeExpanded = false
                        }
                    )
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category Dropdown
            var catExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { catExpanded = true }) {
                            Icon(Icons.Rounded.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false }
                ) {
                    categories.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                category = it
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date Picker
            val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = sdf.format(Date(date)),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.date)) },
                    trailingIcon = { Icon(Icons.Rounded.CalendarToday, null) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            // + Add Button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && category.isNotBlank()) {
                        onAdd(Transaction(amount = amt, type = type, category = category, note = note, date = date))
                        amount = ""
                        category = ""
                        note = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text(stringResource(R.string.search_note)) },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Filter icon button (funnel icon)
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = stringResource(R.string.filter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    typeFilter: TransactionType?,
    onTypeChange: (TransactionType?) -> Unit,
    categoryFilter: String?,
    onCategoryChange: (String?) -> Unit,
    startDate: Long?,
    endDate: Long?,
    onDateRangeChange: (Long?, Long?) -> Unit,
    categories: List<String>,
    onDismiss: () -> Unit
) {
    var showDateRangePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.filter_components),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Transaction Type Dropdown ("All types", "Income", "Expenses")
                var typeExpanded by remember { mutableStateOf(false) }
                val typeText = when (typeFilter) {
                    null -> stringResource(R.string.all_types)
                    TransactionType.INCOME -> stringResource(R.string.income)
                    TransactionType.EXPENSE -> stringResource(R.string.expense)
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = typeText,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.transaction_type)) },
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { typeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_types)) },
                            onClick = { onTypeChange(null); typeExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.income)) },
                            onClick = { onTypeChange(TransactionType.INCOME); typeExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.expense)) },
                            onClick = { onTypeChange(TransactionType.EXPENSE); typeExpanded = false }
                        )
                    }
                }

                // Category Dropdown ("All categories" + dynamic categories list)
                var catExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categoryFilter ?: stringResource(R.string.all_categories),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.category)) },
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { catExpanded = true }
                    )
                    DropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_categories)) },
                            onClick = { onCategoryChange(null); catExpanded = false }
                        )
                        categories.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = { onCategoryChange(it); catExpanded = false }
                            )
                        }
                    }
                }

                // Date range picker button ("Date range" / selected range text)
                val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                val dateRangeText = if (startDate != null && endDate != null) {
                    "${sdf.format(Date(startDate))} - ${sdf.format(Date(endDate))}"
                } else {
                    stringResource(R.string.date_range)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDateRangePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.DateRange, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dateRangeText)
                    }
                    if (startDate != null || endDate != null) {
                        IconButton(onClick = { onDateRangeChange(null, null) }) {
                            Icon(Icons.Rounded.Close, stringResource(R.string.clear_date_filter))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )

    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateRangeChange(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    showDateRangePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TransactionTable(
    transactions: List<Transaction>,
    currencySymbol: String,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onRowClick: (Transaction) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .requiredWidth(800.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.date), modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.type), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.category), modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.note), modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.amount), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text(stringResource(R.string.actions), modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                transactions.forEach { transaction ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRowClick(transaction) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sdf.format(Date(transaction.date)), modifier = Modifier.weight(2f))
                        Text(
                            text = if (transaction.type == TransactionType.INCOME) stringResource(R.string.income) else stringResource(R.string.expense),
                            modifier = Modifier.weight(1f),
                            color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(transaction.category, modifier = Modifier.weight(1.5f))
                        Text(transaction.note, modifier = Modifier.weight(3f), maxLines = 1)
                        Text(
                            String.format(Locale.getDefault(), "%s%.2f", currencySymbol, transaction.amount),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.weight(1.5f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = { onEdit(transaction) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit), modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onDelete(transaction) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalCount: Int,
    pageSize: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val totalPages = (totalCount + pageSize - 1) / pageSize
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.total_transactions, totalCount), style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, enabled = currentPage > 0) {
                Icon(Icons.Rounded.ChevronLeft, null)
            }
            Text(text = stringResource(R.string.page_info, currentPage + 1, maxOf(1, totalPages)), style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onNext, enabled = (currentPage + 1) * pageSize < totalCount) {
                Icon(Icons.Rounded.ChevronRight, null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    categories: List<String>
) {
    var type by remember { mutableStateOf(transaction.type) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var note by remember { mutableStateOf(transaction.note) }
    var date by remember { mutableLongStateOf(transaction.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_transaction)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Same fields as in AddForm but vertically stacked
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.type_label, ""))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE })
                        Text(stringResource(R.string.expense))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME })
                        Text(stringResource(R.string.income))
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.amount)) })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.category)) })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.note)) })
                val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(text = sdf.format(Date(date)))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onConfirm(transaction.copy(amount = amt, type = type, category = category, note = note, date = date))
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
