# EnergyEx Hardware Wiring & Assembly Guide
**Target Hardware Arrival: Tomorrow**  
**Components:** ESP32 DevKit V1 (30-pin/38-pin), PZEM-004T V3.0 Energy Meter Module, 4-Channel 5V Relay Module with Optocoupler Isolation.

---

## 1. Safety Warnings for AC Mains (230V 50Hz)

> [!CAUTION]
> **MAINS ELECTRICITY (230V AC) CAN BE LETHAL.**
> - Always unplug the main AC power cord before touching any screw terminals or wire connections.
> - Ensure all AC connections are insulated using heat-shrink tubing or an enclosed plastic project box.
> - Current Transformer (CT) rule: Pass **ONLY ONE WIRE (the LIVE / Phase wire)** through the round hole of the CT clamp. If you pass both Live and Neutral through the CT, their magnetic fields will cancel out and current will read **0.00 A**.

---

## 2. Complete Wiring Pinout

### A. ESP32 to PZEM-004T V3.0 (Low-Voltage DC Side)

| PZEM-004T Pin | ESP32 Pin | Wire Color Recommendation | Notes |
| :--- | :--- | :--- | :--- |
| **5V (VCC)** | **VIN / 5V** | Red | PZEM-004T UART logic requires 5V supply |
| **GND** | **GND** | Black | Common Ground |
| **TX** | **GPIO 16 (RX2)** | Yellow / White | Connects PZEM TX to ESP32 RX2 |
| **RX** | **GPIO 17 (TX2)** | Green / Blue | Connects PZEM RX to ESP32 TX2 |

*Note: The PZEM-004T has internal optocouplers between the 230V AC side and the 5V DC microcontroller side, providing galvanic isolation.*

---

### B. ESP32 to 4-Channel Relay Module (Low-Voltage DC Side)

| 4-Channel Relay Pin | ESP32 Pin | Function |
| :--- | :--- | :--- |
| **VCC** | **VIN (5V)** | Power to relay coils and optocouplers |
| **GND** | **GND** | Common Ground |
| **IN1** | **GPIO 18** | Relay Channel 1: Living Room Light |
| **IN2** | **GPIO 19** | Relay Channel 2: Ceiling Fan |
| **IN3** | **GPIO 21** | Relay Channel 3: Smart TV |
| **IN4** | **GPIO 22** | Relay Channel 4: Kitchen Refrigerator |

*Note: Relay modules are **Active-LOW**. Outputting `LOW` (`0V`) turns the relay **ON**; outputting `HIGH` (`3.3V`) turns the relay **OFF**.*

---

### C. PZEM-004T V3.0 High-Voltage (AC Side) & CT Clamp

1. **Screw Terminals `L` and `N` on PZEM:**
   - Connect terminal `L` to **AC Mains 230V Live (Phase)**.
   - Connect terminal `N` to **AC Mains 230V Neutral**.
   - *This provides power to the onboard measurement IC and voltage sensing.*
2. **Current Transformer (CT) 2-pin connector:**
   - Plug the 2-pin CT clamp wire into the CT connector on the PZEM board.
   - Clamp the CT ring around the **Live wire** feeding the appliances.

---

### D. 4-Channel Relay to Home Appliances (AC Switching)

For each appliance channel (CH1 to CH4):
1. Take the AC 230V Live wire from your wall outlet.
2. Connect it to the **Common (`COM`)** terminal of Relay Channel 1.
3. Connect the **Normally Open (`NO`)** terminal of Relay Channel 1 to the appliance Live input.
4. Connect the appliance Neutral wire directly back to AC Mains Neutral.
5. Repeat for Channels 2, 3, and 4.

When Relay 1 is activated (`LOW`), the internal contact closes between `COM` and `NO`, powering the appliance.

---

## 3. Circuit Schematic Diagram

```text
       +-------------------------------------------------------------+
       |                        ESP32 WROOM                          |
       |                                                             |
       |  VIN (5V) ---------+--------------+                         |
       |  GND --------------|---------+    |                         |
       |                    |         |    |                         |
       |  GPIO 16 (RX2) <---+ TX      |    |                         |
       |  GPIO 17 (TX2) --->| RX      |    |                         |
       |                    |         |    |                         |
       |  GPIO 18 --------->| IN1     |    |                         |
       |  GPIO 19 --------->| IN2     |    |                         |
       |  GPIO 21 --------->| IN3     |    |                         |
       |  GPIO 22 --------->| IN4     |    |                         |
       +--------------------|---------|----+-------------------------+
                            |         |    |
                   +--------+         |    |
                   |                  |    |
         +---------v-------+          |    |
         |  PZEM-004T v3.0 |          |    |
         |  5V   GND       |          |    |
         |  [+]   [-]      |          |    |
         +-----------------+          |    |
            |      |                  |    |
       (230V AC)   |                  |    |
         L    N    |                  |    |
         |    |    |                  |    |
         |    |    +------------------+----+
         |    |                       |    |
         |    |              +--------v----v---------+
         |    |              | 4-Channel Relay Board |
         |    |              | VCC  GND  IN1..IN4    |
         |    |              +-----------------------+
         |    |                  |    |    |    |
         |    |                 CH1  CH2  CH3  CH4
         |    |                  |    |    |    |
       [CT CLAMP]              Light Fan   TV  Fridge
       around Live wire
```

---

## 4. Flashing Instructions (Arduino IDE)

1. Open **Arduino IDE**.
2. Go to **File -> Preferences**, and add the ESP32 board URL:  
   `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
3. Go to **Tools -> Board -> Boards Manager**, search for `esp32` by Espressif, and install.
4. Go to **Sketch -> Include Library -> Manage Libraries**, and install:
   - `PZEM004Tv30` by Mandar
   - `ArduinoJson` by Benoit Blanchon (version 6.x or 7.x)
5. Open `EnergyEx/firmware/EnergyEx_ESP32/EnergyEx_ESP32.ino`.
6. Open `config.h` and update:
   - `WIFI_SSID` & `WIFI_PASSWORD`
   - `SERVER_HOST` (e.g. `http://192.168.1.100:8080`)
7. Select **Board: ESP32 Dev Module**, select your COM port, and click **Upload**.
8. Open the Serial Monitor at **115200 baud** to verify connection.
9. Open the Android App or Web Dashboard -> the device will show **LIVE IoT MODE** automatically!

