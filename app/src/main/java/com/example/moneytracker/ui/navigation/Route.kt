package com.example.moneytracker.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Dashboard : Route

    @Serializable
    data object TransactionList : Route

    @Serializable
    data class TransactionDetail(val transactionId: Long) : Route

    @Serializable
    data object LoansDebts : Route
    
    @Serializable
    data object Settings : Route

    @Serializable
    data object Chat : Route
}
