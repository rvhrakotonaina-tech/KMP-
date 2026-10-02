package com.example.moneytracker.data.ai

import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import java.util.Locale

data class AdviceResult(
    val message: String
)

object PersonalizedAdviceEngine {

    fun generateAdvice(transactions: List<Transaction>, currencySymbol: String, isFrench: Boolean): AdviceResult {
        if (transactions.isEmpty()) {
            val msg = if (isFrench) {
                "Ajoutez quelques transactions pour recevoir des conseils personnalisés sur votre budget."
            } else {
                "Add some transactions to receive personalized budgeting advice."
            }
            return AdviceResult(msg)
        }

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        val categoryTotals = expenseTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }

        // Rule A: Dominant spending category (>= 50% of total expenses)
        if (totalExpense > 0 && categoryTotals.isNotEmpty()) {
            val topCategory = categoryTotals.first()
            val percentage = (topCategory.value / totalExpense) * 100
            if (percentage >= 50.0) {
                val localizedCat = PromptManager.localizeCategory(topCategory.key, isFrench)
                val pctInt = percentage.toInt()
                val msg = if (isFrench) {
                    "Vos dépenses en $localizedCat représentent $pctInt % de vos dépenses totales. Réduire cette catégorie pourrait avoir le plus d'impact sur votre budget."
                } else {
                    "$localizedCat accounts for $pctInt% of your total expenses. Reducing this category could have the biggest impact on your budget."
                }
                return AdviceResult(msg)
            }
        }

        // Rule D: High expense ratio (>= 75% of income)
        if (totalIncome > 0) {
            val expenseRatio = totalExpense / totalIncome
            if (expenseRatio >= 0.75) {
                val ratioPct = (expenseRatio * 100).toInt()
                val msg = if (isFrench) {
                    "Vos dépenses représentent $ratioPct % de vos revenus. Surveiller vos principales catégories pourrait vous aider à préserver davantage de marge."
                } else {
                    "Your expenses account for $ratioPct% of your income. Monitoring your main categories could help you preserve more margin."
                }
                return AdviceResult(msg)
            }
        }

        // Rule C: Savings opportunity (Net balance is substantial)
        if (netBalance > 0 && totalIncome > 0 && netBalance >= totalIncome * 0.4) {
            val formattedBalance = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, netBalance)
            val msg = if (isFrench) {
                "Vous avez actuellement $formattedBalance de solde après vos dépenses. En mettre une partie de côté chaque mois pourrait vous aider à constituer une épargne régulière."
            } else {
                "You currently have $formattedBalance remaining after your expenses. Setting aside part of this each month could help you build regular savings."
            }
            return AdviceResult(msg)
        }

        // Rule B: High expense category (Largest category >= 25%)
        if (totalExpense > 0 && categoryTotals.isNotEmpty()) {
            val topCategory = categoryTotals.first()
            val percentage = (topCategory.value / totalExpense) * 100
            if (percentage >= 25.0) {
                val localizedCat = PromptManager.localizeCategory(topCategory.key, isFrench)
                val pctInt = percentage.toInt()
                val msg = if (isFrench) {
                    "Votre principale catégorie de dépenses est l'$localizedCat avec $pctInt % de vos dépenses. C'est le premier poste à surveiller si vous souhaitez réduire vos dépenses."
                } else {
                    "Your largest expense category is $localizedCat with $pctInt% of your expenses. This is the first category to monitor if you want to reduce spending."
                }
                return AdviceResult(msg)
            }
        }

        // Rule E: Low expense ratio
        if (totalIncome > 0) {
            val expenseRatio = totalExpense / totalIncome
            if (expenseRatio <= 0.40) {
                val ratioPct = (expenseRatio * 100).toInt()
                val msg = if (isFrench) {
                    "Vos dépenses représentent actuellement $ratioPct % de vos revenus. Vous disposez d'une marge importante pour planifier votre épargne."
                } else {
                    "Your expenses currently account for $ratioPct% of your income. You have significant margin to plan your savings."
                }
                return AdviceResult(msg)
            }
        }

        val fallbackMsg = if (isFrench) {
            "Votre budget est équilibré. Continuez à suivre vos transactions pour affiner vos analyses."
        } else {
            "Your budget is balanced. Keep tracking your transactions to refine your analysis."
        }
        return AdviceResult(fallbackMsg)
    }
}
