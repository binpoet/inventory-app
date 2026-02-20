/**
 * Zebra RFID SDK for Android (RFIDAPI3) - Library module.
 *
 * SETUP: Download Zebra RFID SDK from
 * https://www.zebra.com/us/en/support-downloads/software/rfid-software/rfid-sdk-for-android.html
 * Unzip the package, locate the .aar file, rename it to API3_LIB-release.aar,
 * and place it in this RFIDAPI3Library folder.
 */
configurations.maybeCreate("default")
artifacts.add("default", file("API3_LIB-release.aar"))
