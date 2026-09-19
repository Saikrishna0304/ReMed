# ReMed

ReMed is an Android health assistant for medication adherence, hydration, and daily step goals. It helps users enter prescriptions manually or from images, receive reminders, and keep track of everyday health routines.

## Features

- Add, edit, and review medications.
- Scan prescription images with the camera or select them from the gallery.
- Extract medication details with Google ML Kit text recognition.
- Enter handwritten characters using the bundled TensorFlow Lite model.
- Mark medication doses as taken and schedule medication reminders.
- Track daily water intake with configurable goals, quick-add amounts, and reminders.
- Track daily steps and configure a step goal.
- Sign in with email/password or Google Sign-In.
- Use the app as a guest without signing in.
- Create or join a family using a generated join code.

## Technology Stack

- Kotlin 2.1.0
- Jetpack Compose and Material 3
- Android Gradle Plugin 8.13.2
- Gradle 9.3.1
- Room for local health records
- Firebase Authentication and Firestore for identity and family metadata
- Google ML Kit for Latin text recognition
- TensorFlow Lite for handwriting recognition
- CameraX for camera capture
- WorkManager and AlarmManager for reminders

## Requirements

- Android Studio with Android SDK platforms 34 and 35 installed
- JDK 17
- An Android device or emulator running Android 8.0 (API 26) or newer
- Network access while Gradle downloads dependencies
- A Firebase project configured for the application ID `com.example.remed`

The project compiles with SDK 35, targets SDK 34, and supports a minimum SDK of 26.

## Getting Started

1. Clone or open this repository in Android Studio.
2. Set up the Android SDK path in `local.properties` for your machine. Do not copy another developer's machine-specific path.
3. Confirm that `app/google-services.json` belongs to the Firebase project you intend to use.
4. Enable Email/Password authentication, Google Sign-In, and Firestore in Firebase.
5. Add the SHA-1 and SHA-256 fingerprints for the development device or signing key when configuring Google Sign-In.
6. Sync the project with Gradle.
7. Run the `app` configuration on a device or emulator.

From Windows PowerShell or Command Prompt, the main build commands are:

```bat
gradlew.bat assembleDebug
gradlew.bat installDebug
```

To run the available verification tasks:

```bat
gradlew.bat test
gradlew.bat connectedAndroidTest
```

The repository currently declares test dependencies but does not contain checked-in unit, instrumentation, or Compose UI test files.

## Firebase Configuration

Firebase is used for authentication, user profiles, and family metadata. The application reads its Android Firebase configuration from `app/google-services.json`.

The following Firestore paths are used:

- `users/{uid}`
- `families/{familyId}`
- `_connection_test_/test` for connectivity checks

Medication, hydration, and step records are stored locally in Room and are not synchronized to Firestore. Before distributing the app, review Firebase security rules and restrict the registered Android app and OAuth clients appropriately. The Google OAuth server client ID is currently configured in the login implementation, so production deployments should use environment-specific configuration.

## Architecture

ReMed is a single-module Android application using a lightweight MVVM and repository structure:

```text
Jetpack Compose UI
	|
    ViewModels
	|
ReMedRepository / AuthRepository
	|
 Room DAOs / Firebase Auth / Firestore
```

Important source areas:

- `app/src/main/java/com/example/remed/MainActivity.kt` - startup permissions and state-based screen routing.
- `app/src/main/java/com/example/remed/ui/components/` - Compose screens for login, family setup, dashboard, scanning, and medication editing.
- `app/src/main/java/com/example/remed/ui/` - authentication, medication, hydration, and step ViewModels.
- `app/src/main/java/com/example/remed/data/` - Room entities, DAOs, repositories, and user/family models.
- `app/src/main/java/com/example/remed/ocr/` - prescription OCR, handwriting processing, and medication parsing.
- `app/src/main/java/com/example/remed/notifications/` - alarms, notification actions, receivers, and background work.

The app does not use Navigation Compose. Dashboard and scanner routing are controlled by Compose state in `MainActivity`.

## Local Storage

Room database details:

- Database name: `remed_database`
- Database version: 4
- Entities: `Medication`, `WaterLog`, `WaterSettings`, `StepLog`, and `StepSettings`
- Guest records use the local user ID `GUEST_USER`
- Default water goal: 2,000 ml
- Default quick-add amount: 250 ml
- Default water reminder interval: 60 minutes
- Default step goal: 5,000 steps

The database uses `fallbackToDestructiveMigration()`. A schema change without a migration can delete local Room data. Android backup and device-transfer rules include the local database, so the app should not be described as strictly device-only storage for every deployment.

## OCR and Handwriting

Prescription scanning uses ML Kit Latin text recognition, then passes the result to `MedicationParser`. The parser understands labels such as `Medication:`, `Name:`, `Rx:`, `Dosage:`, and `Frequency:` and also applies dosage/frequency heuristics.

Handwriting recognition uses `app/src/main/ml/handwriting_recognition_model.tflite`. Input is converted to grayscale, resized to 28x28 pixels, normalized, and passed through TensorFlow Lite. The current model and processor are designed for individual alphanumeric characters rather than complete handwritten prescriptions.

## Reminders and Permissions

The manifest declares permissions for:

- Camera access
- Notifications on Android 13+
- Exact alarms on Android 12+
- Vibration

Medication reminders use exact alarms and can schedule a follow-up notification five minutes later. Hydration reminders use periodic WorkManager work, with intervals below 15 minutes clamped to WorkManager's minimum. The app does not currently declare a boot receiver, so exact medication alarms may need to be recreated after a device restart.

## Current Limitations

- Medication frequency is stored and displayed, but recurring medication alarm generation is not implemented.
- The step counter sensor listener is registered, but sensor events are not currently persisted; automatic step tracking is incomplete.
- Handwriting recognition returns one character per inference and may require manual correction.
- No Firestore security rules are checked into this repository.
- Notification receivers currently use `GlobalScope`, which may outlive their receiver lifecycle.
- There is no release signing configuration in the repository.

## License

This project is licensed under the [GNU General Public License v3.0](file:///K:/Codeing/Projects/ReMed/GIT/app/LICENCE). See the license file for details.
