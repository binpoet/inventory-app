# Zebra RFID SDK – No longer used as a module

The app now uses the Zebra RFID SDK **directly from the project root**:

- **Folder:** `Zebra_RFIDAPI3_SDK_2.0.5.238/` (in the same directory as `app/` and `RFIDAPI3Library/`)
- **Dependency:** All `*.aar` files in that folder are included via `fileTree` in `app/build.gradle.kts`.

No need to copy any AAR into this `RFIDAPI3Library` folder. You can keep or delete this folder.
