package com.example.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.api.LocationCapture
import com.example.api.ShipmentItem
import com.example.api.TripService
import com.example.api.TripTrackingService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentDetailsScreen(
    context: android.content.Context,
    shipment: ShipmentItem,
    demoMode: Boolean,
    onBack: () -> Unit,
    onTripUpdated: () -> Unit
) {
    var activeTrip by remember { mutableStateOf(TripService.getActiveTrip(context)) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun performTripAction(start: Boolean) {
        loading = true
        error = ""
        try {
            val location = if (demoMode) null else LocationCapture.current(context)
            if (start) {
                TripService.startTrip(context, shipment, location, demoMode)
                activeTrip = TripService.getActiveTrip(context)
                if (!demoMode) TripTrackingService.start(context, shipment.id)
            } else {
                if (!demoMode) TripTrackingService.stop(context)
                try {
                    TripService.finishTrip(context, shipment.id, location, demoMode)
                } catch (issue: Exception) {
                    if (!demoMode) runCatching { TripTrackingService.start(context, shipment.id) }
                    throw issue
                }
            }
            activeTrip = TripService.getActiveTrip(context)
            onTripUpdated()
        } catch (issue: Exception) {
            error = issue.message ?: "ثبت عملیات روی سرویس انجام نشد."
        } finally {
            loading = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fineGranted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
            || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            scope.launch { performTripAction(pendingStart) }
        } else {
            error = "برای ثبت شروع یا پایان حمل، مجوز موقعیت مکانی لازم است."
        }
    }

    fun requestTripAction(start: Boolean) {
        if (demoMode) {
            scope.launch { performTripAction(start) }
            return
        }
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val permissions = mutableListOf<String>()
        if (!fineGranted && !coarseGranted) {
            permissions += listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        if (start && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        if (permissions.isEmpty()) {
            scope.launch { performTripAction(start) }
        } else {
            pendingStart = start
            permissionLauncher.launch(
                permissions.toTypedArray()
            )
        }
    }

    val shouldFinish = activeTrip?.id == shipment.id || shipment.status == "carrying"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات سند حمل") },
                navigationIcon = { TextButton(onClick = onBack) { Text("بازگشت ‹") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("شماره سند: ${shipment.docNo}", style = MaterialTheme.typography.titleMedium)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("وضعیت: ${if (shipment.status == "carrying") "در حال حمل" else "صادر شده"}")
                    Text("راننده: ${shipment.driver}")
                    Text("پلاک خودرو: ${shipment.plate}")
                    Text("مبدا: ${shipment.origin}")
                    Text("مقصد: ${shipment.destination}")
                    Text("نوع بار: ${shipment.cargo}")
                }
            }

            if (demoMode) {
                Spacer(Modifier.height(10.dp))
                Text("نسخه آزمایشی؛ تغییرات در سرور ثبت نمی‌شوند.", style = MaterialTheme.typography.bodySmall)
            }
            if (error.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { requestTripAction(start = !shouldFinish) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !loading,
                colors = if (shouldFinish) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(9.dp))
                    Text("در حال ثبت روی سرویس...")
                } else {
                    Text(if (shouldFinish) "ثبت پایان حمل و ارسال مسیر" else "ثبت شروع حمل")
                }
            }
            if (!demoMode && shouldFinish) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "برای پایان حمل، موقعیت فعلی دستگاه برای سرویس ارسال می‌شود.",
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
