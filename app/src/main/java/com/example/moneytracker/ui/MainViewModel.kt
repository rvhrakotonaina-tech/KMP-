package com.example.moneytracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.R
import com.example.moneytracker.data.model.Transaction
import com.example.moneytracker.data.model.TransactionType
import com.example.moneytracker.data.model.FinancialCalculator
import com.example.moneytracker.data.repository.TransactionRepository
import com.example.moneytracker.data.local.UserPreferencesRepository
import com.example.moneytracker.data.ai.ChatMessage
import com.example.moneytracker.data.ai.AIService
import com.example.moneytracker.data.ai.LocalLlmEngine
import com.example.moneytracker.data.ai.ModelDownloadManager
import com.example.moneytracker.data.ai.DownloadProgress
import com.example.moneytracker.data.ai.PromptManager
import com.example.moneytracker.data.ai.AdviceResult
import com.example.moneytracker.data.ai.PersonalizedAdviceEngine
import com.example.moneytracker.data.local.SampleData
import com.example.moneytracker.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.moneytracker.data.model.LoanDebt
import com.example.moneytracker.data.model.LoanDebtStatus
import com.example.moneytracker.data.model.LoanDebtType
import com.example.moneytracker.data.model.LoanDebtWithRepayments
import com.example.moneytracker.data.model.Repayment
import com.example.moneytracker.data.repository.LoanDebtRepository
import com.example.moneytracker.data.update.UpdateChecker
import com.example.moneytracker.data.update.UpdateInfo
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val aiService: AIService,
    private val modelDownloadManager: ModelDownloadManager,
    private val updateChecker: UpdateChecker = UpdateChecker(),
    private val loanDebtRepository: LoanDebtRepository
) : ViewModel() {
    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val loansDebtsWithRepayments: StateFlow<List<LoanDebtWithRepayments>> = loanDebtRepository.getAllLoansDebtsFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val netBalance: StateFlow<Double> = combine(
        transactions,
        loansDebtsWithRepayments
    ) { txs, ldList ->
        FinancialCalculator.calculateNetBalance(txs, ldList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    val typeFilter = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter = _categoryFilter.asStateFlow()

    private val _startDateFilter = MutableStateFlow<Long?>(null)
    val startDateFilter = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow<Long?>(null)
    val endDateFilter = _endDateFilter.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _pageSize = MutableStateFlow(10)
    val pageSize = _pageSize.asStateFlow()

    private val _toastEvent = MutableSharedFlow<ToastEvent>()
    val toastEvent = _toastEvent.asSharedFlow()

    val currencySymbol: StateFlow<String> = userPreferencesRepository.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "$"
        )

    val languageCode: StateFlow<String> = userPreferencesRepository.languageCode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Locale.getDefault().language
        )

    val isAiEnabled: StateFlow<Boolean> = userPreferencesRepository.isAiEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val modelDownloadId: StateFlow<Long> = userPreferencesRepository.modelDownloadId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1L
        )

    val hasCompletedOnboarding: StateFlow<Boolean> = userPreferencesRepository.hasCompletedOnboarding
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isSampleDataActive: StateFlow<Boolean> = userPreferencesRepository.isSampleDataActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _availableUpdate = MutableStateFlow<UpdateInfo?>(null)
    val availableUpdate = _availableUpdate.asStateFlow()

    private var hasCheckedUpdateThisSession = false

    init {
        viewModelScope.launch {
            val count = repository.getTransactionsCountFlow(null, null, null, null, null).first()
            val completed = userPreferencesRepository.hasCompletedOnboarding.first()
            if (count > 0 && !completed) {
                userPreferencesRepository.setOnboardingCompleted(true)
            }
        }
    }

    fun checkForUpdates(context: Context) {
        if (hasCheckedUpdateThisSession) return
        hasCheckedUpdateThisSession = true
        viewModelScope.launch(Dispatchers.IO) {
            val update = updateChecker.checkForUpdate(context)
            if (update != null) {
                _availableUpdate.value = update
            }
        }
    }

    fun dismissUpdate() {
        _availableUpdate.value = null
    }

    fun onUpdateTapped(context: Context, url: String) {
        _availableUpdate.value = null
        try {
            val targetUrl = if (url.isNotBlank()) url else "https://github.com/rvhrakotonaina-tech/Money-tracker-app/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun loadSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            val sampleTxs = SampleData.getSampleTransactions()
            for (tx in sampleTxs) {
                repository.insertTransaction(tx)
            }
            userPreferencesRepository.setSampleDataActive(true)
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun clearSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTransactions()
            loanDebtRepository.deleteAllLoansDebts()
            userPreferencesRepository.setSampleDataActive(false)
            _toastEvent.emit(ToastEvent.Resource(R.string.sample_data_cleared))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val downloadProgress: StateFlow<DownloadProgress> = modelDownloadId
        .flatMapLatest { id ->
            if (modelDownloadManager.isModelDownloaded()) {
                flowOf(DownloadProgress.Completed)
            } else {
                modelDownloadManager.downloadStatusFlow(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (modelDownloadManager.isModelDownloaded()) DownloadProgress.Completed else DownloadProgress.NotStarted
        )

    val dashboardAdvice: StateFlow<AdviceResult> = combine(
        transactions,
        currencySymbol,
        languageCode
    ) { txs, symbol, lang ->
        val isFrench = lang.startsWith("fr", ignoreCase = true)
        PersonalizedAdviceEngine.generateAdvice(txs, symbol, isFrench)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdviceResult("")
    )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    private val _connectivityStatus = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectivityObserver.Status.Unavailable)

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMessage = ChatMessage(text, "user")
        _chatMessages.value = _chatMessages.value + userMessage
        
        viewModelScope.launch {
            _isAiLoading.value = true
            val isFrench = languageCode.value.startsWith("fr", ignoreCase = true)

            try {
                val intent = PromptManager.detectIntent(text)
                val answer = PromptManager.generateDeterministicAnswer(
                    intent,
                    transactions.value,
                    currencySymbol.value,
                    isFrench,
                    text
                )

                _chatMessages.value = _chatMessages.value + ChatMessage(answer, "model")
            } catch (t: Throwable) {
                t.printStackTrace()
                val fallbackMsg = if (isFrench) {
                    "Je n'ai pas pu générer une réponse. Veuillez réessayer."
                } else {
                    "I couldn't generate a response. Please try again."
                }
                _chatMessages.value = _chatMessages.value + ChatMessage(fallbackMsg, "model")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun startModelDownload(url: String) {
        viewModelScope.launch {
            val id = modelDownloadManager.startDownload(url)
            if (id != -1L) {
                userPreferencesRepository.updateModelDownloadId(id)
            }
        }
    }

    val categories: StateFlow<List<String>> = repository.getCategories()
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val pagedTransactions: StateFlow<List<Transaction>> = combine(
        _searchQuery.debounce { if (it.isEmpty()) 0L else 300L },
        _typeFilter,
        _categoryFilter,
        _startDateFilter,
        _endDateFilter,
        _currentPage,
        _pageSize
    ) { args: Array<Any?> ->
        FilterParams(
            query = args[0] as String,
            type = args[1] as TransactionType?,
            cat = args[2] as String?,
            start = args[3] as Long?,
            end = args[4] as Long?,
            page = args[5] as Int,
            size = args[6] as Int
        )
    }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            repository.getTransactionsFilteredPagedFlow(
                params.query.takeIf { it.isNotBlank() },
                params.type,
                params.cat,
                params.start,
                params.end,
                params.page,
                params.size
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val totalCount: StateFlow<Int> = combine(
        _searchQuery.debounce { if (it.isEmpty()) 0L else 300L },
        _typeFilter,
        _categoryFilter,
        _startDateFilter,
        _endDateFilter
    ) { args: Array<Any?> ->
        FilterParams(
            query = args[0] as String,
            type = args[1] as TransactionType?,
            cat = args[2] as String?,
            start = args[3] as Long?,
            end = args[4] as Long?,
            page = 0,
            size = 0
        )
    }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            repository.getTransactionsCountFlow(
                params.query.takeIf { it.isNotBlank() },
                params.type,
                params.cat,
                params.start,
                params.end
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
    }

    fun onTypeFilterChange(type: TransactionType?) {
        _typeFilter.value = type
        _currentPage.value = 0
    }

    fun onCategoryFilterChange(category: String?) {
        _categoryFilter.value = category
        _currentPage.value = 0
    }

    fun onDateRangeChange(start: Long?, end: Long?) {
        _startDateFilter.value = start
        _endDateFilter.value = end
        _currentPage.value = 0
    }

    fun nextPage() {
        if ((_currentPage.value + 1) * _pageSize.value < totalCount.value) {
            _currentPage.value++
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    fun addLoanDebt(
        type: LoanDebtType,
        person: String,
        amount: Double,
        date: Long,
        dueDate: Long?,
        note: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val loanDebt = LoanDebt(
                type = type,
                person = person.trim(),
                originalAmount = amount,
                remainingAmount = amount,
                date = date,
                dueDate = dueDate,
                note = note?.trim()?.ifBlank { null }
            )
            loanDebtRepository.insertLoanDebt(loanDebt)
            val resId = if (type == LoanDebtType.LOAN) R.string.loan_added else R.string.debt_added
            _toastEvent.emit(ToastEvent.Resource(resId))
        }
    }

    fun updateLoanDebt(
        existing: LoanDebt,
        person: String,
        originalAmount: Double,
        date: Long,
        dueDate: Long?,
        note: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = loanDebtRepository.getLoanDebtByIdFlow(existing.id).first()
            val repaymentsSum = record?.repayments?.sumOf { it.amount } ?: 0.0
            val newRemaining = (originalAmount - repaymentsSum).coerceAtLeast(0.0)
            val newStatus = if (newRemaining <= 0.0) {
                if (existing.type == LoanDebtType.LOAN) LoanDebtStatus.PAID else LoanDebtStatus.REPAID
            } else {
                LoanDebtStatus.PENDING
            }

            val updated = existing.copy(
                person = person.trim(),
                originalAmount = originalAmount,
                remainingAmount = newRemaining,
                date = date,
                dueDate = dueDate,
                note = note?.trim()?.ifBlank { null },
                status = newStatus
            )
            loanDebtRepository.updateLoanDebt(updated)
            val resId = if (existing.type == LoanDebtType.LOAN) R.string.loan_updated else R.string.debt_updated
            _toastEvent.emit(ToastEvent.Resource(resId))
        }
    }

    fun deleteLoanDebt(loanDebt: LoanDebt) {
        viewModelScope.launch(Dispatchers.IO) {
            loanDebtRepository.deleteLoanDebt(loanDebt)
            val resId = if (loanDebt.type == LoanDebtType.LOAN) R.string.loan_deleted else R.string.debt_deleted
            _toastEvent.emit(ToastEvent.Resource(resId))
        }
    }

    fun addRepayment(
        loanDebtId: Long,
        amount: Double,
        date: Long,
        note: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = loanDebtRepository.addRepayment(loanDebtId, amount, date, note)
            if (success) {
                _toastEvent.emit(ToastEvent.Resource(R.string.repayment_added))
            }
            withContext(Dispatchers.Main) {
                onResult(success)
            }
        }
    }

    fun deleteRepayment(repayment: Repayment) {
        viewModelScope.launch(Dispatchers.IO) {
            loanDebtRepository.deleteRepayment(repayment)
            _toastEvent.emit(ToastEvent.Resource(R.string.repayment_deleted))
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(transaction)
            _toastEvent.emit(ToastEvent.Resource(R.string.transaction_added))
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(transaction)
            _toastEvent.emit(ToastEvent.Resource(R.string.transaction_updated))
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
            _toastEvent.emit(ToastEvent.Resource(R.string.transaction_deleted))
        }
    }

    fun getTransactionByIdFlow(id: Long): Flow<Transaction?> {
        return repository.getTransactionByIdFlow(id)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return repository.getTransactionById(id)
    }

    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateCurrencySymbol(symbol)
            _toastEvent.emit(ToastEvent.Resource(R.string.currency_updated, listOf(symbol)))
        }
    }

    fun updateLanguage(code: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguageCode(code)
            // Note: Toast might not be shown if activity recreates immediately
        }
    }

    fun updateAiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAiEnabled(enabled)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        (aiService as? LocalLlmEngine)?.close()
    }
}

sealed interface ToastEvent {
    data class Resource(val resId: Int, val args: List<Any> = emptyList()) : ToastEvent
}

private data class FilterParams(
    val query: String,
    val type: TransactionType?,
    val cat: String?,
    val start: Long?,
    val end: Long?,
    val page: Int,
    val size: Int
)
