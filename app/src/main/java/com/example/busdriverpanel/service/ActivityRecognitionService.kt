package com.example.busdriverpanel.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class ActivityRecognitionService : Service(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var deltaAcceleration = 0f
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var movementDetectionJob: Job? = null
    
    companion object {
        private const val TAG = "ActivityRecognition"
        private const val MOVEMENT_THRESHOLD = 2.0f
        private const val MOVEMENT_CHECK_INTERVAL = 5000L // 5 seconds
        private const val GRAVITY = 9.81f
    }
    
    override fun onCreate() {
        super.onCreate()
        setupSensorDetection()
        startMovementDetection()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Activity recognition service started")
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun setupSensorDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Accelerometer sensor registered")
        }
        
        currentAcceleration = GRAVITY
        lastAcceleration = GRAVITY
    }
    
    private fun startMovementDetection() {
        movementDetectionJob = serviceScope.launch {
            while (isActive) {
                checkForMovement()
                delay(MOVEMENT_CHECK_INTERVAL)
            }
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]
                    
                    lastAcceleration = currentAcceleration
                    currentAcceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    deltaAcceleration = kotlin.math.abs(currentAcceleration - lastAcceleration)
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
    
    private fun checkForMovement() {
        val isMoving = deltaAcceleration > MOVEMENT_THRESHOLD
        
        Log.d(TAG, "Movement check - Delta: $deltaAcceleration, Moving: $isMoving")
        
        if (isMoving) {
            // Detected significant movement - likely in a vehicle
            startLocationTracking()
        }
        
        // Reset the delta for next measurement
        deltaAcceleration = 0f
    }
    
    private fun startLocationTracking() {
        Log.d(TAG, "Starting location tracking due to detected movement")
        
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
        
        // Cancel coroutines
        serviceScope.cancel()
        movementDetectionJob?.cancel()
        
        Log.d(TAG, "Activity recognition service destroyed")
    }
}
