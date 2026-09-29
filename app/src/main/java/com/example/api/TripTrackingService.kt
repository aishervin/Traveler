package com.example.api

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TripTrackingService : Service() {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @Suppress("DEPRECATION", "MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val documentId = intent?.getStringExtra(EXTRA_DOCUMENT_ID)
            ?: TripService.getActiveTrip(this)?.id
        if (documentId.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("ثبت مسیر حمل")
            .setContentText("موقعیت سند $documentId هر ۲۰ ثانیه ثبت می‌شود.")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = runCatching { manager.getProviders(true) }.getOrDefault(emptyList())
        val provider = if (fineGranted) {
            providers.firstOrNull { it == LocationManager.GPS_PROVIDER }
                ?: providers.firstOrNull { it == LocationManager.NETWORK_PROVIDER }
        } else {
            providers.firstOrNull { it == LocationManager.NETWORK_PROVIDER }
        }
        if (provider == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        locationManager = manager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                ioScope.launch { TripService.appendRoutePoint(this@TripTrackingService, location) }
            }

            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
        }
        val updateRequest = runCatching {
            manager.requestLocationUpdates(provider, LOCATION_INTERVAL_MS, 0f, locationListener!!, Looper.getMainLooper())
        }
        if (updateRequest.isFailure) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        ioScope.cancel()
        locationListener?.let { listener -> runCatching { locationManager?.removeUpdates(listener) } }
        locationListener = null
        locationManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "ثبت مسیر حمل", NotificationManager.IMPORTANCE_LOW)
        )
    }

    companion object {
        private const val CHANNEL_ID = "trip_location_tracking"
        private const val NOTIFICATION_ID = 2405
        private const val EXTRA_DOCUMENT_ID = "documentId"
        private const val LOCATION_INTERVAL_MS = 20_000L

        fun start(context: Context, documentId: String) {
            val intent = Intent(context, TripTrackingService::class.java)
                .putExtra(EXTRA_DOCUMENT_ID, documentId)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TripTrackingService::class.java))
        }
    }
}
