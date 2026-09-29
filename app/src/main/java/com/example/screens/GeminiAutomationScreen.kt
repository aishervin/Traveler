package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.api.GeminiKeyStore
import com.example.automation.ChatMessage
import com.example.automation.GeminiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiAutomationScreen(
    context: android.content.Context,
    onOpenSettings: () -> Unit,
    onNavigate: ((String) -> Unit)? = null,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "welcome",
                    role = "assistant",
                    text = "سلام. من دستیار هوشمند برنامه هستم. می‌توانم درباره اسناد، وضعیت سفر و خدمات باربر پاسخ دهم."
                )
            )
        )
    }
    var draft by remember { mutableStateOf("") }
    var apiKeyReady by remember { mutableStateOf(GeminiKeyStore.hasGeminiApiKey(context)) }
    var policyReady by remember { mutableStateOf(GeminiKeyStore.isAutomationEnabled(context)) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun send() {
        val text = draft.trim()
        if (text.isBlank() || busy) return
        draft = ""
        error = ""
        busy = true
        val userMsg = ChatMessage(id = "u-${System.currentTimeMillis()}", role = "user", text = text)
        messages = messages + userMsg

        coroutineScope.launch {
            try {
                val responseText = GeminiService.runTurn(context, messages, text, onNavigate)
                val assistantMsg = ChatMessage(id = "a-${System.currentTimeMillis()}", role = "assistant", text = responseText)
                messages = messages + assistantMsg
            } catch (e: Exception) {
                error = e.message ?: "خطا در دریافت پاسخ از دستیار."
            } finally {
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دستیار هوشمند Gemini") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ بازگشت") }
                },
                actions = {
                    TextButton(onClick = onOpenSettings) { Text("تنظیمات") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!apiKeyReady || !policyReady) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (!apiKeyReady) Text("• کلید Gemini در تنظیمات ذخیره نشده است.", style = MaterialTheme.typography.bodySmall)
                        if (!policyReady) Text("• اجازه اتوماسیون در تنظیمات فعال نشده است.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onOpenSettings) {
                            Text("رفتن به تنظیمات")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (error.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = error, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (busy) {
                    item {
                        Text("در حال پردازش درخواست...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("پیام خود را بنویسید...") },
                    modifier = Modifier.weight(1f),
                    enabled = !busy
                )
                Button(
                    onClick = ::send,
                    enabled = !busy && draft.isNotBlank()
                ) {
                    Text("ارسال")
                }
            }
        }
    }
}
