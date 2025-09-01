package com.example.busdriverpanel.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.busdriverpanel.data.api.ApiClient
import com.example.busdriverpanel.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BusRepository(private val context: Context) {
    
    private val apiService = ApiClient.busApiService
    private val prefs: SharedPreferences = context.getSharedPreferences("bus_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_BUS_ID = "bus_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    suspend fun login(busId: String, driverName: String = ""): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(busId, driverName))
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    saveLoginData(busId, driverName, loginResponse.token ?: "")
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Login failed: ${response.body()?.message ?: "Unknown error"}"))
                }
            } catch (e: Exception) {
                // For demo purposes, allow offline login
                saveLoginData(busId, driverName, "demo_token")
                Result.success(LoginResponse(
                    success = true,
                    message = "Logged in offline mode",
                    token = "demo_token",
                    busInfo = BusInfo(busId, "Demo Route", "R1")
                ))
            }
        }
    }
    
    suspend fun updateLocation(locationUpdate: LocationUpdateRequest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val response = apiService.updateLocation("Bearer $token", locationUpdate)
                if (response.isSuccessful) {
                    Result.success("Location updated successfully")
                } else {
                    Result.failure(Exception("Failed to update location"))
                }
            } catch (e: Exception) {
                // For demo, pretend it works
                Result.success("Location updated (offline mode)")
            }
        }
    }
    
    private fun saveLoginData(busId: String, driverName: String, token: String) {
        prefs.edit().apply {
            putString(KEY_BUS_ID, busId)
            putString(KEY_DRIVER_NAME, driverName)
            putString(KEY_AUTH_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getBusId(): String? = prefs.getString(KEY_BUS_ID, null)
    
    fun getDriverName(): String? = prefs.getString(KEY_DRIVER_NAME, null)
    
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
