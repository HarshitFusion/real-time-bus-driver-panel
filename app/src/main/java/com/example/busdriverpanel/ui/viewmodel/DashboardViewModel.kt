package com.example.busdriverpanel.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busdriverpanel.data.repository.BusRepository
import com.example.busdriverpanel.service.ActivityRecognitionService
import com.example.busdriverpanel.service.LocationTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val busId: String = "",
    val driverName: String = "",
    val isTracking: Boolean = false,
    val currentLocation: String = "No location data",
    val currentSpeed: String = "0 km/h",
    val busStatus: String = "Idle",
    val lastUpdateTime: String = "",
    val permissionsGranted: Boolean = false,
    val permissionsDenied: List<String> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = BusRepository(application)
    private val context = application.applicationContext
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        checkPermissions()
    }
    
    private fun loadUserData() {
        _uiState.value = _uiState.value.copy(
            busId = repository.getBusId() ?: "",
            driverName = repository.getDriverName() ?: ""
        )
    }
    
    private fun checkPermissions() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        _uiState.value = _uiState.value.copy(
            permissionsGranted = hasLocationPermission && hasCoarseLocation,
            permissionsDenied = emptyList()
        )
    }
    
    fun onPermissionsDenied(deniedPermissions: List<String>) {
        _uiState.value = _uiState.value.copy(
            permissionsDenied = deniedPermissions,
            errorMessage = "Some permissions were denied. Please grant all permissions for proper tracking."
        )
    }
    
    fun startTracking() {
        if (!_uiState.value.permissionsGranted) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Location permissions are required for tracking"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        try {
            // Start activity recognition service
            val activityIntent = Intent(context, ActivityRecognitionService::class.java)
            context.startForegroundService(activityIntent)
            
            // Start location tracking service
            val locationIntent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_START_TRACKING
            }
            context.startForegroundService(locationIntent)
            
            _uiState.value = _uiState.value.copy(
                isTracking = true,
                busStatus = "Tracking Started",
                errorMessage = null,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to start tracking: ${e.message}",
                isLoading = false
            )
        }
    }
    
    fun stopTracking() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        try {
            // Stop activity recognition service
            val activityIntent = Intent(context, ActivityRecognitionService::class.java)
            context.stopService(activityIntent)
            
            // Stop location tracking service
            val locationIntent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP_TRACKING
            }
            context.startService(locationIntent)
            
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                busStatus = "Tracking Stopped",
                errorMessage = null,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to stop tracking: ${e.message}",
                isLoading = false
            )
        }
    }
    
    fun updateLocation(latitude: Double, longitude: Double, speed: Float) {
        val speedKmh = speed * 3.6f
        _uiState.value = _uiState.value.copy(
            currentLocation = "${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}",
            currentSpeed = "${"%.1f".format(speedKmh)} km/h",
            busStatus = if (speedKmh > 5) "Moving" else "Idle",
            lastUpdateTime = getCurrentTime()
        )
    }
    
    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    
    fun logout() {
        stopTracking()
        repository.logout()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun refreshPermissions() {
        checkPermissions()
    }
}
