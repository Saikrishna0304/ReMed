# ReMed

ReMed is a personal health assistant Android application designed to help users manage their medications and stay hydrated.

## Features

*   **Medication Management:**
    *   Add medications with dosage and frequency.
    *   Receive timely reminders for taking medications.
    *   Mark medications as taken.

*   **Hydration Tracking:**
    *   Track daily water intake.
    *   Set custom reminder intervals to stay hydrated.
    *   Receive interactive notifications to quickly log water intake (adds 200ml when you confirm).

*   **Prescription Scanning:**
    *   Uses your device's camera to scan prescription labels.
    *   Utilizes Google's ML Kit for on-device Text Recognition to extract medication names.
    *   (Note: The parsing logic is currently basic and serves as a proof-of-concept).

## Core Technologies

*   **UI:** 100% built with [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern, declarative UI development.
*   **Asynchronous Programming:** Uses [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://developer.android.com/kotlin/flow) for all background tasks and data streams.
*   **Background Processing:** Leverages [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable and battery-efficient background tasks, such as scheduling notifications.
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) is used for local persistence of medication and water log data.
*   **Text Recognition:** Implements [Google's ML Kit](https://developers.google.com/ml-kit/vision/text-recognition) for on-device OCR.
*   **Architecture:** Follows a standard ViewModel and Repository pattern.

## Setup & Build

1.  Clone this repository.
2.  Open the project in a recent version of Android Studio.
3.  Let Gradle sync and download all dependencies.
4.  Build and run the application on an emulator or a physical device.

### Important Note for Android 13+

To receive notifications, you **must** grant the "Post Notifications" permission when the app requests it upon first launch. If you deny it, you will need to manually enable the permission in your device's system settings for the app.
