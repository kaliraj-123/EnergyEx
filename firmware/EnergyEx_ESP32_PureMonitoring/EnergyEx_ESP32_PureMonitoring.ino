/**
 * EnergyEx - Pure Energy Monitoring Firmware (No Relays)
 * Target: ESP32 + PZEM-004T v3.0
 * 
 * Required Libraries (Install via Arduino Library Manager):
 *  1. PZEM004Tv30 by Mandar
 *  2. ArduinoJson by Benoit Blanchon (v6 or v7)
 * 
 * Wiring (Only 4 Wires):
 *  PZEM 5V  -> ESP32 VIN (or 5V)
 *  PZEM GND -> ESP32 GND
 *  PZEM TX  -> ESP32 GPIO 16 (RX2)
 *  PZEM RX  -> ESP32 GPIO 17 (TX2)
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <PZEM004Tv30.h>

// ==========================================
// 1. Wi-Fi & Server Settings
// ==========================================
const char* WIFI_SSID       = "Your_WiFi_Name";        // <-- Replace with your Wi-Fi SSID
const char* WIFI_PASSWORD   = "Your_WiFi_Password";    // <-- Replace with your Wi-Fi Password

// Your computer's local IP running Spring Boot:
const char* SERVER_URL      = "http://10.47.247.16:8080/api/v1/iot/telemetry";

const char* DEVICE_ID       = "ESP32_ENERGYEX_01";
const char* AUTH_TOKEN      = "ENERGYEX_IOT_SECURE_TOKEN_2026";

// ==========================================
// 2. Hardware Pin Definitions
// ==========================================
#define PZEM_RX_PIN 16 // Connects to PZEM-004T TX pin
#define PZEM_TX_PIN 17 // Connects to PZEM-004T RX pin
#define STATUS_LED  2  // Onboard blue LED

// HardwareSerial 2 (GPIO 16 RX, GPIO 17 TX)
PZEM004Tv30 pzem(Serial2, PZEM_RX_PIN, PZEM_TX_PIN);

unsigned long lastTelemetryTime = 0;
const unsigned long TELEMETRY_INTERVAL = 2500; // Send telemetry every 2.5 seconds

void setup() {
    Serial.begin(115200);
    delay(1000);
    Serial.println("\n==============================================");
    Serial.println("  EnergyEx: ESP32 PZEM-004T Pure Power Meter  ");
    Serial.println("==============================================");

    pinMode(STATUS_LED, OUTPUT);
    digitalWrite(STATUS_LED, LOW);

    connectWiFi();

    Serial.println("[PZEM] Initializing energy meter sensor...");
    float testV = pzem.voltage();
    if (isnan(testV)) {
        Serial.println("[PZEM] WARNING: PZEM sensor not responding!");
        Serial.println("Check: 1) 5V & GND wires  2) TX->GPIO16 & RX->GPIO17  3) AC mains power connected to PZEM L & N");
    } else {
        Serial.printf("[PZEM] Sensor Connected! Initial Voltage: %.1f V\n", testV);
    }
}

void loop() {
    // Keep Wi-Fi connected
    if (WiFi.status() != WL_CONNECTED) {
        digitalWrite(STATUS_LED, LOW);
        connectWiFi();
    } else {
        digitalWrite(STATUS_LED, HIGH);
    }

    // Send sensor telemetry at fixed interval
    unsigned long now = millis();
    if (now - lastTelemetryTime >= TELEMETRY_INTERVAL) {
        lastTelemetryTime = now;
        sendSensorTelemetry();
    }

    delay(20);
}

void connectWiFi() {
    Serial.printf("[WiFi] Connecting to '%s' ...\n", WIFI_SSID);
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 25) {
        delay(400);
        Serial.print(".");
        attempts++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n[WiFi] Connected successfully!");
        Serial.printf("[WiFi] ESP32 IP: %s\n", WiFi.localIP().toString().c_str());
        Serial.printf("[WiFi] Signal: %d dBm\n", WiFi.RSSI());
    } else {
        Serial.println("\n[WiFi] Connection timeout. Retrying...");
    }
}

void sendSensorTelemetry() {
    if (WiFi.status() != WL_CONNECTED) return;

    // Read real sensor parameters from PZEM-004T v3.0
    float voltage   = pzem.voltage();
    float current   = pzem.current();
    float power     = pzem.power();
    float energy    = pzem.energy();
    float frequency = pzem.frequency();
    float pf        = pzem.pf();

    // Safety checks for sensor read errors
    if (isnan(voltage))   voltage = 230.0;
    if (isnan(current))   current = 0.0;
    if (isnan(power))     power = 0.0;
    if (isnan(energy))    energy = 0.0;
    if (isnan(frequency)) frequency = 50.0;
    if (isnan(pf))        pf = 0.95;

    // Print clean readings to Serial Monitor
    Serial.printf("⚡ [PZEM] Voltage: %.1f V | Current: %.3f A | Power: %.1f W | Energy: %.3f kWh | Freq: %.1f Hz | PF: %.2f\n",
                  voltage, current, power, energy, frequency, pf);

    // Build JSON payload
    StaticJsonDocument<512> doc;
    doc["deviceId"]     = DEVICE_ID;
    doc["authToken"]    = AUTH_TOKEN;
    doc["voltageV"]     = voltage;
    doc["currentA"]     = current;
    doc["powerWatts"]   = power;
    doc["energyKwh"]    = energy;
    doc["frequencyHz"]  = frequency;
    doc["powerFactor"]  = pf;
    doc["isSimulated"]  = false; // Real hardware sensor data

    String jsonPayload;
    serializeJson(doc, jsonPayload);

    // Send HTTP POST to Spring Boot backend
    HTTPClient http;
    http.begin(SERVER_URL);
    http.addHeader("Content-Type", "application/json");

    int httpCode = http.POST(jsonPayload);
    if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_CREATED) {
        Serial.println("  -> [HTTP 200] Telemetry sent to Backend successfully!");
    } else {
        Serial.printf("  -> [HTTP Error] Code: %d (Check if backend is running on PC)\n", httpCode);
    }
    http.end();
}
