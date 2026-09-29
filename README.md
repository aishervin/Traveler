# صدور بارنامه شهری

A native Android transport-management app built with Kotlin and Jetpack Compose. The interface follows the original Persian app branding and right-to-left layout.

## Features

- **Authentication**: Server-backed login with National Code, password, and optional captcha token; Demo Mode remains local only.
- **Shipments**: Fetch issued and carrying documents from the Baarbarg API and search them locally.
- **Trip tracking**: Start/end calls use current GPS; a foreground service records encrypted location points every 20 seconds while a trip is active.
- **Branding**: Original logo, service icons, background art, IRANSans Mobile fonts, and light teal color palette.
- **Gemini Automation Assistant**: Integrated AI assistant for transport queries and automation tasks.
- **Settings**: Configure the HTTPS API URL and optional service headers; sensitive values are encrypted on-device.

## Architecture

- **UI**: Jetpack Compose with Material Design 3.
- **Networking**: OkHttp with bounded timeouts, HTTPS-only server configuration, and Persian service errors.
- **Local Storage**: Android EncryptedSharedPreferences for the login token, service keys, and Gemini key.

## Build and Verification

The repository is a native Gradle project. CI runs `testDebugUnitTest` and `assembleDebug` with Java 17, Android SDK 34, and Gradle 8.7.

- **Tool catalog and test download**: [GitHub Pages](https://aishervin.github.io/Traveler/)
- **Latest test APK**: [Traveler-debug.apk](releases/Traveler-debug.apk)

The app uses the original service URL by default. Users with authorized service access may change it and enter optional `ServicePassword` / `SecurityKey` headers under **Settings**. These credentials are not included in source control. The service's live login and trip mutations require an authorized account; Demo Mode never sends these requests.
