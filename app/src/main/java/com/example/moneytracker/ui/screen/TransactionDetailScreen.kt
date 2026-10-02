package com.example.moneytracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneytracker.R
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import java.util.Date

@Composable
fun TransactionDetailScreen(
    transaction: Transaction?,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (transaction == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = stringResource(R.string.select_transaction))
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.details), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.category_label, transaction.category))
            Text(text = stringResource(R.string.amount_label, currencySymbol, transaction.amount))
            Text(
                text = stringResource(
                    R.string.type_label,
                    if (transaction.type == TransactionType.INCOME) stringResource(R.string.income) else stringResource(R.string.expense)
                )
            )
            Text(text = stringResource(R.string.note_label, transaction.note))
            Text(text = stringResource(R.string.date_label, Date(transaction.date).toString()))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailPreview() {
    Surface {
        TransactionDetailScreen(
            transaction = Transaction(
                id = 1,
                amount = 1200.0,
                type = TransactionType.INCOME,
                category = "Salary",
                date = System.currentTimeMillis(),
                note = "Monthly salary"
            ),
            currencySymbol = "$"
        )
    }
}
