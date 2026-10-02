package com.example.moneytracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneytracker.R
import com.example.moneytracker.data.ai.DownloadProgress
import com.example.moneytracker.ui.MainViewModel
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentCurrency by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.languageCode.collectAsStateWithLifecycle()
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showClearSampleDataDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val allCurrencies = remember {
        Currency.getAvailableCurrencies()
            .map { currency ->
                val symbol = try {
                    currency.getSymbol(Locale.getDefault())
                } catch (_: Exception) {
                    currency.currencyCode
                }
                val name = try {
                    currency.getDisplayName(Locale.getDefault())
                } catch (_: Exception) {
                    "Currency"
                }
                CurrencyOption(
                    symbol = symbol,
                    label = "${currency.currencyCode} - $name ($symbol)"
                )
            }
            .sortedBy { it.label }
            .distinctBy { it.label }
    }

    val allLanguages = remember {
        listOf("en", "fr", "es", "de", "it", "pt", "ru", "zh", "ja", "ko")
            .map { tag ->
                val locale = Locale.forLanguageTag(tag)
                LanguageOption(
                    tag = tag,
                    label = "${locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }} (${tag.uppercase()})"
                )
            }
            .sortedBy { it.label }
    }

    val filteredOptions = remember(searchQuery, showCurrencyPicker, showLanguagePicker) {
        if (showCurrencyPicker) {
            if (searchQuery.isBlank()) allCurrencies else allCurrencies.filter { it.label.contains(searchQuery, ignoreCase = true) || it.symbol.contains(searchQuery, ignoreCase = true) }
        } else if (showLanguagePicker) {
            if (searchQuery.isBlank()) allLanguages else allLanguages.filter { it.label.contains(searchQuery, ignoreCase = true) || it.tag.contains(searchQuery, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Currency Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_customization),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(R.string.currency_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    val displaySelected = allCurrencies.find { it.symbol == currentCurrency }?.label ?: stringResource(R.string.custom_currency, currentCurrency)
                    OutlinedTextField(
                        value = displaySelected,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.primary_currency)) },
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showCurrencyPicker = true }
                    )
                }
            }
        }

        // 2. Language Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(R.string.choose_language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    val displaySelected = allLanguages.find { it.tag == currentLanguage.split("-")[0] }?.label ?: currentLanguage
                    OutlinedTextField(
                        value = displaySelected,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.app_language)) },
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showLanguagePicker = true }
                    )
                }
            }
        }

        // 3. AI Assistant Card
        val isAiEnabled by viewModel.isAiEnabled.collectAsStateWithLifecycle()
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_assistant),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(R.string.ai_assistant_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.activate_ai_features),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isAiEnabled,
                        onCheckedChange = { viewModel.updateAiEnabled(it) }
                    )
                }

                if (isAiEnabled) {
                    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

                    when (val progress = downloadProgress) {
                        is DownloadProgress.NotStarted -> {
                            Button(
                                onClick = { 
                                    viewModel.startModelDownload("https://huggingface.co/diamondbelema/edu-hive-llm-models/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task") 
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.download_ai_model))
                            }
                        }
                        is DownloadProgress.Downloading -> {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(R.string.downloading_ai_model), style = MaterialTheme.typography.bodyMedium)
                                    Text("${progress.percentage}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { progress.percentage / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        is DownloadProgress.Completed -> {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.local_ai_model_ready),
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        is DownloadProgress.Failed -> {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.download_failed, progress.reason), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                                Button(
                                    onClick = { 
                                        viewModel.startModelDownload("https://huggingface.co/diamondbelema/edu-hive-llm-models/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task") 
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.retry_download))
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.ai_privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // 4. Sample Data Management Card
        val isSampleDataActive by viewModel.isSampleDataActive.collectAsStateWithLifecycle()
        if (isSampleDataActive) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sample_data_banner),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = { showClearSampleDataDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.clear_sample_data))
                    }
                }
            }
        }
    }

    if (showClearSampleDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearSampleDataDialog = false },
            title = { Text(stringResource(R.string.clear_sample_data_confirm_title)) },
            text = { Text(stringResource(R.string.clear_sample_data_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSampleData()
                        showClearSampleDataDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear_sample_data), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSampleDataDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showCurrencyPicker || showLanguagePicker) {
        ModalBottomSheet(
            onDismissRequest = { 
                showCurrencyPicker = false
                showLanguagePicker = false
                searchQuery = ""
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (showCurrencyPicker) stringResource(R.string.select_currency) else stringResource(R.string.select_language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (showCurrencyPicker) stringResource(R.string.search_currency_hint) else stringResource(R.string.search_language_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(filteredOptions.size) { index ->
                        val option = filteredOptions[index]
                        val isSelected = when (option) {
                            is CurrencyOption -> option.symbol == currentCurrency
                            is LanguageOption -> option.tag == currentLanguage.split("-")[0]
                            else -> false
                        }
                        ListItem(
                            headlineContent = { Text(if (option is CurrencyOption) option.label else (option as LanguageOption).label) },
                            modifier = Modifier.clickable {
                                if (option is CurrencyOption) {
                                    viewModel.updateCurrency(option.symbol)
                                    showCurrencyPicker = false
                                } else if (option is LanguageOption) {
                                    viewModel.updateLanguage(option.tag)
                                    showLanguagePicker = false
                                }
                                searchQuery = ""
                            },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                    if (filteredOptions.isEmpty()) {
                        item {
                            Text(
                                text = if (showCurrencyPicker) stringResource(R.string.no_currencies_found, searchQuery) else stringResource(R.string.no_languages_found, searchQuery),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class LanguageOption(val tag: String, val label: String)

private data class CurrencyOption(val symbol: String, val label: String)
