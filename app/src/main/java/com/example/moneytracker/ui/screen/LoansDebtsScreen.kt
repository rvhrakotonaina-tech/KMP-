package com.example.moneytracker.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneytracker.R
import com.example.moneytracker.data.model.*
import com.example.moneytracker.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

val LoanYellow = Color(0xFFFBC02D)
val DebtRed = Color(0xFFE57373)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansDebtsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val loansDebts by viewModel.loansDebtsWithRepayments.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Loans, 1 = Debts

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<LoanDebt?>(null) }
    var repayingRecord by remember { mutableStateOf<LoanDebtWithRepayments?>(null) }
    var deletingRecord by remember { mutableStateOf<LoanDebt?>(null) }

    val currentType = if (selectedTab == 0) LoanDebtType.LOAN else LoanDebtType.DEBT
    val filteredList = remember(loansDebts, currentType) {
        loansDebts.filter { it.loanDebt.type == currentType }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.loans_and_debts),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = if (selectedTab == 0) LoanYellow else DebtRed,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = if (selectedTab == 0) stringResource(R.string.add_loan) else stringResource(R.string.add_debt)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            stringResource(R.string.loans),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) LoanYellow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            stringResource(R.string.debts),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) DebtRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) stringResource(R.string.no_loans) else stringResource(R.string.no_debts),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.loanDebt.id }) { item ->
                        LoanDebtCard(
                            item = item,
                            currencySymbol = currencySymbol,
                            onRepay = { repayingRecord = item },
                            onEdit = { editingRecord = item.loanDebt },
                            onDelete = { deletingRecord = item.loanDebt },
                            onDeleteRepayment = { repayment -> viewModel.deleteRepayment(repayment) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditLoanDebtDialog(
            type = currentType,
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { person, amt, date, dueDate, note ->
                viewModel.addLoanDebt(currentType, person, amt, date, dueDate, note)
                showAddDialog = false
            }
        )
    }

    editingRecord?.let { record ->
        AddEditLoanDebtDialog(
            type = record.type,
            existing = record,
            onDismiss = { editingRecord = null },
            onSave = { person, amt, date, dueDate, note ->
                viewModel.updateLoanDebt(record, person, amt, date, dueDate, note)
                editingRecord = null
            }
        )
    }

    repayingRecord?.let { record ->
        AddRepaymentDialog(
            record = record,
            currencySymbol = currencySymbol,
            onDismiss = { repayingRecord = null },
            onSave = { amount, date, note ->
                viewModel.addRepayment(record.loanDebt.id, amount, date, note) { success ->
                    if (success) {
                        repayingRecord = null
                    }
                }
            }
        )
    }

    deletingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = {
                Text(
                    text = if (record.type == LoanDebtType.LOAN) stringResource(R.string.delete_loan) else stringResource(R.string.delete_debt),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.delete_confirm_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLoanDebt(record)
                        deletingRecord = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRecord = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun LoanDebtCard(
    item: LoanDebtWithRepayments,
    currencySymbol: String,
    onRepay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDeleteRepayment: (Repayment) -> Unit
) {
    val ld = item.loanDebt
    val isLoan = ld.type == LoanDebtType.LOAN
    val accentColor = if (isLoan) LoanYellow else DebtRed
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var expandedHistory by remember { mutableStateOf(false) }
    val totalRepaid = item.repayments.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(accentColor, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ld.person,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = when (ld.status) {
                        LoanDebtStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                        LoanDebtStatus.PAID, LoanDebtStatus.REPAID -> MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (ld.status) {
                            LoanDebtStatus.PENDING -> stringResource(R.string.status_pending)
                            LoanDebtStatus.PAID -> stringResource(R.string.status_paid)
                            LoanDebtStatus.REPAID -> stringResource(R.string.status_repaid)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (ld.status) {
                            LoanDebtStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            LoanDebtStatus.PAID, LoanDebtStatus.REPAID -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            // Financial Breakdown: Original | Repaid | Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.original_amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$currencySymbol${String.format(Locale.getDefault(), "%.2f", ld.originalAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Column {
                    Text(
                        stringResource(R.string.total_repaid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$currencySymbol${String.format(Locale.getDefault(), "%.2f", totalRepaid)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Column {
                    Text(
                        stringResource(R.string.remaining_amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$currencySymbol${String.format(Locale.getDefault(), "%.2f", ld.remainingAmount)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }
            }

            // Dates & Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.date)}: ${dateFormat.format(Date(ld.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (ld.dueDate != null) {
                    Text(
                        text = "${stringResource(R.string.due_date)}: ${dateFormat.format(Date(ld.dueDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!ld.note.isNullOrBlank()) {
                Text(
                    text = ld.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }

                if (ld.remainingAmount > 0) {
                    Button(
                        onClick = onRepay,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (isLoan) stringResource(R.string.receive_repayment) else stringResource(R.string.repay_debt),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Repayment History Section
            if (item.repayments.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedHistory = !expandedHistory },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(R.string.repayment_history)} (${item.repayments.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (expandedHistory) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = expandedHistory) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.repayments.forEach { rep ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", rep.amount)} — ${dateFormat.format(Date(rep.date))}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!rep.note.isNullOrBlank()) {
                                            Text(
                                                text = rep.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDeleteRepayment(rep) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanDebtDialog(
    type: LoanDebtType,
    existing: LoanDebt?,
    onDismiss: () -> Unit,
    onSave: (person: String, amount: Double, date: Long, dueDate: Long?, note: String?) -> Unit
) {
    var person by remember { mutableStateOf(existing?.person ?: "") }
    var amountText by remember { mutableStateOf(existing?.originalAmount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var date by remember { mutableLongStateOf(existing?.date ?: System.currentTimeMillis()) }
    var dueDate by remember { mutableStateOf<Long?>(existing?.dueDate) }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    var personError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) {
                    if (type == LoanDebtType.LOAN) stringResource(R.string.add_loan) else stringResource(R.string.add_debt)
                } else {
                    if (type == LoanDebtType.LOAN) stringResource(R.string.edit_loan) else stringResource(R.string.edit_debt)
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = person,
                    onValueChange = {
                        person = it
                        personError = false
                    },
                    label = { Text(stringResource(R.string.person_organization)) },
                    isError = personError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text(stringResource(R.string.amount)) },
                    isError = amountError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(date)),
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

                // Due Date Picker Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dueDate?.let { dateFormat.format(Date(it)) } ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.due_date)) },
                        trailingIcon = {
                            if (dueDate != null) {
                                IconButton(onClick = { dueDate = null }) {
                                    Icon(Icons.Rounded.Clear, null)
                                }
                            } else {
                                Icon(Icons.Rounded.Event, null)
                            }
                        },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDueDatePicker = true }
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val isPersonValid = person.isNotBlank()
                    val isAmountValid = amt > 0.0

                    personError = !isPersonValid
                    amountError = !isAmountValid

                    if (isPersonValid && isAmountValid) {
                        onSave(person, amt, date, dueDate, note)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showDueDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = state.selectedDateMillis
                    showDueDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepaymentDialog(
    record: LoanDebtWithRepayments,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: Long, note: String?) -> Unit
) {
    val ld = record.loanDebt
    val isLoan = ld.type == LoanDebtType.LOAN

    var amountText by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isLoan) stringResource(R.string.receive_repayment) else stringResource(R.string.repay_debt),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.remaining_amount)}: $currencySymbol${String.format(Locale.getDefault(), "%.2f", ld.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMsg = null
                    },
                    label = { Text(stringResource(R.string.amount)) },
                    isError = errorMsg != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Date Picker Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(date)),
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

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        errorMsg = "Please enter a valid amount greater than 0"
                    } else if (amt > ld.remainingAmount) {
                        errorMsg = "Repayment cannot exceed remaining amount ($currencySymbol${String.format(Locale.getDefault(), "%.2f", ld.remainingAmount)})"
                    } else {
                        onSave(amt, date, note)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
