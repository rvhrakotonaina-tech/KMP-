package com.example.moneytracker.data.model

object FinancialCalculator {

    fun calculateNetBalance(
        transactions: List<Transaction>,
        loansDebts: List<LoanDebtWithRepayments>
    ): Double {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        var loansGiven = 0.0
        var loanRepayments = 0.0
        var debtsBorrowed = 0.0
        var debtRepayments = 0.0

        for (item in loansDebts) {
            val ld = item.loanDebt
            val totalRepaid = item.repayments.sumOf { it.amount }
            if (ld.type == LoanDebtType.LOAN) {
                loansGiven += ld.originalAmount
                loanRepayments += totalRepaid
            } else {
                debtsBorrowed += ld.originalAmount
                debtRepayments += totalRepaid
            }
        }

        return income - expense - loansGiven + loanRepayments + debtsBorrowed - debtRepayments
    }

    fun calculateCategoryTotals(transactions: List<Transaction>): Map<String, Double> {
        return transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
