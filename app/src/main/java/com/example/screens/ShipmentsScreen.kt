package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.api.ShipmentItem
import com.example.api.TripService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsScreen(
    context: android.content.Context,
    initialStatus: String = "issued",
    mode: String?,
    onSelect: (ShipmentItem) -> Unit,
    onBack: () -> Unit
) {
    var status by remember { mutableStateOf(initialStatus) }
    var search by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ShipmentItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        loading = true
        error = ""
        try {
            items = TripService.listShipments(context, status, mode == "demo")
        } catch (issue: Exception) {
            items = emptyList()
            error = issue.message ?: "دریافت فهرست اسناد انجام نشد."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(status, mode) { refresh() }

    val filtered = items.filter {
        "${it.docNo} ${it.plate} ${it.driver}".contains(search.trim(), ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فهرست اسناد حمل") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("بازگشت ‹") }
                },
                actions = {
                    TextButton(onClick = { scope.launch { refresh() } }, enabled = !loading) {
                        Text("تازه‌سازی")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            if (mode == "demo") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        "فهرست نمونه است؛ اسناد واقعی خوانده نمی‌شوند.",
                        modifier = Modifier.padding(11.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("issued" to "اسناد صادر شده", "carrying" to "اسناد در حال حمل").forEach { (key, title) ->
                    val selected = status == key
                    Button(
                        onClick = { status = key },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(title, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("جستجو بر اساس شماره سند، پلاک یا راننده") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (error.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            "نشانی HTTPS و سرآیندهای سرویس را در تنظیمات بررسی کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (loading && items.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (error.isNotBlank()) "دریافت اسناد ممکن نشد." else "سندی برای نمایش وجود ندارد.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(filtered, key = { "${it.id}-${it.status}" }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (item.status == "carrying") "در حال حمل" else "صادر شده",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("شماره سند: ${item.docNo}", style = MaterialTheme.typography.titleSmall)
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("خودرو: ${item.plate}", style = MaterialTheme.typography.bodySmall)
                                Text("راننده: ${item.driver}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${item.origin}  ←  ${item.destination}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
