package com.example.moneytracker.data.local

import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import java.util.Calendar

object SampleData {
    fun getSampleTransactions(): List<Transaction> {
        val calendar = Calendar.getInstance()
        return listOf(
            Transaction(
                amount = 1200.0,
                type = TransactionType.INCOME,
                category = "Salary",
                date = calendar.timeInMillis,
                note = "Monthly salary"
            ),
            Transaction(
                amount = 50.0,
                type = TransactionType.EXPENSE,
                category = "Food",
                date = calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis,
                note = "Grocery shopping"
            ),
            Transaction(
                amount = 30.0,
                type = TransactionType.EXPENSE,
                category = "Transport",
                date = calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis,
                note = "Bus ticket"
            ),
            Transaction(
                amount = 200.0,
                type = TransactionType.INCOME,
                category = "Freelance",
                date = calendar.apply { add(Calendar.DAY_OF_YEAR, -2) }.timeInMillis,
                note = "Logo design"
            ),
            Transaction(
                amount = 15.0,
                type = TransactionType.EXPENSE,
                category = "Entertainment",
                date = calendar.apply { add(Calendar.DAY_OF_YEAR, -3) }.timeInMillis,
                note = "Cinema"
            )
        )
    }
}
