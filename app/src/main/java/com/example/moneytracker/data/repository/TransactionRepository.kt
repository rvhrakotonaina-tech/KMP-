package com.example.moneytracker.data.repository

import com.example.moneytracker.data.local.TransactionDao
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionsPaged(page: Int, pageSize: Int): List<Transaction>
    fun getTransactionsFilteredPagedFlow(
        noteQuery: String?,
        type: TransactionType?,
        category: String?,
        startDate: Long?,
        endDate: Long?,
        page: Int,
        pageSize: Int
    ): Flow<List<Transaction>>

    fun getTransactionsCountFlow(
        noteQuery: String?,
        type: TransactionType?,
        category: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<Int>

    fun getCategories(): Flow<List<String>>

    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteAllTransactions()
    fun getTransactionByIdFlow(id: Long): Flow<Transaction?>
    suspend fun getTransactionById(id: Long): Transaction?
}

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    override suspend fun getTransactionsPaged(page: Int, pageSize: Int): List<Transaction> {
        val offset = page * pageSize
        return transactionDao.getTransactionsPaged(pageSize, offset)
    }

    override fun getTransactionsFilteredPagedFlow(
        noteQuery: String?,
        type: TransactionType?,
        category: String?,
        startDate: Long?,
        endDate: Long?,
        page: Int,
        pageSize: Int
    ): Flow<List<Transaction>> {
        val offset = page * pageSize
        return transactionDao.getTransactionsFilteredPagedFlow(
            noteQuery, type, category, startDate, endDate, pageSize, offset
        )
    }

    override fun getTransactionsCountFlow(
        noteQuery: String?,
        type: TransactionType?,
        category: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<Int> {
        return transactionDao.getTransactionsCountFlow(noteQuery, type, category, startDate, endDate)
    }

    override fun getCategories(): Flow<List<String>> =
        transactionDao.getCategories()

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    override fun getTransactionByIdFlow(id: Long): Flow<Transaction?> =
        transactionDao.getTransactionByIdFlow(id)

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)
}
