package com.example.api

import android.content.Context
import com.google.gson.Gson

object TripService {
    private const val PREF_TRIP = "baarbarg_trip_prefs"
    private const val KEY_ACTIVE_TRIP = "trip.active"
    private const val KEY_ROUTE_POINTS = "trip.routePoints"

    val demoShipments = listOf(
        ShipmentItem("D-24051", "۱۴۰۴۰۱۲۴۰۵۱", "راننده نمونه", "۱۲ الف ۳۴۵ ایران ۶۶", "تهران", "کرج", "کالای نمونه", "issued"),
        ShipmentItem("D-24052", "۱۴۰۴۰۱۲۴۰۵۲", "راننده نمونه", "۴۵ ب ۷۸۹ ایران ۲۲", "تهران", "قم", "بار شهری", "carrying")
    )

    fun listShipments(context: Context, status: String = "carrying", demoMode: Boolean = false): List<ShipmentItem> {
        if (demoMode) {
            val active = getActiveTrip(context)
            return demoShipments.map { item ->
                if (item.id == active?.id) item.copy(status = "carrying") else item
            }.filter { status == "all" || it.status == status }
        }
        return demoShipments.filter { status == "all" || it.status == status }
    }

    fun getShipmentDetails(context: Context, docId: String, demoMode: Boolean = false): ShipmentItem {
        return demoShipments.find { it.id == docId } ?: demoShipments[0]
    }

    fun startTrip(context: Context, document: ShipmentItem) {
        val prefs = context.getSharedPreferences(PREF_TRIP, Context.MODE_PRIVATE)
        val json = Gson().toJson(document.copy(status = "carrying"))
        prefs.edit().putString(KEY_ACTIVE_TRIP, json).apply()
    }

    fun finishTrip(context: Context, docId: String) {
        val prefs = context.getSharedPreferences(PREF_TRIP, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ACTIVE_TRIP).remove(KEY_ROUTE_POINTS).apply()
    }

    fun getActiveTrip(context: Context): ShipmentItem? {
        val prefs = context.getSharedPreferences(PREF_TRIP, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ACTIVE_TRIP, null) ?: return null
        return try {
            Gson().fromJson(json, ShipmentItem::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
