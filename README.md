# GoalFlow

GoalFlow is a modern Android goal-tracking app built with Jetpack Compose and Room. It helps users create personal targets, log daily progress, manage reminders, and review performance through reports and charts.

## Overview

The app is designed for daily habits and long-term targets such as:

- drinking water
- walking or exercise goals
- savings goals
- reading goals
- custom personal targets

All app data is stored locally on the device, so the app works offline and keeps user data private.

## What You Can Do

- Create daily, monthly, and yearly goals.
- Set a target value, unit, and date range for each goal.
- Edit or delete existing goals.
- Log progress from the daily check-in screen.
- Mark a goal as complete with confirmation before finishing the day.
- Automatically complete the remaining sets for that goal on the same day.
- Stop further reminders for that goal once completion is confirmed.
- Turn reminders on or off per goal.
- Review progress summaries, completion rates, and missed-day reports.
- Use charts and calendar views to understand consistency over time.

## Main Screens

### Dashboard
The dashboard gives a quick overview of active goals, progress, and completion status.

### Targets
Create, edit, and manage all goals from one place.

### Daily Check-In
Log progress for today's active goals and confirm completion when a target is done.

### Reminders and Reports
View reminder coverage, missed-day reports, completion history, and analytics charts.

### Profile
Store basic user profile information and keep the app experience more personalized.

## Completion Behavior

When the user taps `Complete`:

1. A confirmation dialog appears.
2. After confirmation, the goal is marked complete for the current day.
3. Any remaining sets for that day are treated as completed.
4. No more reminders are shown for that goal on the same day.
5. The report and charts update to show the goal as completed.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- ViewModel
- StateFlow
- Navigation Compose
- AlarmManager and notifications for reminders

## Setup

1. Open the project in Android Studio.
2. Sync Gradle and wait for dependencies to download.
3. Run the app on an emulator or a physical device.
4. On Android 13 and above, allow notification permission so reminders can appear.

## Project Structure

- `app/src/main/java/com/example/shops/MainActivity.kt` - app entry point.
- `app/src/main/java/com/example/shops/GoalsViewModel.kt` - state management, goal actions, report generation, and reminder sync.
- `app/src/main/java/com/example/shops/data/GoalDatabase.kt` - Room database, entities, and DAO definitions.
- `app/src/main/java/com/example/shops/model/GoalModels.kt` - UI models and report models.
- `app/src/main/java/com/example/shops/reminders/ReminderScheduler.kt` - schedules and cancels reminders.
- `app/src/main/java/com/example/shops/reminders/ReminderReceiver.kt` - shows reminder notifications.
- `app/src/main/java/com/example/shops/reminders/ReminderPopupActivity.kt` - reminder popup and action handling.
- `app/src/main/java/com/example/shops/ui/navigation/AppNavGraph.kt` - navigation setup.
- `app/src/main/java/com/example/shops/ui/screens/*` - UI screens for dashboard, targets, check-in, reminders, and profile.

## Notes

- The app label is set to `target`.
- Data is stored locally in Room.
- The current development build uses destructive migration for simplicity.
