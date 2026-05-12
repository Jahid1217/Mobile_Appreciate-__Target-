# GoalFlow

GoalFlow is a Jetpack Compose Android app for creating, tracking, and reviewing goals with local Room storage.

## Features

- Create daily, monthly, and yearly goals.
- Track progress from the dashboard and daily check-in screen.
- Confirm completion before marking a goal done.
- Complete today's remaining sets in one action after confirmation.
- Stop further reminders for a goal once it is completed for the day.
- Review reminders and reports with charts, completion rates, and missed-day history.
- Enable, disable, and customize reminders per goal.
- Store all app data locally with Room.

## Completion Flow

When a user taps `Complete`, the app now shows a confirmation dialog.

After confirmation:

- all remaining progress for that goal is recorded for the current day,
- any remaining reminders for that day are cancelled,
- the report updates the goal as completed for that day.

## Screens

- Dashboard
- Targets
- Daily check-in
- Reminders and reports
- Profile

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- StateFlow
- ViewModel
- Navigation Compose

## Setup

1. Open the project in Android Studio.
2. Sync Gradle and let dependencies download.
3. Run the app on an emulator or physical device running Android 8.0 or newer.
4. On Android 13+, allow notification permission so reminders can appear.

## Project Structure

- `app/src/main/java/com/example/shops/MainActivity.kt` - app entry point.
- `app/src/main/java/com/example/shops/GoalsViewModel.kt` - state management, Room access, and completion/reminder logic.
- `app/src/main/java/com/example/shops/data/GoalDatabase.kt` - Room entities, DAO, and database setup.
- `app/src/main/java/com/example/shops/model/GoalModels.kt` - goal and report models.
- `app/src/main/java/com/example/shops/reminders/ReminderReceiver.kt` - reminder notification receiver.
- `app/src/main/java/com/example/shops/reminders/ReminderScheduler.kt` - alarm scheduling and cancellation.
- `app/src/main/java/com/example/shops/ui/navigation/AppNavGraph.kt` - navigation wiring.

## Notes

- The app label is set to `target`.
- Data is stored locally in Room.
- The repository uses a destructive migration strategy for the current development setup.
