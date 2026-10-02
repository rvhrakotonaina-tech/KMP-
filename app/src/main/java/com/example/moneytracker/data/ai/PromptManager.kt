package com.example.moneytracker.data.ai

import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import java.util.Locale

enum class FinancialIntent {
    GREETING,
    TOTAL_INCOME,
    TOTAL_EXPENSES,
    NET_BALANCE,
    LARGEST_EXPENSE,
    CATEGORY_EXPENSE,
    SPENDING_TOO_MUCH,
    SAVINGS_ADVICE,
    GENERAL_FINANCIAL_QUESTION
}

object PromptManager {

    fun detectIntent(message: String): FinancialIntent {
        val lower = message.lowercase(Locale.ROOT)
        return when {
            lower == "bonjour" || lower == "hello" || lower == "salut" || lower == "hi" || lower == "coucou" || lower.startsWith("bonjour") || lower.startsWith("hello") || lower.startsWith("salut") -> FinancialIntent.GREETING
            lower.contains("dépense trop") || lower.contains("depense trop") || lower.contains("spending too much") || lower.contains("trop de dépenses") -> FinancialIntent.SPENDING_TOO_MUCH
            lower.contains("revenu") || lower.contains("gagné") || lower.contains("income") || lower.contains("earned") -> FinancialIntent.TOTAL_INCOME
            lower.contains("plus grosse dépense") || lower.contains("plus grande dépense") || lower.contains("biggest expense") || lower.contains("highest expense") -> FinancialIntent.LARGEST_EXPENSE
            lower.contains("alimentation") || lower.contains("food") || lower.contains("transport") || lower.contains("entertainment") || lower.contains("divertissement") || lower.contains("accessoires") || lower.contains("accessories") -> FinancialIntent.CATEGORY_EXPENSE
            lower.contains("dépense") || lower.contains("depense") || lower.contains("spent") || lower.contains("expenses") || lower.contains("coût") -> FinancialIntent.TOTAL_EXPENSES
            lower.contains("solde") || lower.contains("balance") || lower.contains("reste") || lower.contains("epargne") || lower.contains("net") -> FinancialIntent.NET_BALANCE
            lower.contains("conseil") || lower.contains("conseils") || lower.contains("économiser") || lower.contains("economiser") || lower.contains("epargner") || lower.contains("save") || lower.contains("budget") || lower.contains("gerer") || lower.contains("gérer") -> FinancialIntent.SAVINGS_ADVICE
            else -> FinancialIntent.GENERAL_FINANCIAL_QUESTION
        }
    }

    fun localizeCategory(category: String, isFrench: Boolean): String {
        val lower = category.lowercase(Locale.ROOT)
        return if (isFrench) {
            when (lower) {
                "food", "alimentation", "groceries", "nourriture" -> "Alimentation"
                "entertainment", "divertissement", "loisirs" -> "Divertissement"
                "transport", "transportation" -> "Transport"
                "accessories", "accessoires" -> "Accessoires"
                "salary", "salaire" -> "Salaire"
                "freelance" -> "Freelance"
                else -> category.replaceFirstChar { it.uppercase() }
            }
        } else {
            when (lower) {
                "alimentation", "nourriture", "groceries" -> "Food"
                "divertissement", "loisirs" -> "Entertainment"
                "transportation" -> "Transport"
                "accessoires" -> "Accessories"
                "salaire" -> "Salary"
                else -> category.replaceFirstChar { it.uppercase() }
            }
        }
    }

