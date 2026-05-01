# Enveloppe

A simple Android app for managing budgets using cash stuffing.

## Prérequis

- JDK 17+
- Android SDK (API 35)
- Un appareil Android ou émulateur avec Android 8.0+ (minSdk 26)
- ADB pour l'installation sur appareil physique

## Build

```bash
# Debug
./gradlew assembleDebug
# APK généré : app/build/outputs/apk/debug/app-debug.apk

# Release
./gradlew assembleRelease
# APK généré : app/build/outputs/apk/release/app-release.apk
```

## Signer la release

```bash
~/Android/Sdk/build-tools/34.0.0/apksigner sign --ks ~/.android/keystores/my-app-release.jks ./app/build/outputs/apk/release/app-release-unsigned.apk
```

## Lancer l’émulateur

```bash
adb start-server
~/Android/Sdk/emulator/emulator -avd Pixel9
```

## Installer sur un appareil

Option 1 — Build + installation en une commande (débogage USB activé sur l'appareil) :

```bash
./gradlew installDebug
```

Option 2 — Installation manuelle avec ADB :

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```
