#ifndef CONFIG_H
#define CONFIG_H

// ==========================================
// 1. Wi-Fi Configuration
// ==========================================
// Enter your home/college Wi-Fi network credentials
#define WIFI_SSID           "Your_WiFi_SSID"
#define WIFI_PASSWORD       "Your_WiFi_Password"

// ==========================================
// 2. Spring Boot Backend Server Configuration
// ==========================================
// Replace with the local IP address of the computer running Spring Boot
// (e.g. "http://192.168.1.100:8080")
#define SERVER_HOST         "http://192.168.1.100:8080"
#define TELEMETRY_ENDPOINT  "/api/v1/iot/telemetry"
#define COMMANDS_ENDPOINT   "/api/v1/iot/commands"
#define ACK_ENDPOINT        "/api/v1/iot/commands/"

// Secure Device Authentication Token (must match application.yml)
#define DEVICE_ID           "ESP32_ENERGYEX_01"
#define DEVICE_AUTH_TOKEN   "ENERGYEX_IOT_SECURE_TOKEN_2026"

// ==========================================
// 3. Hardware Pin Definitions (ESP32 WROOM-32)
// ==========================================
// PZEM-004T V3.0 UART Serial Pins (HardwareSerial 2)
#define PZEM_RX_PIN         16 // Connects to PZEM-004T TX pin
#define PZEM_TX_PIN         17 // Connects to PZEM-004T RX pin

// 4-Channel Relay Module Control Pins (Active-LOW Optocoupler Trigger)
#define RELAY_CH1_PIN       18 // Appliance 1: Living Room Light
#define RELAY_CH2_PIN       19 // Appliance 2: Ceiling Fan
#define RELAY_CH3_PIN       21 // Appliance 3: Smart TV
#define RELAY_CH4_PIN       22 // Appliance 4: Refrigerator

// Onboard Status LED Pin
#define STATUS_LED_PIN      2

// ==========================================
// 4. Operational Timings (Milliseconds)
// ==========================================
#define TELEMETRY_INTERVAL_MS   3000 // Send sensor readings every 3 seconds
#define COMMAND_POLL_INTERVAL_MS 1500 // Check for remote relay commands every 1.5s
#define WIFI_RECONNECT_INTERVAL 5000

#endif // CONFIG_H

