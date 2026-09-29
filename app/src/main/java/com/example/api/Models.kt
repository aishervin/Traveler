package com.example.api

data class LoginRequest(
    val nationalCode: String,
    val password: String,
    val capToken: String = ""
)

data class ShipmentItem(
    val id: String,
    val docNo: String,
    val driver: String,
    val plate: String,
    val origin: String,
    val destination: String,
    val cargo: String,
    val status: String
)

data class GpsPoint(
    val type: Int = 3,
    val longitude: Double,
    val latitude: Double,
    val speed: Double,
    val date: String
)
