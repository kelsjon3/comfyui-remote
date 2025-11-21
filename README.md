# ComfyUI Remote

A two-part system to control ComfyUI from an Android application.

## Project Structure

```
comfyui-remote/
├── wrapper-api/       # Python FastAPI backend
│   ├── main.py        # Entry point
│   ├── routers/       # API endpoints
│   ├── Dockerfile     # Container definition
│   └── ...
└── android-app/       # Kotlin Android application
    ├── app/           # Main module
    ├── build.gradle.kts
    └── ...
```

## Architecture

1.  **ComfyUI**: The underlying image generation engine (running locally or on a server).
2.  **Wrapper API**: A middleware service that exposes a simplified REST API for the mobile app. It communicates with ComfyUI.
3.  **Android App**: The user interface for browsing workflows, starting jobs, and viewing results. It communicates ONLY with the Wrapper API.

## Getting Started

### Backend

See [wrapper-api/README.md](wrapper-api/README.md) for instructions on running the backend service.

### Android App

See [android-app/README.md](android-app/README.md) for instructions on building and running the Android app.

## Next Steps

1.  **Backend**: Implement actual logic in `wrapper-api` to talk to your ComfyUI instance.
2.  **Android**: Implement Retrofit calls and connect them to the UI.
3.  **Integration**: Test the end-to-end flow from Android -> Wrapper -> ComfyUI.