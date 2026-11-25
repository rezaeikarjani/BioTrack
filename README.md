# BioTrack Oulu: Patient Recovery Monitor

An Android app (Kotlin, Jetpack Compose) for people recovering at home. It combines a daily health log, step counting and local weather, so a patient can see their progress and decide whether conditions suit outdoor activity.

Built as a solo project for the Mobile Computing course at the University of Oulu. The original course write-up (design questions on accessibility, navigation, storage and reliability) is in [app/README.md](app/README.md).

## Features
- **Health log:** text notes and photos saved with a timestamp in a **Room** database and shown in a history view
- **Step counting:** the device step-counter sensor (`TYPE_STEP_COUNTER`)
- **Weather for Oulu:** current temperature and humidity from the **OpenWeatherMap API** via Retrofit, with friendly handling when the network is unavailable
- **Notifications and runtime permissions** for notifications and gallery access
- **Animated splash screen** and two-screen navigation (Dashboard and History) with `NavHost`

## Tech stack
Kotlin · Jetpack Compose · Material 3 · Room · Retrofit + Gson · Coil · Navigation Compose · Android SensorManager

## Run it
1. Open the project in Android Studio (min SDK 34).
2. Get a free API key from [OpenWeatherMap](https://openweathermap.org/api).
3. Add it to `local.properties` in the project root (this file is not committed):
   ```
   OPENWEATHER_API_KEY=your_key_here
   ```
4. Build and run on a device or emulator.

## Notes
The navigation, Room and Retrofit scaffolding was developed with help from an AI assistant, as stated in the course submission.
