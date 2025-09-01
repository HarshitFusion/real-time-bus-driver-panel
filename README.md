# 🚌 Real-Time Bus Driver Panel

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![AWS](https://img.shields.io/badge/Backend-AWS-orange.svg)](https://aws.amazon.com)
[![Material Design 3](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://m3.material.io)

**A professional Android application for real-time bus tracking with GPS, motion detection, and cloud integration**

[Features](#-key-features) • [Installation](#-installation--setup) • [Architecture](#-technical-architecture) • [Usage](#-usage-guide)

</div>

---

## 🌟 **Overview**

The Real-Time Bus Driver Panel is a comprehensive Android application designed for public transportation systems. It enables bus drivers to provide real-time location tracking with automatic trip detection, professional UI, and seamless cloud integration. Built with modern Android development practices using Jetpack Compose and MVVM architecture.

## ✨ **Key Features**

### 🚌 **Real-Time Location Tracking**
- **Continuous GPS monitoring** with 5-second update intervals
- **Foreground service** implementation for reliable background tracking
- **Battery optimization** with intelligent location request management
- **Offline data storage** with automatic sync when network is available

### 🎯 **Automatic Trip Detection**
- **Motion sensor integration** for automatic trip start/stop detection
- **Activity recognition** using accelerometer to detect bus movement
- **Smart filtering** to avoid false positives from minor movements
- **Configurable sensitivity** for different vehicle types and conditions

### 📱 **Enhanced User Interface**
- **Material Design 3** with modern gradient backgrounds and animations
- **Professional dashboard** with real-time status indicators and controls
- **Intuitive login flow** with enhanced driver authentication
- **Responsive design** optimized for various Android screen sizes

### 🔐 **Smart Permission Management**
- **Runtime permission handling** for all location services
- **Background location** support for Android 10+ devices
- **User-friendly explanations** with clear permission requirements
- **Graceful fallback** handling when permissions are denied

## 🛠 Tech Stack

### Android App
- **Platform**: Android (Kotlin)
- **UI**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Location**: Google Play Services Location API
- **Activity Recognition**: Google Activity Recognition API
- **Background Processing**: Foreground Services
- **Networking**: Retrofit + OkHttp
- **Data**: SharedPreferences + Room (future)

### Backend (AWS)
- **API**: AWS API Gateway
- **Compute**: AWS Lambda (Node.js)
- **Database**: AWS DynamoDB
- **Authentication**: JWT tokens (or AWS Cognito)

## 📋 Prerequisites

### Development Environment
- Android Studio (latest version)
- Android SDK API 24+ (Android 7.0+)
- Kotlin support
- Google Play Services

### AWS Setup (for backend)
- AWS Account
- AWS CLI configured
- DynamoDB tables created
- API Gateway setup
- Lambda functions deployed

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd busdriverpanel
```

### 2. Android Studio Setup
1. Open project in Android Studio
2. Sync Gradle dependencies
3. Ensure Google Play Services are available on test device

### 3. Configure API Endpoint
Update the base URL in `ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://your-api-gateway-url.amazonaws.com/prod/"
```

### 4. AWS Backend Setup (Optional for Development)
The app includes offline mode for development. For production:

1. **Create DynamoDB Tables**:
   - `Buses` - Store bus information
   - `BusLocations` - Store location updates
   - `DriverSessions` - Track driver sessions

2. **Deploy Lambda Function**:
   - Use the provided `aws-lambda-function.js`
   - Set up API Gateway endpoints
   - Configure CORS for mobile app

3. **Update API URLs**:
   - Replace demo endpoints with actual AWS URLs

## 🔧 Configuration

### Permissions Required
The app automatically requests these permissions:
- `ACCESS_FINE_LOCATION` - GPS location access
- `ACCESS_COARSE_LOCATION` - Network location access
- `ACCESS_BACKGROUND_LOCATION` - Location when app is closed
- `ACTIVITY_RECOGNITION` - Motion detection
- `FOREGROUND_SERVICE` - Background tracking
- `INTERNET` - API communication

### Location Settings
- **Update Interval**: 5 seconds (when moving)
- **Minimum Distance**: 5 meters
- **High Accuracy**: GPS + Network
- **Background Tracking**: Enabled

## 📱 User Flow

1. **Driver Login**
   - Driver enters assigned Bus ID
   - Optional driver name
   - App validates and creates session

2. **Automatic Tracking**
   - App monitors device activity
   - Detects vehicle movement automatically
   - Starts GPS tracking when bus is moving

3. **Location Sharing**
   - Sends location updates every 5 seconds
   - Includes: Bus ID, coordinates, speed, timestamp
   - Status automatically changes: Moving/Idle

4. **Background Operation**
   - Continues tracking when app is minimized
   - Shows persistent notification
   - Stops tracking when bus is stationary

## 📊 API Endpoints

### Driver Login
```
POST /driver/login
{
    "busId": "BUS001",
    "driverName": "John Doe"
}
```

### Location Update
```
POST /bus/location
{
    "busId": "BUS001",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "speed": 25.5,
    "timestamp": 1640995200000,
    "bearing": 180.0,
    "accuracy": 5.0,
    "status": "moving"
}
```

## 🔒 Security Features

- **Token-based Authentication**: JWT tokens for API access
- **Input Validation**: All inputs validated before processing
- **Permission Checks**: Runtime permission verification
- **Secure Storage**: Sensitive data encrypted in SharedPreferences

## 🎯 Development Features

### For Hackathon/Demo
- **Offline Mode**: Works without backend for demonstrations
- **Mock Data**: Generates realistic demo data
- **Easy Setup**: Minimal configuration required
- **Instant Testing**: No AWS setup needed for basic functionality

### Production Ready
- **Error Handling**: Comprehensive error management
- **Retry Logic**: Automatic retry for failed API calls
- **Battery Optimization**: Smart power management
- **Network Efficiency**: Minimizes data usage

## 🔧 Build & Run

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Install on Device
```bash
./gradlew installDebug
```

## 📱 Testing

### Test Scenarios
1. **Permission Flow**: Test all permission requests
2. **Login Flow**: Valid/invalid Bus IDs
3. **Location Tracking**: Movement detection accuracy
4. **Background Mode**: App behavior when minimized
5. **Network Issues**: Offline/online transitions

### Test Devices
- **Minimum**: Android 7.0 (API 24)
- **Recommended**: Android 10+ for best experience
- **GPS Required**: Device must have GPS capability

## 🚀 Deployment

### Android App
1. Generate signed APK/AAB
2. Upload to Google Play Store
3. Configure app signing
4. Set up crash reporting

### AWS Backend
1. Deploy Lambda functions
2. Configure API Gateway
3. Set up DynamoDB tables
4. Configure monitoring/logging

## 🔄 Future Enhancements

### Phase 2 Features
- **QR Code Login**: Scan bus QR for instant login
- **Route Planning**: Integration with route schedules
- **Driver Communication**: Chat with dispatch
- **Passenger Count**: Manual passenger counting
- **Fuel Tracking**: Fuel consumption monitoring

### Advanced Features
- **Predictive ETA**: Machine learning for arrival times
- **Traffic Integration**: Real-time traffic data
- **Emergency Alerts**: Panic button integration
- **Analytics Dashboard**: Driver performance metrics

## 🐛 Troubleshooting

### Common Issues
1. **Location Not Working**: Check GPS/permissions
2. **Background Tracking Stops**: Check battery optimization settings
3. **API Errors**: Verify network connectivity
4. **Permission Denied**: Manually grant in device settings

### Debug Tips
- Enable developer options for detailed logging
- Use Android Studio logcat for debugging
- Test on multiple devices for compatibility

## 📝 License

[Add your license information here]

## 🤝 Contributing

[Add contribution guidelines here]

## 📞 Support

[Add support contact information here]

---

**Note**: This app includes both offline demo mode and production-ready AWS integration. For quick testing and demonstrations, the offline mode allows full functionality without requiring AWS setup.
