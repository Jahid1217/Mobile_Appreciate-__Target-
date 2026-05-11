GoalFlow - Mobile Goal Tracking App
GoalFlow is a modern Android application designed to help users create, track, and achieve their goals (targets) with ease. Whether it's a daily habit, a monthly savings target, or a yearly learning objective, GoalFlow provides the tools to keep you on track.

target
target is a Jetpack Compose Android app for managing goals, reminders, target date ranges, and missed-date reporting with local Room storage.

App Image
target icon

🚀 Features
🎯 Goal Creation & Management
Custom Targets: Create goals with names, units (steps, mins, etc.), and target values.
Tracking Types: Support for Daily, Monthly, and Yearly goal types, each with its own progress calculation.
Date Ranges: Set a specific "From" and "To" date range for every target.
Edit & Delete: Full CRUD support for managing your list of active and archived targets.
✅ Tracking & Check-ins
Daily Check-in Screen: A dedicated area to log progress for today's active targets.
Incremental Progress: Easily add progress or mark a goal as completed for the day.
Progress Visuals: Dynamic progress bars and percentage indicators.
📊 Analytics & Reporting
Dashboard: A summary view showing active targets and overall average progress.
Missed Dates Report: Automatically identifies and lists dates where targets were not met within their active range.
Goal Types Color-Coding: Teal for Daily, Orange for Monthly, and Blue for Yearly goals.
🔔 Reminders Management
Local Notifications: Set daily reminders for each goal at a specific time.
Toggle Support: Enable or disable reminders individually per target.
Custom Timing: Pick the exact hour and minute for your daily motivational nudge.
💾 Data & Tech Stack
Local Database: Powered by Room Persistence Library for fast, offline-first data storage.
Modern UI: Built entirely with Jetpack Compose and Material 3.
Reactive State: Uses StateFlow and ViewModel for a smooth, responsive user experience.
📸 Screenshots
Dashboard	My Targets	Check-in	Reminders
Dashboard	Targets	Check-in	Reminders
🛠 Setup & Installation
Clone the repository.
Open in Android Studio (Ladybug or newer recommended).
Sync Gradle to download dependencies (Room, Navigation, Material 3, etc.).
Run the app on an emulator or physical device with Android 8.0 (Oreo) or higher.
📝 License
This project is for demonstration purposes and is open for further expansion.

Create and edit targets with a from and to date range.
Enable or disable local reminders for each target.
Delete targets from the app.
Check progress from the dashboard and daily check-in screens.
View a missed-dates report for unfinished days.
Store all app data locally with Room.
Project Files
Kotlin source
app/src/main/java/com/example/shops/MainActivity.kt - Compose navigation and screens.
app/src/main/java/com/example/shops/GoalsViewModel.kt - UI state, Room access, reminders, and missed-date reporting.
app/src/main/java/com/example/shops/data/GoalDatabase.kt - Room entities, DAO, and database setup.
app/src/main/java/com/example/shops/model/GoalModels.kt - Goal and report models.
app/src/main/java/com/example/shops/reminders/ReminderReceiver.kt - Notification receiver.
app/src/main/java/com/example/shops/reminders/ReminderScheduler.kt - Alarm scheduling and cancel logic.
app/src/main/java/com/example/shops/ui/theme/Color.kt - Theme colors.
app/src/main/java/com/example/shops/ui/theme/Theme.kt - App theme wrapper.
app/src/main/java/com/example/shops/ui/theme/Type.kt - Typography settings.
Resources
app/src/main/res/values/strings.xml - App name and string resources.
app/src/main/res/values/themes.xml - Android theme configuration.
app/src/main/res/values/colors.xml - Base color resources.
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml - Adaptive launcher icon.
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml - Round adaptive launcher icon.
app/src/main/res/mipmap-mdpi/ic_launcher.jpg - Launcher icon asset.
app/src/main/res/mipmap-mdpi/ic_launcher_round.jpg - Round launcher icon asset.
app/src/main/res/mipmap-hdpi/ic_launcher.jpg - Launcher icon asset.
app/src/main/res/mipmap-hdpi/ic_launcher_round.jpg - Round launcher icon asset.
app/src/main/res/mipmap-xhdpi/ic_launcher.jpg - Launcher icon asset.
app/src/main/res/mipmap-xhdpi/ic_launcher_round.jpg - Round launcher icon asset.
app/src/main/res/mipmap-xxhdpi/ic_launcher.jpg - Launcher icon asset.
app/src/main/res/mipmap-xxhdpi/ic_launcher_round.jpg - Round launcher icon asset.
app/src/main/res/mipmap-xxxhdpi/ic_launcher.jpg - Launcher icon asset.
app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.jpg - Round launcher icon asset.
app/src/main/res/drawable/ic_launcher_foreground.jpg - Shared foreground image used by the launcher.
app/src/main/res/xml/backup_rules.xml - Backup configuration.
app/src/main/res/xml/data_extraction_rules.xml - Data extraction rules.
Build files
build.gradle.kts - Root Gradle configuration.
settings.gradle.kts - Project module settings.
app/build.gradle.kts - App module dependencies and plugins.
gradle/libs.versions.toml - Version catalog.
gradle/wrapper/gradle-wrapper.properties - Gradle wrapper version.
Run It
Open the project in Android Studio.
Sync Gradle so Room and KSP are downloaded.
Run the app configuration on an emulator or device.
On Android 13+, allow notification permission so reminders can show.
Notes
The app label is set to target.
Data is stored locally in Room.
The launcher icon uses the new ic_launcher.jpg artwork.