    fun generateDeterministicAnswer(
        intent: FinancialIntent,
        transactions: List<Transaction>,
        currencySymbol: String,
        isFrench: Boolean,
        message: String = ""
    ): String {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        return when (intent) {
            FinancialIntent.GREETING -> {
                if (isFrench) "Bonjour ! Comment puis-je vous aider avec votre budget aujourd'hui ?" else "Hello! How can I help you with your budget today?"
            }
            FinancialIntent.TOTAL_INCOME -> {
                val formattedInc = String.format(Locale.getDefault(), "%.2f", totalIncome)
                if (isFrench) "Votre revenu total est de $currencySymbol$formattedInc." else "Your total income is $currencySymbol$formattedInc."
            }
            FinancialIntent.TOTAL_EXPENSES -> {
                val formattedExp = String.format(Locale.getDefault(), "%.2f", totalExpense)
                if (isFrench) "Vos dépenses totales sont de $currencySymbol$formattedExp." else "Your total expenses are $currencySymbol$formattedExp."
            }
            FinancialIntent.NET_BALANCE -> {
                val formattedNet = String.format(Locale.getDefault(), "%.2f", netBalance)
                val formattedInc = String.format(Locale.getDefault(), "%.2f", totalIncome)
                val formattedExp = String.format(Locale.getDefault(), "%.2f", totalExpense)
                if (isFrench) {
                    "Votre solde net actuel est de $currencySymbol$formattedNet (Revenus : $currencySymbol$formattedInc, Dépenses : $currencySymbol$formattedExp)."
                } else {
                    "Your current net balance is $currencySymbol$formattedNet (Income: $currencySymbol$formattedInc, Expenses: $currencySymbol$formattedExp)."
                }
            }
            FinancialIntent.LARGEST_EXPENSE -> {
                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                if (expenses.isEmpty()) {
                    if (isFrench) "Vous n'avez enregistré aucune dépense." else "You have no recorded expenses."
                } else {
                    val maxExp = expenses.maxByOrNull { it.amount }!!
                    val localizedCat = localizeCategory(maxExp.category, isFrench)
                    val formattedMax = String.format(Locale.getDefault(), "%.2f", maxExp.amount)
                    if (isFrench) {
                        "Votre plus grande dépense est $localizedCat avec un montant de $currencySymbol$formattedMax."
                    } else {
                        "Your largest expense is $localizedCat with an amount of $currencySymbol$formattedMax."
                    }
                }
            }
            FinancialIntent.CATEGORY_EXPENSE -> {
                val lowerMsg = message.lowercase(Locale.ROOT)
                val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }
                val targetEntry = expenseTxs.groupBy { it.category }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
                    .entries
                    .firstOrNull { entry -> 
                        val loc = localizeCategory(entry.key, isFrench).lowercase(Locale.ROOT)
                        lowerMsg.contains(loc) || lowerMsg.contains(entry.key.lowercase(Locale.ROOT))
                    }
                if (targetEntry != null) {
                    val catName = localizeCategory(targetEntry.key, isFrench)
                    val amountStr = String.format(Locale.getDefault(), "%.2f", targetEntry.value)
                    if (isFrench) {
                        "Vous avez dépensé $currencySymbol$amountStr en $catName."
                    } else {
                        "You spent $currencySymbol$amountStr on $catName."
                    }
                } else {
                    val formattedExp = String.format(Locale.getDefault(), "%.2f", totalExpense)
                    if (isFrench) {
                        "Vos dépenses totales s'élèvent à $currencySymbol$formattedExp. Aucune dépense spécifique n'a été trouvée pour cette catégorie."
                    } else {
                        "Your total expenses are $currencySymbol$formattedExp. No specific expenses were found for that category."
                    }
                }
            }
            FinancialIntent.SPENDING_TOO_MUCH -> {
                if (totalIncome > 0) {
                    val ratioPct = ((totalExpense / totalIncome) * 100).toInt()
                    val formattedExp = String.format(Locale.getDefault(), "%.2f", totalExpense)
                    val formattedInc = String.format(Locale.getDefault(), "%.2f", totalIncome)
                    if (isFrench) {
                        "Vos dépenses représentent actuellement $ratioPct % de vos revenus (Dépenses : $currencySymbol$formattedExp / Revenus : $currencySymbol$formattedInc)."
                    } else {
                        "Your expenses currently account for $ratioPct% of your income (Expenses: $currencySymbol$formattedExp / Income: $currencySymbol$formattedInc)."
                    }
                } else {
                    val formattedExp = String.format(Locale.getDefault(), "%.2f", totalExpense)
                    if (isFrench) "Vos dépenses totales sont de $currencySymbol$formattedExp. Aucun revenu n'est enregistré pour évaluer ce ratio." else "Your total expenses are $currencySymbol$formattedExp. No income recorded to evaluate this ratio."
                }
            }
            FinancialIntent.SAVINGS_ADVICE -> {
                val savingsGoal = extractSavingsGoal(message)
                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val maxExp = expenses.maxByOrNull { it.amount }
                
                if (savingsGoal != null && savingsGoal > 0) {
                    val achievable = netBalance >= savingsGoal
                    val remaining = netBalance - savingsGoal
                    val formattedGoal = String.format(Locale.getDefault(), "%.2f", savingsGoal)
                    val formattedNet = String.format(Locale.getDefault(), "%.2f", netBalance)
                    val formattedRem = String.format(Locale.getDefault(), "%.2f", remaining)

                    val topCatAdvice = if (maxExp != null) {
                        val catName = localizeCategory(maxExp.category, isFrench)
                        val formattedMax = String.format(Locale.getDefault(), "%.2f", maxExp.amount)
                        if (isFrench) " en surveillant principalement votre poste $catName ($currencySymbol$formattedMax)" else " by monitoring your top category $catName ($currencySymbol$formattedMax)"
                    } else ""

                    if (isFrench) {
                        if (achievable) {
                            "Avec un solde net actuel de $currencySymbol$formattedNet, économiser $currencySymbol$formattedGoal le mois prochain est tout à fait réalisable (il vous restera $currencySymbol$formattedRem). Vous pouvez préserver cette marge$topCatAdvice."
                        } else {
                            "Votre solde net actuel de $currencySymbol$formattedNet est inférieur à votre objectif de $currencySymbol$formattedGoal. Pour atteindre cet objectif, il sera nécessaire de réduire vos dépenses principales$topCatAdvice."
                        }
                    } else {
                        if (achievable) {
                            "With your current net balance of $currencySymbol$formattedNet, saving $currencySymbol$formattedGoal next month is fully achievable (leaving $currencySymbol$formattedRem). You can protect this margin$topCatAdvice."
                        } else {
                            "Your current net balance of $currencySymbol$formattedNet is lower than your target of $currencySymbol$formattedGoal. To reach this goal, consider reducing main expenses$topCatAdvice."
                        }
                    }
                } else if (transactions.isNotEmpty()) {
                    val formattedInc = String.format(Locale.getDefault(), "%.2f", totalIncome)
                    val formattedExp = String.format(Locale.getDefault(), "%.2f", totalExpense)
                    val formattedNet = String.format(Locale.getDefault(), "%.2f", netBalance)
                    
                    val topCatStr = if (maxExp != null) {
                        val catName = localizeCategory(maxExp.category, isFrench)
                        val formattedMax = String.format(Locale.getDefault(), "%.2f", maxExp.amount)
                        if (isFrench) " Concentrez-vous principalement sur la catégorie $catName ($currencySymbol$formattedMax)." else " Focus primarily on reducing expenses in $catName ($currencySymbol$formattedMax)."
                    } else ""

                    if (isFrench) {
                        "Pour optimiser votre budget (Revenus : $currencySymbol$formattedInc, Dépenses : $currencySymbol$formattedExp, Solde : $currencySymbol$formattedNet),$topCatStr Réduire vos postes principaux vous permettra de dégager une meilleure marge d'épargne."
                    } else {
                        "To optimize your budget (Income: $currencySymbol$formattedInc, Expenses: $currencySymbol$formattedExp, Balance: $currencySymbol$formattedNet),$topCatStr Reducing major expenses will help you build stronger savings."
                    }
                } else {
                    if (isFrench) {
                        "Pouvez-vous préciser votre question ? Vous pouvez ajouter vos transactions ou me poser une question sur votre budget, vos dépenses ou vos revenus."
                    } else {
                        "Could you clarify your question? You can add your transactions or ask me about your budget, expenses, or income."
                    }
                }
            }
            FinancialIntent.GENERAL_FINANCIAL_QUESTION -> {
                if (isFrench) {
                    "Pouvez-vous préciser votre question ? Je peux vous aider avec votre budget, vos dépenses, vos revenus ou vos économies."
                } else {
                    "Could you clarify your question? I can help you with your budget, expenses, income, or savings."
                }
            }
        }
    }

    private fun extractSavingsGoal(message: String): Double? {
        val regex = Regex("""(\d+[\d\s,.]*)\s*(?:€|\$|eur|usd|euros|dollars)""", RegexOption.IGNORE_CASE)
        val match = regex.find(message) ?: Regex("""(?:save|économiser|epargner|economiser)\D*(\d+)""", RegexOption.IGNORE_CASE).find(message)
        return match?.groupValues?.get(1)?.replace(" ", "")?.replace(",", ".")?.toDoubleOrNull()
    }

    fun buildInsightPrompt(transactions: List<Transaction>, currencySymbol: String, isFrench: Boolean): String {
        val summary = summarizeTransactions(transactions, currencySymbol, isFrench)
        val systemInstruction = if (isFrench) {
            """Vous êtes un conseiller en finances personnelles analytique. Utilisez UNIQUEMENT les faits financiers ci-dessous. N'effectuez aucun calcul. N'inventez jamais de chiffres. N'utilisez jamais le mot "pertes" pour les dépenses.
Répondez en français naturel, concis et direct, sans Markdown, sans gras (**), sans titres (##). Pas de séquences \n."""
        } else {
            """You are an analytical personal finance advisor. Use ONLY the financial facts below. Do not calculate anything. Never invent numbers. Never call expenses "losses".
Respond in natural, concise English without Markdown, bold (**), or headings (##). No \n sequences."""
        }
        val userPrompt = if (isFrench) {
            "En vous basant strictement sur ce résumé financier, donnez 1 ou 2 conseils personnalisés et concrets faisant référence aux chiffres fournis (maximum 25 mots au total, style direct):\n\n$summary"
        } else {
            "Based strictly on this financial summary, provide 1 or 2 personalized, actionable insights referencing the provided figures (under 25 words total, direct style):\n\n$summary"
        }
        return wrapInQwenTemplate(systemInstruction, userPrompt)
    }

    fun buildChatSystemPrompt(transactions: List<Transaction>, currencySymbol: String, userMessage: String, isFrench: Boolean = false): String {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        
        val savingsGoal = extractSavingsGoal(userMessage)
        val goalContext = if (savingsGoal != null) {
            val achievable = netBalance >= savingsGoal
            val remaining = netBalance - savingsGoal
            if (isFrench) {
                "\n[Calcul système précalculé - Objectif d'épargne: $savingsGoal$currencySymbol | Atteignable: ${if (achievable) "Oui" else "Non"} | Reste après objectif: $remaining$currencySymbol]"
            } else {
                "\n[Pre-calculated System Calculation - Savings Goal: $currencySymbol$savingsGoal | Achievable: ${if (achievable) "Yes" else "No"} | Remaining After Goal: $currencySymbol$remaining]"
            }
        } else {
            ""
        }

        val summary = summarizeTransactions(transactions, currencySymbol, isFrench) + goalContext

        val systemInstruction = if (isFrench) {
            """Vous êtes un assistant budgétaire IA personnel. Utilisez UNIQUEMENT les faits financiers précalculés ci-dessous pour répondre. N'effectuez aucun calcul vous-même et n'inventez aucun chiffre. 
N'appelez jamais les dépenses des "pertes". Ne donnez aucun conseil en e-commerce, dropshipping, cadeaux ou ventes de produits. 
Si l'utilisateur a un objectif d'épargne détecté, indiquez clairement s'il est atteignable selon le solde net précalculé et suggérez de réduire les dépenses dans la plus grande catégorie.
Répondez en français naturel, fluide, clair et correct, sans utiliser de Markdown, de gras (**), de titres (##), de JSON ou de code. N'utilisez pas de séquences \n."""
        } else {
            """You are a personal AI budget assistant. Use ONLY the pre-calculated financial facts below to answer. Do not perform any calculations yourself and never invent numbers. 
Never call expenses "losses". Do not give any e-commerce, dropshipping, gift, or product selling advice. 
If a user savings goal is detected, state clearly whether it is achievable according to the pre-calculated net balance and suggest reducing spending in the top expense category.
Respond in natural, fluent, clear English, without using Markdown, bold (**), headings (##), JSON, or code. Do not use \n sequences."""
        }
        
        val contextPrompt = if (isFrench) {
            "Faits financiers réels de l'utilisateur:\n$summary\n\nQuestion de l'utilisateur: $userMessage"
        } else {
            "User's Actual Financial Facts:\n$summary\n\nUser Question: $userMessage"
        }
        return wrapInQwenTemplate(systemInstruction, contextPrompt)
    }

    private fun wrapInQwenTemplate(system: String, user: String): String {
        return "<|im_start|>system\n$system<|im_end|>\n<|im_start|>user\n$user<|im_end|>\n<|im_start|>assistant\n"
    }

    private fun summarizeTransactions(transactions: List<Transaction>, currencySymbol: String, isFrench: Boolean = false): String {
        if (transactions.isEmpty()) return if (isFrench) "Aucune transaction enregistrée." else "No transactions recorded."

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        val transactionCount = transactions.size
        
        val categoryBreakdown = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .joinToString("\n") { 
                val catName = localizeCategory(it.key, isFrench)
                "- $catName: $currencySymbol${it.value}" 
            }

        val categoryText = if (categoryBreakdown.isBlank()) {
            if (isFrench) "Aucune dépense par catégorie." else "No expense categories."
        } else {
            categoryBreakdown
        }

        return if (isFrench) {
            """
                - Nombre total de transactions: $transactionCount
                - Revenu total: $currencySymbol$totalIncome
                - Dépenses totales: $currencySymbol$totalExpense
                - Solde net: $currencySymbol$netBalance
                - Dépenses par catégorie (du plus élevé au plus bas):
                $categoryText
            """.trimIndent()
        } else {
            """
                - Total Transactions: $transactionCount
                - Total Income: $currencySymbol$totalIncome
                - Total Expense: $currencySymbol$totalExpense
                - Net Balance: $currencySymbol$netBalance
                - Expenses by Category (highest to lowest):
                $categoryText
            """.trimIndent()
        }
    }
}
