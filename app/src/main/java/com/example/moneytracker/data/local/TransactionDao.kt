package com.example.moneytracker.data.local

import androidx.room.*
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE (:noteQuery IS NULL OR note LIKE '%' || :noteQuery || '%')
        AND (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getTransactionsFilteredPagedFlow(
        noteQuery: String?,
        type: TransactionType?,
        category: String?,
        startDate: Long?,
        endDate: Long?,
        limit: Int,
        offset: Int
    ): Flow<List<Transaction>>

    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE (:noteQuery IS NULL OR note LIKE '%' || :noteQuery || '%')
        AND (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
    """)
    fun getTransactionsCountFlow(
        noteQuery: String?,
        type: TransactionType?,
        category: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<Int>

    @Query("SELECT DISTINCT category FROM transactions")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaged(limit: Int, offset: Int): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionByIdFlow(id: Long): Flow<Transaction?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
}
