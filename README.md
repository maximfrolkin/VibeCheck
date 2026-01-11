# VibeCheck

VibeCheck is a modern Android application that helps you stay up-to-date with the latest English slang. It uses the Gemini AI (Flash-Lite) to analyze real-time trends across social media platforms like TikTok, X (Twitter), and Instagram to provide you with a "Slang of the Day."

## Features

*   **AI-Powered Trends**: Identifies trending slang from the last 24 hours using Gemini AI.
*   **Glance Widget**: A beautiful home screen widget that shows the word, its translation, context, and a popularity "fire" score.
*   **Multilingual Support**: Translate slang into multiple languages including Russian, English, Spanish, German, and French.
*   **Manual & Auto Refresh**: Updates automatically every 24 hours using WorkManager, with a manual refresh button on both the app and the widget.
*   **Social Sharing**: Easily share the trending slang with your friends.

## Technical Highlights

*   **Jetpack Compose**: Modern UI development for the main app.
*   **Jetpack Glance**: Used for building the interactive app widget.
*   **WorkManager**: Handles background tasks for periodic slang updates.
*   **DataStore**: Secure and reactive storage for app settings and slang data.
*   **Gemini API**: Integration with Google's latest AI models for trend analysis.

## Setup Instructions

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/maximfrolkin/VibeCheck.git
    ```

2.  **API Key Setup**:
    To protect your privacy, the API key is not included in the repository.
    *   Create a `local.properties` file in the root directory (if it doesn't exist).
    *   Add your Gemini API key:
        ```properties
        GEMINI_API_KEY=your_api_key_here
        ```
    *   You can obtain an API key from the [Google AI Studio](https://aistudio.google.com/).

3.  **Build and Run**:
    Open the project in Android Studio and run it on an emulator or a physical device.

## Privacy & Security

*   The **API Key** is stored locally in `local.properties` and is never committed to version control.
*   User preferences are stored locally on the device using **Jetpack DataStore**.
*   No personal data is collected or sent to third-party servers except for the slang analysis request sent to the Gemini API.

## License

This project is open-source and available under the [MIT License](LICENSE).
