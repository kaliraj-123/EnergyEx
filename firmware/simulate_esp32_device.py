# pyrefly: ignore-errors
# pyright: reportMissingImports=false, reportUnusedImport=false, reportAttributeAccessIssue=false
# type: ignore
"""
EnergyEx - Virtual ESP32 Hardware Emulator
This script acts exactly like the physical ESP32 running 'EnergyEx_ESP32.ino'.
Use it to test Live IoT Mode before the actual ESP32 board arrives tomorrow!
"""

import time
import requests
import random

SERVER_HOST = "http://localhost:8080"
DEVICE_ID = "ESP32_ENERGYEX_01"
AUTH_TOKEN = "ENERGYEX_IOT_SECURE_TOKEN_2026"

TELEMETRY_URL = f"{SERVER_HOST}/api/v1/iot/telemetry"
COMMANDS_URL = f"{SERVER_HOST}/api/v1/iot/commands"
ACK_URL = f"{SERVER_HOST}/api/v1/iot/commands"

# Simulated Relay States (Channels 1 to 4)
relay_states = [True, True, False, True]

print("=" * 60)
print("  EnergyEx Virtual ESP32 Hardware Simulator")
print("  Emulating PZEM-004T Sensor & 4-Channel Relays")
print(f"  Target Backend: {SERVER_HOST}")
print("=" * 60)

cumulative_energy = 73.150 # Starting kWh
tick = 0

try:
    while True:
        tick += 1

        # 1. Simulate AC Mains Readings
        voltage = round(230.0 + random.gauss(0, 1.5), 2)
        frequency = round(50.0 + random.gauss(0, 0.04), 2)
        power_factor = 0.94

        # Channel active loads
        ch_powers = [
            28.0 if relay_states[0] else 0.0,  # CH1: Light
            55.0 if relay_states[1] else 0.0,  # CH2: Fan
            85.0 if relay_states[2] else 0.0,  # CH3: TV
            160.0 if relay_states[3] else 0.0  # CH4: Fridge
        ]

        total_power = sum(ch_powers)
        total_current = round(total_power / (voltage * power_factor) if total_power > 0 else 0.0, 3)
        cumulative_energy += (total_power * (3.0 / 3600.0)) / 1000.0

        telemetry_payload = {
            "deviceId": DEVICE_ID,
            "authToken": AUTH_TOKEN,
            "voltageV": voltage,
            "currentA": total_current,
            "powerWatts": round(total_power, 2),
            "energyKwh": round(cumulative_energy, 4),
            "frequencyHz": frequency,
            "powerFactor": power_factor,
            "isSimulated": False, # Transmitted as actual hardware ingest
            "channelReadings": [
                {
                    "channelNumber": i + 1,
                    "isOn": relay_states[i],
                    "powerWatts": ch_powers[i],
                    "currentA": round(ch_powers[i] / (voltage * power_factor), 3) if ch_powers[i] > 0 else 0.0
                }
                for i in range(4)
            ]
        }

        # 2. Push Telemetry
        try:
            resp = requests.post(TELEMETRY_URL, json=telemetry_payload, timeout=4)
            if resp.status_code == 200:
                print(f"[ESP32 -> Backend] Telemetry Sent | V: {voltage}V | P: {total_power:.1f}W | I: {total_current}A | E: {cumulative_energy:.4f}kWh")
            else:
                print(f"[ESP32] Telemetry warning HTTP {resp.status_code}")
        except Exception as e:
            print(f"[ESP32] Connection error: {e}")

        # 3. Poll Pending Relay Commands
        try:
            cmd_resp = requests.get(f"{COMMANDS_URL}?deviceId={DEVICE_ID}", timeout=3)
            if cmd_resp.status_code == 200:
                commands = cmd_resp.json()
                for cmd in commands:
                    cmd_id = cmd["id"]
                    ch = cmd["relayChannel"]
                    state = cmd["commandState"].upper()

                    print(f"[ESP32 Relay Command] Received command #{cmd_id} for Relay CH{ch} -> {state}")
                    if 1 <= ch <= 4:
                        relay_states[ch - 1] = (state == "ON")
                        print(f"[ESP32 Relay CH{ch}] Physically switched to {state}!")

                    # Confirm command execution back to Spring Boot
                    ack_resp = requests.post(f"{ACK_URL}/{cmd_id}/ack", json={"status": "CONFIRMED"}, timeout=3)
                    print(f"[ESP32 -> Backend] Command #{cmd_id} confirmed (HTTP {ack_resp.status_code})")
        except Exception as e:
            pass

        time.sleep(3)

except KeyboardInterrupt:
    print("\nVirtual ESP32 Simulator stopped.")

