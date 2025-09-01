package com.example.busdriverpanel.data.model

data class BusLocation(
    val busId: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val timestamp: Long,
    val bearing: Float = 0f,
    val accuracy: Float = 0f,
    val isMoving: Boolean = false
)

data class LocationUpdateRequest(
    val busId: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val timestamp: Long,
    val bearing: Float = 0f,
    val accuracy: Float = 0f,
    val status: String = "moving" // moving, idle, offline
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class LoginRequest(
    val busId: String,
    val driverName: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val busInfo: BusInfo? = null
)

data class BusInfo(
    val busId: String,
    val routeName: String,
    val routeNumber: String,
    val isActive: Boolean = true
)
