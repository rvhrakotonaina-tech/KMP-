package com.example.moneytracker.data.local

import androidx.room.*
import com.example.moneytracker.data.model.LoanDebt
import com.example.moneytracker.data.model.LoanDebtType
import com.example.moneytracker.data.model.LoanDebtWithRepayments
import com.example.moneytracker.data.model.Repayment
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDebtDao {
    @Transaction
    @Query("SELECT * FROM loans_debts ORDER BY date DESC")
    fun getAllLoansDebtsWithRepayments(): Flow<List<LoanDebtWithRepayments>>

    @Transaction
    @Query("SELECT * FROM loans_debts WHERE type = :type ORDER BY date DESC")
    fun getLoansDebtsWithType(type: LoanDebtType): Flow<List<LoanDebtWithRepayments>>

    @Transaction
    @Query("SELECT * FROM loans_debts WHERE id = :id")
    fun getLoanDebtWithRepaymentsById(id: Long): Flow<LoanDebtWithRepayments?>

    @Transaction
    @Query("SELECT * FROM loans_debts WHERE id = :id")
    suspend fun getLoanDebtWithRepaymentsByIdDirect(id: Long): LoanDebtWithRepayments?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanDebt(loanDebt: LoanDebt): Long

    @Update
    suspend fun updateLoanDebt(loanDebt: LoanDebt)

    @Delete
    suspend fun deleteLoanDebt(loanDebt: LoanDebt)

    @Query("DELETE FROM loans_debts")
    suspend fun deleteAllLoansDebts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: Repayment): Long

    @Delete
    suspend fun deleteRepayment(repayment: Repayment)

    @Query("SELECT * FROM repayments WHERE loanDebtId = :loanDebtId ORDER BY date DESC")
    fun getRepaymentsForLoanDebt(loanDebtId: Long): Flow<List<Repayment>>
}
