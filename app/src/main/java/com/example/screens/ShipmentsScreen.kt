package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.api.ShipmentItem
import com.example.api.TripService

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

    LaunchedEffect(status) {
        items = TripService.listShipments(context, status, mode == "demo")
    }

    val filtered = items.filter { "${it.docNo} ${it.plate} ${it.driver}".contains(search.trim()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فهرست اسناد حمل") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ بازگشت") }
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { status = "issued" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (status == "issued") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("اسناد صادر شده", color = if (status == "issued") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { status = "carrying" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (status == "carrying") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("اسناد در حال حمل", color = if (status == "carrying") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("جستجو بر اساس شماره سند، پلاک یا راننده") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("سندی برای نمایش وجود ندارد.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "شماره سند: ${item.docNo}", style = MaterialTheme.typography.titleSmall)
                                    Text(text = if (item.status == "carrying") "در حال حمل" else "صادر شده", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("خودرو: ${item.plate}", style = MaterialTheme.typography.bodyMedium)
                                Text("راننده: ${item.driver}", style = MaterialTheme.typography.bodyMedium)
                                Text("مسیر: ${item.origin} ← ${item.destination}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
