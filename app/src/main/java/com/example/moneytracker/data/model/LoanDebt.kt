package com.example.moneytracker.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class LoanDebtType {
    LOAN, // Money lent to someone else (Receivable)
    DEBT  // Money borrowed from someone else (Payable)
}

enum class LoanDebtStatus {
    PENDING,
    PAID,   // For Loans
    REPAID  // For Debts
}

@Entity(tableName = "loans_debts")
data class LoanDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: LoanDebtType,
    val person: String,
    val originalAmount: Double,
    val remainingAmount: Double,
    val date: Long,
    val dueDate: Long? = null,
    val note: String? = null,
    val status: LoanDebtStatus = LoanDebtStatus.PENDING
)

@Entity(
    tableName = "repayments",
    foreignKeys = [
        ForeignKey(
            entity = LoanDebt::class,
            parentColumns = ["id"],
            childColumns = ["loanDebtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanDebtId"])]
)
data class Repayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanDebtId: Long,
    val amount: Double,
    val date: Long,
    val note: String? = null
)

data class LoanDebtWithRepayments(
    @Embedded val loanDebt: LoanDebt,
    @Relation(
        parentColumn = "id",
        entityColumn = "loanDebtId"
    )
    val repayments: List<Repayment>
)
