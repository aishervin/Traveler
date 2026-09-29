# Baarbarg Transport - Android Native App

A fully native Android logistics and transport management application built with **Kotlin** and **Jetpack Compose**, rewritten from React Native/Expo.

## Features

- **Authentication**: Secure login with National Code and Password, supporting Demo Mode.
- **Dashboard**: Quick overview and service tiles (Baarbarg Hagigi, Daily, Carrying, History, Wallet, Fuel, Inbox, Settings).
- **Shipments Management**: View and filter Issued and Carrying transport documents with search.
- **Trip Tracking**: Start and finish trips with GPS coordinates and active trip management.
- **Gemini Automation Assistant**: Integrated AI assistant for transport queries and automation tasks.
- **Settings**: Manage API configuration, Gemini API key securely, and automation policies.

## Architecture

- **UI**: Jetpack Compose with Material Design 3.
- **Local Storage**: SharedPreferences / Encrypted storage for session tokens and Gemini keys.
- **Architecture**: MVVM pattern with Kotlin Coroutines.

## Build and Verification

Built with Gradle (Kotlin DSL) and verified using `compile_applet`.
