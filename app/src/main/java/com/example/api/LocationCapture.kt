package com.example.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationCapture {
    @Suppress("DEPRECATION")
    suspend fun current(context: Context): Location = suspendCancellableCoroutine { continuation ->
        val appContext = context.applicationContext
        val fineGranted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            continuation.resumeWithException(IllegalStateException("برای ثبت مسیر، مجوز موقعیت مکانی لازم است."))
            return@suspendCancellableCoroutine
        }

        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = runCatching {
            val enabled = manager.getProviders(true)
            if (fineGranted) {
                enabled.firstOrNull { it == LocationManager.GPS_PROVIDER }
                    ?: enabled.firstOrNull { it == LocationManager.NETWORK_PROVIDER }
            } else {
                enabled.firstOrNull { it == LocationManager.NETWORK_PROVIDER }
            }
        }.getOrNull()
        if (provider == null) {
            continuation.resumeWithException(IllegalStateException("GPS یا موقعیت مکانی دستگاه خاموش است."))
            return@suspendCancellableCoroutine
        }

        val handler = Handler(Looper.getMainLooper())
        val finished = AtomicBoolean(false)
        lateinit var listener: LocationListener
        lateinit var timeout: Runnable

        fun complete(location: Location?, error: Throwable? = null) {
            if (!finished.compareAndSet(false, true)) return
            handler.removeCallbacks(timeout)
            runCatching { manager.removeUpdates(listener) }
            if (!continuation.isActive) return
            if (location != null) continuation.resume(location)
            else continuation.resumeWithException(error ?: IllegalStateException("موقعیت GPS دریافت نشد."))
        }

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = complete(location)
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
        }
        timeout = Runnable { complete(null, IllegalStateException("موقعیت GPS در مهلت مجاز دریافت نشد.")) }
        continuation.invokeOnCancellation {
            handler.removeCallbacks(timeout)
            runCatching { manager.removeUpdates(listener) }
        }

        try {
            handler.postDelayed(timeout, 20_000)
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (error: SecurityException) {
            complete(null, IllegalStateException("اجازه دسترسی به موقعیت مکانی داده نشده است."))
        } catch (error: Exception) {
            complete(null, IllegalStateException("دریافت موقعیت GPS انجام نشد."))
        }
    }
}
