# ComfyUI Remote - Android App

Native Android application for controlling ComfyUI via the Wrapper API.

## Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit

## Setup

1. Open `android-app` folder in Android Studio.
2. Sync Gradle project.
3. Create a `local.properties` file if not present (usually auto-generated).
4. Run on Emulator or Physical Device.

## Modules

- `app`: Main application module.

## Key Components

- `MainActivity`: Entry point, hosts Navigation Graph.
- `ComfyApiService`: Retrofit interface for Wrapper API.
- `WorkflowViewModel`: Manages UI state for workflow screens.

## TODO

- Implement real API calls in `ComfyApiService`.
- Create data models for Workflows and Jobs.
- Implement UI for Node Editor and Image Gallery.
- Add dependency injection (Hilt).
