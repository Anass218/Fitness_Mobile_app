# Fitness Mobile App

A comprehensive fitness tracking mobile application built with Android and PHP backend.

## Project Structure

- **My_Fitness_Tracker/**: Android application source code
- **BackendServer_KZO_Mobile/**: PHP backend server files
- **127_0_0_1 (4).sql**: Database schema and initial data

## Prerequisites

1. **Android Studio**: Download from [official website](https://developer.android.com/studio)
2. **Visual Studio Code**: Download from [official website](https://code.visualstudio.com/download)
3. **XAMPP Control Panel v3.3.0**: Download from [Apache Friends](https://www.apachefriends.org/download.html)
4. **MySQL**: Included with XAMPP package

## Setup Instructions

### 1. Database Setup
- Open phpMyAdmin at `http://localhost/phpmyadmin`
- Create database named `fitness_kzo`
- Import the provided SQL file

### 2. Backend Configuration
- Open backend project in Visual Studio Code
- Update `database.php` with your MySQL credentials:
```php
$servername = "localhost";
$username = "root";
$password = "";
$database = "fitness_kzo";
```

### 3. Android App Configuration
- Update `RetrofitInstance.kt` with your server URL:
```kotlin
private val BASE_URL: String = "http://YOUR_IP/folderName/"
```
- Add Google Maps API key in `strings.xml`:
```xml
<string name="map_key">YOUR_API_KEY_HERE</string>
```

### 4. Running the Application

#### Virtual Device
- Open Android Studio → Tools → Device Manager
- Create virtual device (Pixel 5 with API 30+)
- Start emulator and click Run

#### Real Device
- Enable Developer Options and USB Debugging
- Connect via USB and trust computer
- Ensure same Wi-Fi network connection

## Testing Credentials

```
Email: khin@gmail.com
Password: Password
```

## Features

- User registration and authentication
- Activity tracking (running, cycling, swimming, weightlifting, yoga)
- Profile management
- Target setting and monitoring
- Real-time data synchronization

## Technologies Used

- **Frontend**: Android (Kotlin)
- **Backend**: PHP
- **Database**: MySQL
- **Maps**: Google Maps API