package com.example.busdriverpanel.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.busdriverpanel.MainActivity
import com.example.busdriverpanel.R
import com.example.busdriverpanel.data.model.LocationUpdateRequest
import com.example.busdriverpanel.data.repository.BusRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationTrackingService : Service() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: BusRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var isTracking = false
    private var lastLocation: Location? = null
    
    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"
        private const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
        private const val FASTEST_UPDATE_INTERVAL = 2000L // 2 seconds
        private const val MIN_DISTANCE_THRESHOLD = 5f // 5 meters
    }
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        repository = BusRepository(this)
        createNotificationChannel()
        setupLocationCallback()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startLocationTracking()
            ACTION_STOP_TRACKING -> stopLocationTracking()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when bus location is being tracked"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }
    
    private fun startLocationTracking() {
        if (!hasLocationPermissions()) {
            stopSelf()
            return
        }
        
        if (isTracking) return
        
        val notification = createNotification("Starting location tracking...")
        startForeground(NOTIFICATION_ID, notification)
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
            setMinUpdateDistanceMeters(MIN_DISTANCE_THRESHOLD)
        }.build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isTracking = true
            updateNotification("Bus location tracking active")
        } catch (e: SecurityException) {
            stopSelf()
        }
    }
    
    private fun stopLocationTracking() {
        if (!isTracking) return
        
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }
    
    private fun handleLocationUpdate(location: Location) {
        val busId = repository.getBusId() ?: return
        
        // Calculate if bus is moving
        val isMoving = isLocationSignificantlyDifferent(location)
        
        val locationUpdate = LocationUpdateRequest(
            busId = busId,
            latitude = location.latitude,
            longitude = location.longitude,
            speed = location.speed,
            timestamp = System.currentTimeMillis(),
            bearing = location.bearing,
            accuracy = location.accuracy,
            status = if (isMoving) "moving" else "idle"
        )
        
        // Send location to server
        serviceScope.launch {
            repository.updateLocation(locationUpdate)
        }
        
        // Update notification
        val statusText = if (isMoving) {
            "Bus moving - Speed: ${"%.1f".format(location.speed * 3.6)} km/h"
        } else {
            "Bus idle - Location: ${"%.6f".format(location.latitude)}, ${"%.6f".format(location.longitude)}"
        }
        updateNotification(statusText)
        
        lastLocation = location
    }
    
    private fun isLocationSignificantlyDifferent(newLocation: Location): Boolean {
        lastLocation?.let { last ->
            val distance = last.distanceTo(newLocation)
            val timeDiff = newLocation.time - last.time
            
            // Consider moving if distance > 10m or speed > 1 m/s
            return distance > 10f || newLocation.speed > 1f
        }
        return false
    }
    
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bus Driver Panel")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
