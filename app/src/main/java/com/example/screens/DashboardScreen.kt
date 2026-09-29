package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.api.ShipmentItem
import com.example.api.TripService

data class ServiceTile(val key: String, val title: String)

val tiles = listOf(
    ServiceTile("newdoc", "بارنامه حقیقی"),
    ServiceTile("daily", "بارنامه روزانه"),
    ServiceTile("carrying", "اسناد در حال حمل"),
    ServiceTile("history", "تاریخچه اسناد حمل"),
    ServiceTile("wallet", "کیف پول اعتباری"),
    ServiceTile("fuel", "سهمیه سوخت"),
    ServiceTile("inbox", "پیام‌ها"),
    ServiceTile("settings", "تنظیمات و حساب")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    context: android.content.Context,
    session: Map<String, Any>?,
    onOpen: (String) -> Unit,
    onOpenAutomation: () -> Unit
) {
    var activeTrip by remember { mutableStateOf<ShipmentItem?>(null) }

    LaunchedEffect(Unit) {
        activeTrip = TripService.getActiveTrip(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سامانه باربر - داشبورد") },
                actions = {
                    TextButton(onClick = onOpenAutomation) {
                        Text("🤖 دستیار هوشمند")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "به سامانه خوش آمدید", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = if (session?.get("mode") == "demo") "کاربر آزمایشی" else (session?.get("nationalCode") as? String ?: "کاربر گرامی"),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (session?.get("mode") == "demo") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = "حالت آزمایشی فعال است · هیچ عملیات واقعی روی سرور انجام نمی‌شود.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (activeTrip != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen("carrying") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("بارنامه در حال حمل فعال", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("شماره سند: ${activeTrip?.docNo} (${activeTrip?.origin} ← ${activeTrip?.destination})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("خدمات سامانه", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiles.chunked(2).forEach { rowTiles ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowTiles.forEach { tile ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clickable { onOpen(tile.key) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tile.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        if (rowTiles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
