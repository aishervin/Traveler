package com.example.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.api.ShipmentItem
import com.example.api.TripService

data class ServiceTile(val key: String, val title: String, val image: Int)

private val tiles = listOf(
    ServiceTile("newdoc", "بارنامه حقیقی", R.drawable.baarbarg_hagigi),
    ServiceTile("daily", "بارنامه روزانه", R.drawable.baarbarg_daily),
    ServiceTile("carrying", "اسناد در حال حمل", R.drawable.iconcarying),
    ServiceTile("history", "تاریخچه اسناد حمل", R.drawable.shipping_doc_history),
    ServiceTile("wallet", "کیف پول اعتباری", R.drawable.wallet),
    ServiceTile("fuel", "سهمیه سوخت", R.drawable.gas_station),
    ServiceTile("inbox", "پیام‌ها", R.drawable.contactus),
    ServiceTile("automation", "اتوماسیون Gemini", R.drawable.logo_app),
    ServiceTile("settings", "تنظیمات و حساب", R.drawable.images_userprofilenoimage)
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
                title = { Text("صدور بارنامه شهری") },
                actions = {
                    TextButton(onClick = onOpenAutomation) {
                        Text("دستیار هوشمند")
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
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.mainpagebkscreen),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "به سامانه خوش آمدید",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (session?.get("mode") == "demo") "کاربر آزمایشی"
                        else (session?.get("nationalCode") as? String ?: "کاربر گرامی"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (session?.get("mode") == "demo") {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = "حالت آزمایشی فعال است؛ هیچ عملیات واقعی روی سرور انجام نمی‌شود.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (activeTrip != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen("carrying") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text("بارنامه در حال حمل فعال", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "شماره سند: ${activeTrip?.docNo} · ${activeTrip?.origin} تا ${activeTrip?.destination}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("خدمات سامانه", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                tiles.chunked(2).forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        rowTiles.forEach { tile ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(128.dp)
                                    .clickable { onOpen(tile.key) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(60.dp),
                                        shape = CircleShape,
                                        color = if (tile.key == "carrying" || tile.key == "inbox" || tile.key == "automation") {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.background
                                        }
                                    ) {
                                        Image(
                                            painter = painterResource(tile.image),
                                            contentDescription = null,
                                            modifier = Modifier.padding(9.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(Modifier.height(7.dp))
                                    Text(
                                        text = tile.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        if (rowTiles.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
