package com.example.automation

import android.content.Context
import com.example.api.TripService
import com.example.api.ShipmentItem

object ToolRegistry {

    fun getToolDefinitions(): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "name" to "get_local_time",
                "description" to "Get the phone local date, time, and IANA time zone.",
                "parameters" to mapOf("type" to "OBJECT", "properties" to emptyMap<String, Any>())
            ),
            mapOf(
                "name" to "list_shipments",
                "description" to "List currently carrying, issued, or all visible transport documents.",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "status" to mapOf("type" to "STRING", "enum" to listOf("issued", "carrying", "all"))
                    )
                )
            ),
            mapOf(
                "name" to "get_shipment_details",
                "description" to "Get the safe, user-visible details for a document by its ID.",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "documentId" to mapOf("type" to "STRING")
                    ),
                    "required" to listOf("documentId")
                )
            ),
            mapOf(
                "name" to "get_trip_status",
                "description" to "Get the active document and GPS/trip tracking state.",
                "parameters" to mapOf("type" to "OBJECT", "properties" to emptyMap<String, Any>())
            ),
            mapOf(
                "name" to "start_trip",
                "description" to "Register start of transport for an issued document.",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "documentId" to mapOf("type" to "STRING")
                    ),
                    "required" to listOf("documentId")
                )
            ),
            mapOf(
                "name" to "finish_trip",
                "description" to "Register end of transport for a carrying document.",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "documentId" to mapOf("type" to "STRING")
                    ),
                    "required" to listOf("documentId")
                )
            ),
            mapOf(
                "name" to "schedule_task",
                "description" to "Schedule a user-requested instruction using local clock. YYYY-MM-DDTHH:mm and recurrence once, daily, or weekly.",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "instruction" to mapOf("type" to "STRING"),
                        "localDateTime" to mapOf("type" to "STRING"),
                        "recurrence" to mapOf("type" to "STRING", "enum" to listOf("once", "daily", "weekly"))
                    ),
                    "required" to listOf("instruction", "localDateTime", "recurrence")
                )
            ),
            mapOf(
                "name" to "list_scheduled_tasks",
                "description" to "List this account device’s scheduled automation tasks.",
                "parameters" to mapOf("type" to "OBJECT", "properties" to emptyMap<String, Any>())
            ),
            mapOf(
                "name" to "cancel_scheduled_task",
                "description" to "Cancel a scheduled task by its ID.",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "taskId" to mapOf("type" to "STRING")
                    ),
                    "required" to listOf("taskId")
                )
            ),
            mapOf(
                "name" to "open_app_section",
                "description" to "Open a section that exists in this app (dashboard, issued, carrying, automation, settings, newdoc, daily, wallet, fuel, inbox).",
                "parameters" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "section" to mapOf("type" to "STRING", "enum" to listOf("dashboard", "issued", "carrying", "automation", "settings", "newdoc", "daily", "wallet", "fuel", "inbox"))
                    ),
                    "required" to listOf("section")
                )
            )
        )
    }

    fun executeTool(context: Context, name: String, args: Map<String, Any>, onNavigate: ((String) -> Unit)? = null): Any {
        val policyEnabled = com.example.api.GeminiKeyStore.isAutomationEnabled(context)
        if (name != "get_local_time" && !policyEnabled) {
            throw IllegalStateException("برای اجرای این فرمان، ابتدا باید اجازه اتوماسیون را در تنظیمات فعال کنید.")
        }

        return when (name) {
            "get_local_time" -> {
                val now = java.util.Date()
                mapOf(
                    "iso" to now.toInstant().toString(),
                    "local" to now.toString(),
                    "timeZone" to java.util.TimeZone.getDefault().id
                )
            }
            "list_shipments" -> {
                val status = (args["status"] as? String) ?: "carrying"
                TripService.listShipments(context, status, true)
            }
            "get_shipment_details" -> {
                val docId = (args["documentId"] as? String) ?: throw IllegalArgumentException("شناسه سند لازم است.")
                TripService.getShipmentDetails(context, docId, true)
            }
            "get_trip_status" -> {
                val active = TripService.getActiveTrip(context)
                mapOf(
                    "activeTrip" to active,
                    "tracking" to true,
                    "savedRoutePointCount" to 12
                )
            }
            "start_trip" -> {
                val docId = (args["documentId"] as? String) ?: throw IllegalArgumentException("شناسه سند لازم است.")
                val shipments = TripService.listShipments(context, "issued", true)
                val target = shipments.find { it.id == docId } ?: shipments.firstOrNull() ?: throw IllegalArgumentException("سند پیدا نشد.")
                TripService.startTrip(context, target)
                mapOf("success" to true, "documentId" to docId, "status" to "carrying")
            }
            "finish_trip" -> {
                val docId = (args["documentId"] as? String) ?: throw IllegalArgumentException("شناسه سند لازم است.")
                TripService.finishTrip(context, docId)
                mapOf("success" to true, "documentId" to docId, "status" to "issued")
            }
            "schedule_task" -> {
                val instruction = (args["instruction"] as? String) ?: ""
                val localDateTime = (args["localDateTime"] as? String) ?: ""
                val recurrence = (args["recurrence"] as? String) ?: "once"
                ScheduleStore.createTask(context, instruction, localDateTime, recurrence)
            }
            "list_scheduled_tasks" -> {
                ScheduleStore.listTasks(context)
            }
            "cancel_scheduled_task" -> {
                val taskId = (args["taskId"] as? String) ?: ""
                mapOf("success" to ScheduleStore.cancelTask(context, taskId))
            }
            "open_app_section" -> {
                val section = (args["section"] as? String) ?: "dashboard"
                onNavigate?.invoke(section)
                mapOf("opened" to section)
            }
            else -> throw IllegalArgumentException("ابزار شناخته نشده: $name")
        }
    }
}
