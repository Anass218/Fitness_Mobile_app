Installation and Setup Guide
=========================================================================================================================================

Prerequisites
   
   1. Android Studio: Install Android Studio on your machine. You can download it from the official website: https://developer.android.com/studio

   2. Visual Studio Code: Install Visual Studio Code on your machine. You can download it from the official website: https://code.visualstudio.com/download

   3. XAMPP Control Panel v3.3.0: Download and install XAMPP Control Panel v3.3.0 from the official Apache Friends website: https://www.apachefriends.org/download.html

   4. MySQL: The Fitness Tracking Application uses MySQL as the database management system. It will be installed as part of the XAMPP package.


=========================================================================================================================================

Steps to Run Fitness Tracking Application


1. Import the SQL File:
     - Open phpMyAdmin by accessing http://localhost/phpmyadmin in your web browser.
     - Create a new database named fitness_kzo.
     - Click on the "Import" tab.
     - Choose the "Browse" button and select the provided SQL file containing the database schema and initial data.
     - Scroll down and click the "Go" button to import the SQL file.

3. Open Backend project in Visual Studio Code:
     - Launch Visual Studio Code.
     - Open the backend project folder(File > Open) .
    
3. Update the Database Connection String:
     - Open the database.php file located in the project.
     - Locate the following line of code:

     $servername = "localhost";
     $username = "root";
     $password = "";
     $database = "fitness_kzo";

     - Update the $username and $password values with your MySQL database credentials, if necessary.
     - Save the changes to the database.php file.

4. Change the base URL in android studio
     - Double-click on RetrofitInstance.kt inside the ApiBackend folder under com.example.fitnesstrackingapp package
     - private val BASE_URL: String = "http://localhost/folderName/" change your IP address and folder name

5. Update Api Key in android studio
    - Open the string.xml file under app/res/values/. Add your Api key in here:
    
	<string name="map_key">YOUR_API_KEY_HERE</string>

    - Replace YOUR_API_KEY_HERE with the actual API key you generated from the Google Cloud Console.
 
6. Running on Android Devices (Virtual Device Emulator)
    - Open Android Studio
    - Go to Tools > Device Manager.
    - Create a new virtual device (e.g., Pixel 5 with API level 30+).
    - Start the emulator.
    - Click Run in Android Studio.

   (Real Device)
    - Enable Developer Options on your Android phone.
    - Enable USB Debugging.
    - Connect your device via USB and trust the computer.
    - Select the device in Android Studio and click Run.

!!Make sure your phone and computer are connected to the same Wi-Fi network.!!
=========================================================================================================================================

Testing Credentials
To test the Fitness Tracking App, you can use the following credentials:
	
	email : khin@gmail.com
	password : Password

Remember to update the database connection string in the database.php if your MySQL database settings differ from the provided example.