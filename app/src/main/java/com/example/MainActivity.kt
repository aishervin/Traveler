package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.api.GeminiKeyStore
import com.example.api.ShipmentItem
import com.example.screens.*
import com.example.ui.theme.BaarbargTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaarbargTheme {
                var currentScreen by remember { mutableStateOf("dashboard") }
                var session by remember {
                    mutableStateOf<Map<String, Any>?>(
                        if (GeminiKeyStore.getAccessToken(applicationContext) != null) mapOf("mode" to "live") else null
                    )
                }
                var selectedShipment by remember { mutableStateOf<ShipmentItem?>(null) }
                var placeholderTitle by remember { mutableStateOf("") }

                if (session == null) {
                    LoginScreen(
                        context = applicationContext,
                        onSignedIn = { s ->
                            session = s
                            currentScreen = "dashboard"
                        },
                        onOpen = { key ->
                            when (key) {
                                "signup" -> { placeholderTitle = "ثبت‌نام در سامانه"; currentScreen = "placeholder" }
                                "forgotPassword" -> { placeholderTitle = "بازیابی رمز عبور"; currentScreen = "placeholder" }
                            }
                        }
                    )
                } else {
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen(
                            context = applicationContext,
                            session = session,
                            onOpen = { key ->
                                when (key) {
                                    "newdoc" -> { placeholderTitle = "بارنامه حقیقی"; currentScreen = "placeholder" }
                                    "daily" -> { placeholderTitle = "بارنامه روزانه"; currentScreen = "placeholder" }
                                    "carrying" -> currentScreen = "shipments_carrying"
                                    "history" -> { placeholderTitle = "تاریخچه اسناد حمل"; currentScreen = "placeholder" }
                                    "wallet" -> { placeholderTitle = "کیف پول اعتباری"; currentScreen = "placeholder" }
                                    "fuel" -> { placeholderTitle = "سهمیه سوخت"; currentScreen = "placeholder" }
                                    "inbox" -> { placeholderTitle = "پیام‌ها"; currentScreen = "placeholder" }
                                    "settings" -> currentScreen = "settings"
                                }
                            },
                            onOpenAutomation = { currentScreen = "automation" }
                        )
                        "shipments_issued" -> ShipmentsScreen(
                            context = applicationContext,
                            initialStatus = "issued",
                            mode = session?.get("mode") as? String,
                            onSelect = { item ->
                                selectedShipment = item
                                currentScreen = "details"
                            },
                            onBack = { currentScreen = "dashboard" }
                        )
                        "shipments_carrying" -> ShipmentsScreen(
                            context = applicationContext,
                            initialStatus = "carrying",
                            mode = session?.get("mode") as? String,
                            onSelect = { item ->
                                selectedShipment = item
                                currentScreen = "details"
                            },
                            onBack = { currentScreen = "dashboard" }
                        )
                        "details" -> {
                            if (selectedShipment != null) {
                                ShipmentDetailsScreen(
                                    context = applicationContext,
                                    shipment = selectedShipment!!,
                                    onBack = { currentScreen = "dashboard" },
                                    onTripUpdated = { currentScreen = "dashboard" }
                                )
                            } else {
                                currentScreen = "dashboard"
                            }
                        }
                        "automation" -> GeminiAutomationScreen(
                            context = applicationContext,
                            onOpenSettings = { currentScreen = "settings" },
                            onBack = { currentScreen = "dashboard" }
                        )
                        "settings" -> SettingsScreen(
                            context = applicationContext,
                            onBack = { currentScreen = "dashboard" },
                            onLogout = {
                                GeminiKeyStore.clearSession(applicationContext)
                                session = null
                                currentScreen = "login"
                            }
                        )
                        "placeholder" -> PlaceholderScreen(
                            title = placeholderTitle,
                            onBack = { currentScreen = "dashboard" }
                        )
                        else -> DashboardScreen(
                            context = applicationContext,
                            session = session,
                            onOpen = { currentScreen = "dashboard" },
                            onOpenAutomation = { currentScreen = "automation" }
                        )
                    }
                }
            }
        }
    }
}
