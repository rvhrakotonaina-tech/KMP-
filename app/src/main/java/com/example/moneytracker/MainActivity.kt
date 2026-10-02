package com.example.moneytracker

import android.app.Activity
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneytracker.data.local.AppDatabase
import com.example.moneytracker.data.local.UserPreferencesRepository
import com.example.moneytracker.data.repository.TransactionRepositoryImpl
import com.example.moneytracker.data.ai.LocalLlmEngine
import com.example.moneytracker.data.ai.ModelDownloadManager
import com.example.moneytracker.util.NetworkConnectivityObserver
import com.example.moneytracker.ui.MainViewModel
import com.example.moneytracker.ui.MoneyTrackerApp
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import java.util.Locale

import com.example.moneytracker.data.repository.LoanDebtRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val scope = rememberCoroutineScope()
            val database = AppDatabase.getDatabase(this, scope)
            val repository = TransactionRepositoryImpl(database.transactionDao())
            val loanDebtRepository = LoanDebtRepositoryImpl(database.loanDebtDao())
            val userPreferencesRepository = UserPreferencesRepository(applicationContext)
            val connectivityObserver = NetworkConnectivityObserver(applicationContext)
            
            val localLlmEngine = remember { LocalLlmEngine(applicationContext) }
            val modelDownloadManager = remember { ModelDownloadManager(applicationContext) }
            
            val viewModel: MainViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository, userPreferencesRepository, connectivityObserver, localLlmEngine, modelDownloadManager, loanDebtRepository = loanDebtRepository) as T
                }
            })[MainViewModel::class.java]

            val languageCode by viewModel.languageCode.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val currentConfig = LocalConfiguration.current
            
            val locale = remember(languageCode) { Locale.forLanguageTag(languageCode) }
            
            val localizedContext = remember(context, locale) {
                Locale.setDefault(locale)
                val config = Configuration(currentConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                val newConfigContext = context.createConfigurationContext(config)
                object : ContextWrapper(context) {
                    override fun getResources() = newConfigContext.resources
                    override fun getAssets() = newConfigContext.assets
                }
            }
            
            val configuration = remember(locale) {
                Configuration(currentConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }
            
            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalContext provides localizedContext
            ) {
                MoneyTrackerTheme(darkTheme = true) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        MoneyTrackerApp(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
