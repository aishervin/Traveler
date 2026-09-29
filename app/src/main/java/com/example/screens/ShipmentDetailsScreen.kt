package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.api.ShipmentItem
import com.example.api.TripService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentDetailsScreen(
    context: android.content.Context,
    shipment: ShipmentItem,
    onBack: () -> Unit,
    onTripUpdated: () -> Unit
) {
    var activeTrip by remember { mutableStateOf(TripService.getActiveTrip(context)) }
    var message by remember { mutableStateOf("") }

    fun startTrip() {
        TripService.startTrip(context, shipment)
        activeTrip = TripService.getActiveTrip(context)
        message = "سفر با موفقیت آغاز شد."
        onTripUpdated()
    }

    fun finishTrip() {
        TripService.finishTrip(context, shipment.id)
        activeTrip = TripService.getActiveTrip(context)
        message = "سفر با موفقیت پایان یافت."
        onTripUpdated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات سند حمل #${shipment.docNo}") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("شماره سند: ${shipment.docNo}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("وضعیت: ${if (shipment.status == "carrying") "در حال حمل" else "صادر شده"}", style = MaterialTheme.typography.bodyMedium)
                    Text("راننده: ${shipment.driver}", style = MaterialTheme.typography.bodyMedium)
                    Text("پلاک خودرو: ${shipment.plate}", style = MaterialTheme.typography.bodyMedium)
                    Text("مبدا: ${shipment.origin}", style = MaterialTheme.typography.bodyMedium)
                    Text("مقصد: ${shipment.destination}", style = MaterialTheme.typography.bodyMedium)
                    Text("نوع بار: ${shipment.cargo}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(text = message, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            if (activeTrip?.id == shipment.id || shipment.status == "carrying") {
                Button(
                    onClick = ::finishTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ثبت پایان حمل و ارسال مسیر")
                }
            } else {
                Button(
                    onClick = ::startTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("ثبت شروع حمل")
                }
            }
        }
    }
}
