package com.example.api

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TripService {
    private const val PREF_TRIP = "baarbarg_trip_prefs"
    private const val ENCRYPTED_PREF_TRIP = "baarbarg_trip_prefs_encrypted"
    private const val KEY_ACTIVE_TRIP = "trip.active"
    private const val KEY_ROUTE_POINTS = "trip.routePoints"
    private val gson = Gson()

    val demoShipments = listOf(
        ShipmentItem("D-24051", "۱۴۰۴۰۱۲۴۰۵۱", "راننده نمونه", "۱۲ الف ۳۴۵ ایران ۶۶", "تهران", "کرج", "کالای نمونه", "issued"),
        ShipmentItem("D-24052", "۱۴۰۴۰۱۲۴۰۵۲", "راننده نمونه", "۴۵ ب ۷۸۹ ایران ۲۲", "تهران", "قم", "بار شهری", "carrying")
    )

    suspend fun listShipments(
        context: Context,
        status: String = "carrying",
        demoMode: Boolean = false
    ): List<ShipmentItem> {
        if (demoMode) {
            val active = getActiveTrip(context)
            return demoShipments
                .map { item -> if (item.id == active?.id) item.copy(status = "carrying") else item }
                .filter { status == "all" || it.status == status }
        }
        if (status == "all") {
            val issued = listShipments(context, "issued")
            val carrying = listShipments(context, "carrying")
            return (carrying + issued).distinctBy { it.id }
        }

        val endpoint = if (status == "issued") {
            "/Document/GetIssuedDocuments"
        } else {
            "/Document/GetShippingDocuments"
        }
        val response = BaarbargApi.post(
            context,
            endpoint,
            mapOf(
                "nCarTag" to null,
                "driverNationalCode" to null,
                "docNo" to null,
                "pageNumber" to 1,
                "pageSize" to 10
            )
        )
        return responseRows(response.get("obj")).mapIndexed { index, row ->
            normalizeDocument(row, index, if (status == "issued") "issued" else "carrying")
        }
    }

    suspend fun getShipmentDetails(
        context: Context,
        docId: String,
        demoMode: Boolean = false,
        status: String = "issued"
    ): ShipmentItem? {
        if (demoMode) return demoShipments.find { it.id == docId }

        val response = BaarbargApi.post(
            context,
            "/Document/GetShippingDocumentByID",
            mapOf("data" to docId)
        )
        val document = response.get("obj")?.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        return normalizeDocument(document, 0, status)
    }

    suspend fun startTrip(
        context: Context,
        document: ShipmentItem,
        location: Location?,
        demoMode: Boolean = false
    ) {
        if (demoMode) {
            storeActiveTrip(context, document)
            return
        }
        val activeTrip = getActiveTrip(context)
        require(activeTrip == null || activeTrip.id == document.id) {
            "یک سند دیگر در حال حمل است؛ ابتدا همان حمل را پایان دهید."
        }
        val currentLocation = location
            ?: throw IllegalStateException("برای شروع حمل باید موقعیت GPS دریافت شود.")
        val point = toGpsPoint(currentLocation)
        val payload = mapOf(
            "DocId" to document.id,
            "Speed" to point.speed,
            "Altitude" to (currentLocation.altitude.takeIf { currentLocation.hasAltitude() } ?: 0.0),
            "Longitude" to point.longitude,
            "Latitude" to point.latitude,
            "StartDate" to point.date,
            "havePermission" to true
        )

        BaarbargApi.post(context, "/Document/RegisterStartOfShipping", payload)
        storeActiveTrip(context, document.copy(status = "carrying"))
        tripPrefs(context).edit().putString(KEY_ROUTE_POINTS, gson.toJson(listOf(point))).apply()
    }

    suspend fun finishTrip(
        context: Context,
        docId: String,
        location: Location?,
        demoMode: Boolean = false
    ) {
        if (demoMode) {
            tripPrefs(context).edit().remove(KEY_ACTIVE_TRIP).remove(KEY_ROUTE_POINTS).apply()
            return
        }
        val activeTrip = getActiveTrip(context)
        require(activeTrip == null || activeTrip.id == docId) {
            "شناسه سند با سند حمل فعال هم‌خوانی ندارد."
        }
        val currentLocation = location
            ?: throw IllegalStateException("برای پایان حمل باید موقعیت GPS دریافت شود.")
        val finishPoint = toGpsPoint(currentLocation)
        val details = BaarbargApi.post(
            context,
            "/Document/GetShippingDocumentByID",
            mapOf("data" to docId)
        )
        val serverDateTime = details.get("obj")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?.let { stringValue(it, "serverDateTime", "ServerDateTime") }
        val points = loadRoutePoints(context).toMutableList()
        points.add(0, finishPoint.copy(date = serverDateTime?.let(::apiTime) ?: finishPoint.date))

        BaarbargApi.post(
            context,
            "/Document/RegisterEndOfShipping",
            mapOf("docId" to docId, "gpsList" to points)
        )
        tripPrefs(context).edit().remove(KEY_ACTIVE_TRIP).remove(KEY_ROUTE_POINTS).apply()
    }

    fun getActiveTrip(context: Context): ShipmentItem? {
        val json = tripPrefs(context).getString(KEY_ACTIVE_TRIP, null) ?: return null
        return runCatching { gson.fromJson(json, ShipmentItem::class.java) }.getOrNull()
    }

    fun appendRoutePoint(context: Context, location: Location) {
        val points = loadRoutePoints(context).toMutableList()
        points.add(toGpsPoint(location))
        tripPrefs(context).edit()
            .putString(KEY_ROUTE_POINTS, gson.toJson(points.takeLast(5_000)))
            .apply()
    }

    private fun storeActiveTrip(context: Context, document: ShipmentItem) {
        tripPrefs(context).edit()
            .putString(KEY_ACTIVE_TRIP, gson.toJson(document.copy(status = "carrying")))
            .apply()
    }

    private fun loadRoutePoints(context: Context): List<GpsPoint> {
        val json = tripPrefs(context).getString(KEY_ROUTE_POINTS, null) ?: return emptyList()
        val type = object : TypeToken<List<GpsPoint>>() {}.type
        return runCatching { gson.fromJson<List<GpsPoint>>(json, type).orEmpty() }.getOrDefault(emptyList())
    }

    private fun tripPrefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val encrypted = EncryptedSharedPreferences.create(
            appContext,
            ENCRYPTED_PREF_TRIP,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val legacy = appContext.getSharedPreferences(PREF_TRIP, Context.MODE_PRIVATE)
        if (legacy.all.isNotEmpty()) {
            val editor = encrypted.edit()
            legacy.getString(KEY_ACTIVE_TRIP, null)?.let { editor.putString(KEY_ACTIVE_TRIP, it) }
            legacy.getString(KEY_ROUTE_POINTS, null)?.let { editor.putString(KEY_ROUTE_POINTS, it) }
            if (editor.commit()) legacy.edit().clear().apply()
        }
        return encrypted
    }

    private fun toGpsPoint(location: Location) = GpsPoint(
        longitude = location.longitude,
        latitude = location.latitude,
        speed = location.speed.toDouble().takeIf { location.hasSpeed() } ?: 0.0,
        date = nowApiTime()
    )

    private fun nowApiTime(): String = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        Locale.US
    ).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())

    private fun apiTime(value: String): String = if (value.endsWith("Z")) value else "${value}Z"

    private fun responseRows(element: JsonElement?): List<JsonObject> {
        if (element == null || element.isJsonNull) return emptyList()
        if (element.isJsonArray) return element.asJsonArray.mapNotNull { row ->
            if (row.isJsonObject) row.asJsonObject else null
        }
        if (!element.isJsonObject) return emptyList()
        val container = element.asJsonObject
        val nested = sequenceOf("items", "data", "list", "documents")
            .mapNotNull { key -> container.get(key) }
            .firstOrNull { it.isJsonArray }
        if (nested != null) return nested.asJsonArray.mapNotNull { row ->
            if (row.isJsonObject) row.asJsonObject else null
        }
        return listOf(container)
    }

    private fun normalizeDocument(row: JsonObject, index: Int, status: String) = ShipmentItem(
        id = stringValue(row, "id", "docId", "DocId", "documentId", "ID") ?: index.toString(),
        docNo = stringValue(row, "docNo", "DocNo", "documentNo", "trackingCode") ?: "—",
        driver = stringValue(row, "driverName", "DriverName", "driver") ?: "—",
        plate = stringValue(row, "carTag", "CarTag", "plate", "nCarTag") ?: "—",
        origin = stringValue(row, "origin", "Origin", "senderCity") ?: "—",
        destination = stringValue(row, "destination", "Destination", "receiverCity") ?: "—",
        cargo = stringValue(row, "cargoName", "CargoName", "goodsName") ?: "—",
        status = status
    )

    fun getRoutePointCount(context: Context): Int = loadRoutePoints(context).size

    private fun stringValue(row: JsonObject, vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
        row.get(key)?.takeIf { it.isJsonPrimitive && !it.isJsonNull }?.asString?.takeIf(String::isNotBlank)
    }
}
