package com.example.moneytracker.data.repository

import com.example.moneytracker.data.local.LoanDebtDao
import com.example.moneytracker.data.model.LoanDebt
import com.example.moneytracker.data.model.LoanDebtStatus
import com.example.moneytracker.data.model.LoanDebtType
import com.example.moneytracker.data.model.LoanDebtWithRepayments
import com.example.moneytracker.data.model.Repayment
import kotlinx.coroutines.flow.Flow

interface LoanDebtRepository {
    fun getAllLoansDebtsFlow(): Flow<List<LoanDebtWithRepayments>>
    fun getLoansDebtsWithTypeFlow(type: LoanDebtType): Flow<List<LoanDebtWithRepayments>>
    fun getLoanDebtByIdFlow(id: Long): Flow<LoanDebtWithRepayments?>
    suspend fun insertLoanDebt(loanDebt: LoanDebt): Long
    suspend fun updateLoanDebt(loanDebt: LoanDebt)
    suspend fun deleteLoanDebt(loanDebt: LoanDebt)
    suspend fun deleteAllLoansDebts()
    suspend fun addRepayment(loanDebtId: Long, amount: Double, date: Long, note: String?): Boolean
    suspend fun deleteRepayment(repayment: Repayment)
}

class LoanDebtRepositoryImpl(
    private val loanDebtDao: LoanDebtDao
) : LoanDebtRepository {
    override fun getAllLoansDebtsFlow(): Flow<List<LoanDebtWithRepayments>> {
        return loanDebtDao.getAllLoansDebtsWithRepayments()
    }

    override fun getLoansDebtsWithTypeFlow(type: LoanDebtType): Flow<List<LoanDebtWithRepayments>> {
        return loanDebtDao.getLoansDebtsWithType(type)
    }

    override fun getLoanDebtByIdFlow(id: Long): Flow<LoanDebtWithRepayments?> {
        return loanDebtDao.getLoanDebtWithRepaymentsById(id)
    }

    override suspend fun insertLoanDebt(loanDebt: LoanDebt): Long {
        return loanDebtDao.insertLoanDebt(loanDebt)
    }

    override suspend fun updateLoanDebt(loanDebt: LoanDebt) {
        loanDebtDao.updateLoanDebt(loanDebt)
    }

    override suspend fun deleteLoanDebt(loanDebt: LoanDebt) {
        loanDebtDao.deleteLoanDebt(loanDebt)
    }

    override suspend fun deleteAllLoansDebts() {
        loanDebtDao.deleteAllLoansDebts()
    }

    override suspend fun addRepayment(loanDebtId: Long, amount: Double, date: Long, note: String?): Boolean {
        val record = loanDebtDao.getLoanDebtWithRepaymentsByIdDirect(loanDebtId) ?: return false
        val loanDebt = record.loanDebt
        if (amount <= 0 || amount > loanDebt.remainingAmount) {
            return false
        }

        loanDebtDao.insertRepayment(
            Repayment(
                loanDebtId = loanDebtId,
                amount = amount,
                date = date,
                note = note
            )
        )

        val newRemaining = (loanDebt.remainingAmount - amount).coerceAtLeast(0.0)
        val newStatus = if (newRemaining <= 0.0) {
            if (loanDebt.type == LoanDebtType.LOAN) LoanDebtStatus.PAID else LoanDebtStatus.REPAID
        } else {
            LoanDebtStatus.PENDING
        }

        loanDebtDao.updateLoanDebt(
            loanDebt.copy(
                remainingAmount = newRemaining,
                status = newStatus
            )
        )
        return true
    }

    override suspend fun deleteRepayment(repayment: Repayment) {
        loanDebtDao.deleteRepayment(repayment)
        
        val updatedRecord = loanDebtDao.getLoanDebtWithRepaymentsByIdDirect(repayment.loanDebtId) ?: return
        val totalRepaid = updatedRecord.repayments.sumOf { it.amount }
        val newRemaining = (updatedRecord.loanDebt.originalAmount - totalRepaid).coerceAtLeast(0.0)
        val newStatus = if (newRemaining <= 0.0) {
            if (updatedRecord.loanDebt.type == LoanDebtType.LOAN) LoanDebtStatus.PAID else LoanDebtStatus.REPAID
        } else {
            LoanDebtStatus.PENDING
        }

        loanDebtDao.updateLoanDebt(
            updatedRecord.loanDebt.copy(
                remainingAmount = newRemaining,
                status = newStatus
            )
        )
    }
}
