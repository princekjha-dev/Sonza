package com.sonza.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.sonza.app.core.model.ChatProxyModels
import com.sonza.app.ui.viewmodel.SettingsViewModel
import androidx.compose.material3.HorizontalDivider as M3HorizontalDivider

/**
 * Production-ready AI Assistant Settings screen.
 * Supports Chat Proxy (free), Google Gemini, OpenAI, and Anthropic Claude.
 * Features dynamic model fetching, secure key storage, validation, and auto-fallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Trigger dynamic catalog loading on entry
    LaunchedEffect(uiState.selectedAiProvider) {
        when (uiState.selectedAiProvider) {
            "CHAT_PROXY" -> if (uiState.chatProxyModels.isEmpty()) viewModel.loadChatProxyModels()
            "GEMINI" -> viewModel.loadGeminiModels()
            "OPENAI" -> viewModel.loadOpenAiModels()
            "ANTHROPIC" -> viewModel.loadAnthropicModels()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "AI Assistant Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Provider Selection Section
            item {
                Text(
                    text = "Select AI Provider",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AIProviderItem(
                            name = "Chat Proxy",
                            description = "Free, no API key required",
                            selected = uiState.selectedAiProvider == "CHAT_PROXY",
                            icon = Icons.Default.CloudQueue,
                            onClick = { viewModel.setSelectedAiProvider("CHAT_PROXY") }
                        )
                        M3HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        AIProviderItem(
                            name = "Gemini",
                            description = "Google's AI models",
                            selected = uiState.selectedAiProvider == "GEMINI",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { viewModel.setSelectedAiProvider("GEMINI") }
                        )
                        M3HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        AIProviderItem(
                            name = "OpenAI",
                            description = "OpenAI models",
                            selected = uiState.selectedAiProvider == "OPENAI",
                            icon = Icons.Default.Psychology,
                            onClick = { viewModel.setSelectedAiProvider("OPENAI") }
                        )
                        M3HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        AIProviderItem(
                            name = "Anthropic",
                            description = "Claude models",
                            selected = uiState.selectedAiProvider == "ANTHROPIC",
                            icon = Icons.Default.Lightbulb,
                            onClick = { viewModel.setSelectedAiProvider("ANTHROPIC") }
                        )
                    }
                }
            }

            // 2. Provider-Specific Configuration Section
            item {
                when (uiState.selectedAiProvider) {
                    "CHAT_PROXY" -> ChatProxyConfigSection(
                        selectedModel = uiState.chatProxyModel,
                        models = uiState.chatProxyModels,
                        isLoading = uiState.chatProxyModelsLoading,
                        errorMessage = uiState.chatProxyModelsError,
                        onModelChange = { viewModel.setChatProxyModel(it) },
                        onRetry = { viewModel.loadChatProxyModels(forceRefresh = true) }
                    )
                    "GEMINI" -> ProviderKeyConfigSection(
                        providerName = "Gemini",
                        title = "Gemini Configuration",
                        apiKey = uiState.geminiApiKey,
                        onApiKeyChange = { viewModel.setGeminiApiKey(it) },
                        selectedModel = uiState.geminiModel,
                        onModelChange = { viewModel.setGeminiModel(it) },
                        availableModels = uiState.geminiModels,
                        isLoadingModels = uiState.geminiModelsLoading,
                        modelsError = uiState.geminiModelsError,
                        onRetryModels = { viewModel.loadGeminiModels(forceRefresh = true) },
                        validationStatus = uiState.geminiValidationStatus,
                        onValidateKey = { viewModel.validateApiKey("GEMINI") },
                        keyPlaceholder = "AIzaSy...",
                        dashboardHint = "Get your API key from Google AI Studio (aistudio.google.com)"
                    )
                    "OPENAI" -> ProviderKeyConfigSection(
                        providerName = "OpenAI",
                        title = "OpenAI Configuration",
                        apiKey = uiState.openaiApiKey,
                        onApiKeyChange = { viewModel.setOpenAiApiKey(it) },
                        selectedModel = uiState.openaiModel,
                        onModelChange = { viewModel.setOpenAiModel(it) },
                        availableModels = uiState.openaiModels,
                        isLoadingModels = uiState.openaiModelsLoading,
                        modelsError = uiState.openaiModelsError,
                        onRetryModels = { viewModel.loadOpenAiModels(forceRefresh = true) },
                        validationStatus = uiState.openaiValidationStatus,
                        onValidateKey = { viewModel.validateApiKey("OPENAI") },
                        keyPlaceholder = "sk-proj-...",
                        dashboardHint = "Get your API key from OpenAI Platform (platform.openai.com)"
                    )
                    "ANTHROPIC" -> ProviderKeyConfigSection(
                        providerName = "Anthropic",
                        title = "Anthropic Configuration",
                        apiKey = uiState.anthropicApiKey,
                        onApiKeyChange = { viewModel.setAnthropicApiKey(it) },
                        selectedModel = uiState.anthropicModel,
                        onModelChange = { viewModel.setAnthropicModel(it) },
                        availableModels = uiState.anthropicModels,
                        isLoadingModels = uiState.anthropicModelsLoading,
                        modelsError = uiState.anthropicModelsError,
                        onRetryModels = { viewModel.loadAnthropicModels(forceRefresh = true) },
                        validationStatus = uiState.anthropicValidationStatus,
                        onValidateKey = { viewModel.validateApiKey("ANTHROPIC") },
                        keyPlaceholder = "sk-ant-...",
                        dashboardHint = "Get your API key from Anthropic Console (console.anthropic.com)"
                    )
                }
            }
        }
    }
}

@Composable
private fun AIProviderItem(
    name: String,
    description: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatProxyConfigSection(
    selectedModel: String,
    models: List<String>,
    isLoading: Boolean,
    errorMessage: String?,
    onModelChange: (String) -> Unit,
    onRetry: () -> Unit
) {
    val effectiveModels = remember(models) {
        if (models.isNotEmpty()) models else listOf(ChatProxyModels.RANDOM) + ChatProxyModels.ALL
    }

    var expanded by remember { mutableStateOf(false) }
    var localModel by remember(selectedModel) {
        mutableStateOf(if (selectedModel in effectiveModels || selectedModel.isEmpty()) selectedModel else effectiveModels.firstOrNull() ?: ChatProxyModels.RANDOM)
    }

    // Number of available models (excluding the Random sentinel option)
    val availableCount = remember(effectiveModels) {
        effectiveModels.count { it != ChatProxyModels.RANDOM }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat Proxy Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Model",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = ChatProxyModels.displayName(localModel),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    effectiveModels.forEach { modelKey ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (modelKey == ChatProxyModels.RANDOM) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = ChatProxyModels.displayName(modelKey),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            onClick = {
                                localModel = modelKey
                                onModelChange(modelKey)
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Could not refresh model list. Using cached models.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (localModel == ChatProxyModels.RANDOM)
                    "Randomly selects an optimal model per request. Automatically falls back if one fails."
                else
                    "Uses Chat Proxy API. Automatically falls back to other models if this one fails.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic available models count card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "$availableCount models available",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "If selected model fails, requests automatically try fallback models.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderKeyConfigSection(
    providerName: String,
    title: String,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    selectedModel: String,
    onModelChange: (String) -> Unit,
    availableModels: List<String>,
    isLoadingModels: Boolean,
    modelsError: String?,
    onRetryModels: () -> Unit,
    validationStatus: String?,
    onValidateKey: () -> Unit,
    keyPlaceholder: String,
    dashboardHint: String
) {
    var showApiKey by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isLoadingModels) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // API Key Input Field with Visibility Toggle
            Text(
                text = "API Key",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(keyPlaceholder) },
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide API Key" else "Show API Key"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Validation action and inline status banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onValidateKey()
                    },
                    enabled = apiKey.isNotBlank() && validationStatus != "validating",
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (validationStatus == "validating") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testing Key...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Connection", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Dynamic Status Feedback
                if (validationStatus != null) {
                    when {
                        validationStatus == "valid" -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Connected",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                        validationStatus.startsWith("error:") -> {
                            val errorText = validationStatus.removePrefix("error:")
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Model Selection Dropdown
            Text(
                text = "Model",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = selectedModel.ifBlank { availableModels.firstOrNull() ?: "" },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    availableModels.forEach { modelName ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = modelName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                onModelChange(modelName)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            if (modelsError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Using default models (could not refresh catalog)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onRetryModels) {
                        Text("Retry")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = dashboardHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
