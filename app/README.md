
# BioTrack Oulu: Patient Recovery Monitor

**Student:** Hadi Rezaeikarjani (Hadi)

**Project Type:** Solo Project (Integrated HW1–HW4 Bundle)

## Project Overview

BioTrack is a mobile application designed for patient recovery monitoring in Oulu, Finland. It combines real-time environmental data, physical activity tracking logic, and personal health logging to provide a cohesive recovery tool.

---

## 🛠 Implemented Features (Project: 15 Points)

The following features were implemented to satisfy the solo project requirements:

1.
**Database Integration (5p):** Users can add new health entries (text notes and image URIs) which are displayed in a historical log via a Room Database.


2.
**API Usage (5p):** The app utilizes the **OpenWeatherMap API** to fetch real-time temperature data for Oulu, helping patients decide if conditions are suitable for outdoor recovery activities.


3.
**Animated Splash Screen (5p):** A custom animated splash screen replaces the default Android startup, featuring the BioTrack heartbeat logo.


4.
**Runtime Permissions:** The app implements runtime checks and requests for **Notifications** and **Gallery** access, adapting its behavior based on user approval.



---

## 📝 HW1–HW4 Retrospective (10 Points)

As these assignments were bundled into the final project, the following documentation addresses the required technical questions:

HW1: Accessibility & UI

**Question:** If the user increases font size in Android settings, how does your app look?

**Response:** The app uses `sp` for typography and `dp` for layout dimensions. In the Dashboard, I implemented a `verticalScroll` state, and the History log uses a `LazyColumn`. If the user increases the font size, the UI remains accessible and readable as the content scales naturally, allowing the user to scroll through health logs and weather data without any UI elements overlapping.

HW2: Navigation & Flow

**Question:** Include a chart of the different views. Which views are users expected to spend the most time in?

**Response:** The app features two primary views: the **Dashboard** and the **History Log**. Users are expected to spend 80% of their time in the Dashboard to log daily vitals and check weather data. It takes exactly one step to reach the History view. I utilized `popUpTo` in the `NavHost` to ensure the back gesture returns to the Dashboard and then exits the app, preventing circular navigation loops.

HW3: Persistence & Database

**Question:** What are you storing in a database? How many bytes is it estimated to take?

**Response:** I use a **Room Database** to store `HealthEntry` objects, which include a text note (String), an image URI (String), and a timestamp (Long).  Each entry is estimated to take approximately 500 bytes. To further improve the user experience, the database could be expanded to store categorized vitals such as blood pressure, heart rate, or GPS coordinates for outdoor recovery tracking.

HW4: Sensors, APIs & Reliability

**Question:** What sensors/APIs are you using? What happens if the internet is unstable?

**Response:** I utilize the **OpenWeatherMap API** to provide real-time data for Oulu.  If the internet is unstable or disconnected, the app uses a `try-catch` block to handle the `UnknownHostException`. Instead of crashing, the app displays a user-friendly error message on the Dashboard, ensuring that offline features (like viewing saved history) remain fully functional.

---

## 📜 Borrowed Code Statement

As per the course general instructions:

* The overall architectural structure for **Navigation (`NavHost`)**, **Room Database** setup, and **Retrofit API** integration was developed with the assistance of **Gemini AI** to ensure compliance with the specific technical requirements of the homework-to-project bundling.



---

## 🎥 Video Demonstration

A video recording showing all features (Splash screen, Database entries, API weather fetch, and Background notifications) is included in the submission.

