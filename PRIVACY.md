# Gamma Privacy Policy

This privacy policy explains how the **Gamma** Android application ("the App") handles your data. Gamma is an open-source client for the **Pnut** social network.

## 1. Scope
This policy applies only to the App. It does **not** cover the Pnut social network itself, which maintains its own privacy policy regarding data stored on its servers.

## 2. Information Collected by the App
The App is designed to minimize the collection of personal data.

### Local Storage
The App stores the following information locally on your device to enable its functionality:
*   **Authentication Token**: Used to communicate with the Pnut API on your behalf.
*   **Account Metadata**: Your user ID, screen name, and display name.
*   **App Settings**: Your preferences (e.g., theme, notification settings).

This data remains on your device and is only transmitted to the Pnut API to perform actions you initiate (like posting or fetching your feed).

### Third-Party Services (Google Firebase)
To help us improve the App and fix bugs, we use Google Firebase:
*   **Firebase Crashlytics**: If the App crashes, it collects stack traces and basic device information (model, OS version) to help us diagnose the issue.

Crashlytics is only used in the app if a `google-services.json` file is placed in the `/app` folder. For more information, please see [Google's Privacy Policy](https://policies.google.com/privacy).

## 3. Permissions
The App requests the following permissions:
*   **Internet**: Required to communicate with the Pnut social network.
*   **Storage (Write/Read)**: Used only if you choose to upload images to the social network or save images to your device.

## 4. Data Retention and Deletion
*   **Local Data**: You can delete all locally stored data at any time by logging out of the App or uninstalling it through your device settings.
*   **Analytics/Crash Data**: This data is stored by Google Firebase and is generally anonymized.

## 5. Open Source
Gamma is open-source software. You can review the full source code and how it handles your data at: https://github.com/33mhz/gamma

## 6. Contact
If you have any questions about this privacy policy, please open an issue on our GitHub repository.
