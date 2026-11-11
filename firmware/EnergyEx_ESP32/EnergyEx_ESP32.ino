/**
 * EnergyEx - Smart Electricity Monitoring & Prediction System
 * Microcontroller Firmware: ESP32 + PZEM-004T v3.0 + 4-Channel Relay
 * 
 * Target Board: ESP32 Dev Module (WROOM-32)
 * Required Libraries:
 *  - PZEM004Tv30 by Mandar (install via Arduino Library Manager)
 *  - ArduinoJson by Benoit Blanchon (v6.x or v7.x)
 *  - WiFi & HTTPClient (built into ESP32 Arduino Core)
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <PZEM004Tv30.h>
#include "config.h"

// Initialize PZEM-004T v3.0 on HardwareSerial 2 (GPIO 16 RX, GPIO 17 TX)
PZEM004Tv30 pzem(Serial2, PZEM_RX_PIN, PZEM_TX_PIN);

// Relay Pin mapping array
const int relayPins[4] = { RELAY_CH1_PIN, RELAY_CH2_PIN, RELAY_CH3_PIN, RELAY_CH4_PIN };
bool relayStates[4] = { false, false, false, false };

unsigned long lastTelemetryTime = 0;
unsigned long lastCommandTime = 0;

void setup() {
    Serial.begin(115200);
    delay(1000);
    Serial.println("\n==============================================");
    Serial.println("  EnergyEx ESP32 IoT Power Node Initializing  ");
    Serial.println("==============================================");

    // 1. Initialize Relay Pins (Default OFF - Active LOW: HIGH = Relay Open/OFF)
    for (int i = 0; i < 4; i++) {
        pinMode(relayPins[i], OUTPUT);
        digitalWrite(relayPins[i], HIGH); // Relays are active LOW
        relayStates[i] = false;
        Serial.printf("[Relay] Channel %d initialized on GPIO %d (OFF)\n", i + 1, relayPins[i]);
    }

    pinMode(STATUS_LED_PIN, OUTPUT);
    digitalWrite(STATUS_LED_PIN, LOW);

    // 2. Connect to Wi-Fi
    connectWiFi();

    // 3. Verify PZEM-004T UART connection
    Serial.println("[PZEM] Checking PZEM-004T energy meter connection...");
    float testV = pzem.voltage();
    if (isnan(testV)) {
        Serial.println("[PZEM] WARNING: PZEM-004T not responding. Check 5V power and RX/TX cross-connection.");
    } else {
        Serial.printf("[PZEM] Communication OK! Initial Voltage: %.1f V\n", testV);
    }
}

void loop() {
    // Maintain Wi-Fi connectivity
    if (WiFi.status() != WL_CONNECTED) {
        digitalWrite(STATUS_LED_PIN, LOW);
        connectWiFi();
    } else {
        digitalWrite(STATUS_LED_PIN, HIGH);
    }

    unsigned long now = millis();

    // 1. Send Telemetry to Spring Boot at defined intervals
    if (now - lastTelemetryTime >= TELEMETRY_INTERVAL_MS) {
        lastTelemetryTime = now;
        sendSensorTelemetry();
    }

    // 2. Poll Backend for Pending Remote Relay Commands
    if (now - lastCommandTime >= COMMAND_POLL_INTERVAL_MS) {
        lastCommandTime = now;
        pollRemoteCommands();
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
        Serial.printf("[WiFi] IP Address: %s\n", WiFi.localIP().toString().c_str());
        Serial.printf("[WiFi] RSSI: %d dBm\n", WiFi.RSSI());
    } else {
        Serial.println("\n[WiFi] Connection timeout. Retrying in loop...");
    }
}

void sendSensorTelemetry() {
    if (WiFi.status() != WL_CONNECTED) return;

    // Read electrical parameters from PZEM-004T v3.0
    float voltage   = pzem.voltage();
    float current   = pzem.current();
    float power     = pzem.power();
    float energy    = pzem.energy();
    float frequency = pzem.frequency();
    float pf        = pzem.pf();

    // Handle initial warmup / sensor read retry
    if (isnan(voltage))   voltage = 230.0; // Fallback safe grid value
    if (isnan(current))   current = 0.0;
    if (isnan(power))     power = 0.0;
    if (isnan(energy))    energy = 0.0;
    if (isnan(frequency)) frequency = 50.0;
    if (isnan(pf))        pf = 0.95;

    Serial.printf("[PZEM Telemetry] V: %.1fV | I: %.3fA | P: %.1fW | E: %.3fkWh | F: %.1fHz | PF: %.2f\n",
                  voltage, current, power, energy, frequency, pf);

    // Build JSON Payload
    StaticJsonDocument<1024> doc;
    doc["deviceId"]     = DEVICE_ID;
    doc["authToken"]    = DEVICE_AUTH_TOKEN;
    doc["voltageV"]     = voltage;
    doc["currentA"]     = current;
    doc["powerWatts"]   = power;
    doc["energyKwh"]    = energy;
    doc["frequencyHz"]  = frequency;
    doc["powerFactor"]  = pf;
    doc["isSimulated"]  = false;

    // Add channel breakdown for active relays
    JsonArray channels = doc.createNestedArray("channelReadings");
    for (int i = 0; i < 4; i++) {
        JsonObject ch = channels.createNestedObject();
        ch["channelNumber"] = i + 1;
        ch["isOn"] = relayStates[i];
        ch["currentA"] = relayStates[i] ? (current / max(1, countActiveRelays())) : 0.0;
        ch["powerWatts"] = relayStates[i] ? (power / max(1, countActiveRelays())) : 0.0;
    }

    String jsonPayload;
    serializeJson(doc, jsonPayload);

    // Send HTTP POST to Spring Boot API
    HTTPClient http;
    String url = String(SERVER_HOST) + String(TELEMETRY_ENDPOINT);
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    int httpCode = http.POST(jsonPayload);
    if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_CREATED) {
        Serial.println("[HTTP] Telemetry successfully ingested by backend.");
    } else {
        Serial.printf("[HTTP] Telemetry POST failed. Code: %d\n", httpCode);
    }
    http.end();
}

void pollRemoteCommands() {
    if (WiFi.status() != WL_CONNECTED) return;

    HTTPClient http;
    String url = String(SERVER_HOST) + String(COMMANDS_ENDPOINT) + "?deviceId=" + String(DEVICE_ID);
    http.begin(url);

    int httpCode = http.GET();
    if (httpCode == HTTP_CODE_OK) {
        String payload = http.getString();
        StaticJsonDocument<2048> doc;
        DeserializationError err = deserializeJson(doc, payload);

        if (!err && doc.is<JsonArray>()) {
            JsonArray commands = doc.as<JsonArray>();
            for (JsonObject cmd : commands) {
                long commandId   = cmd["id"];
                int channel      = cmd["relayChannel"];
                const char* state = cmd["commandState"];

                Serial.printf("[Command] Received Command #%ld -> Relay CH%d: %s\n", commandId, channel, state);
                executeRelaySwitch(channel, state);
                acknowledgeCommand(commandId, "CONFIRMED");
            }
        }
    }
    http.end();
}

void executeRelaySwitch(int channel, const char* state) {
    if (channel < 1 || channel > 4) return;
    int pinIndex = channel - 1;
    bool turnOn = (strcasecmp(state, "ON") == 0);

    // Active-LOW Relay Module: LOW = Relay Closed (ON), HIGH = Relay Open (OFF)
    digitalWrite(relayPins[pinIndex], turnOn ? LOW : HIGH);
    relayStates[pinIndex] = turnOn;

    Serial.printf("[Relay CH%d] Switched to %s (GPIO %d set to %s)\n",
                  channel, turnOn ? "ON" : "OFF", relayPins[pinIndex], turnOn ? "LOW" : "HIGH");
}

void acknowledgeCommand(long commandId, const char* status) {
    HTTPClient http;
    String url = String(SERVER_HOST) + String(ACK_ENDPOINT) + String(commandId) + "/ack";
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    String ackPayload = "{\"status\":\"" + String(status) + "\"}";
    int code = http.POST(ackPayload);
    Serial.printf("[Ack] Command #%ld acknowledgment sent (Code: %d)\n", commandId, code);
    http.end();
}

int countActiveRelays() {
    int count = 0;
    for (int i = 0; i < 4; i++) {
        if (relayStates[i]) count++;
    }
    return count;
}

