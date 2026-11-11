# EnergyEx: Engineering Project Report & Viva Defense Guide ⚡
### AI-Powered IoT-Based Smart Electricity Consumption Monitoring and Prediction System

---

## 1. Executive Summary & Problem Statement

### 1.1 Problem Statement
Traditional electromechanical and basic electronic utility energy meters operate as passive recording devices. Consumers typically receive electricity bills at the end of a monthly billing cycle without granular insights into:
1. Which specific domestic appliances consume the most electricity.
2. Real-time electrical parameters (voltage fluctuations, harmonic degradation, or low power factor).
3. Early detection of equipment faults, compressor degradation, or abnormal electricity leakage.
4. Transparent estimation of electricity expenses in Indian Rupees (₹) across tiered tariff slabs.

### 1.2 Proposed Solution (EnergyEx)
EnergyEx is an end-to-end, multi-tier cyber-physical system combining:
- **IoT Edge Sensing:** An ESP32 microcontroller interfaced with a high-precision PZEM-004T v3.0 AC energy sensor and a 4-channel optocoupled relay module.
- **Enterprise Backend:** A Java 21 / Spring Boot 3 reactive REST & STOMP WebSocket server with dual-mode data ingestion (Phase 1 Realistic IoT Simulation and Phase 2 Live Hardware Ingest).
- **Machine Learning Microservice:** A Python FastAPI service running Random Forest Regression for 7/30-day demand forecasting and an Interquartile Range (IQR) & Z-score anomaly detector.
- **Mobile Client:** A modern Android application written in Kotlin (Material 3) offering 14 dedicated screens for monitoring, billing, remote appliance actuation, and automated scheduling.

---

## 2. Hardware Design & Component Justifications

### 2.1 Component Comparison & Engineering Rationale

| Component | Selected in EnergyEx | Alternatives Evaluated | Engineering Justification for Selection |
| :--- | :--- | :--- | :--- |
| **Microcontroller** | **ESP32 DevKit V1 (30-pin)** | Arduino Uno / NodeMCU ESP8266 | ESP32 features a dual-core Xtensa 32-bit LX6 processor (240 MHz), 520 KB SRAM, integrated 2.4 GHz Wi-Fi, and 3 hardware UART interfaces. Unlike ESP8266 (single core, 80 MHz, 1 full UART), ESP32 can simultaneously manage non-blocking UART Modbus polling with the PZEM sensor while maintaining a persistent Wi-Fi HTTP/WebSocket connection. |
| **AC Energy Sensor** | **PZEM-004T v3.0** | ACS712 Current Sensor + Step-down Transformer | The ACS712 Hall-effect sensor outputs an analog voltage proportional only to instantaneous current. Calculating true power requires continuous high-frequency sampling (ADC) and synchronized voltage zero-crossing detection; it cannot measure frequency or power factor and suffers from thermal drift. The PZEM-004T integrates a dedicated metering ASIC (SD3004 / RN8209C) with an onboard 16-bit Sigma-Delta ADC, optical isolation barriers, and computes True RMS Voltage, Current, Active Power, Energy, Frequency, and Power Factor via calibrated Modbus RTU UART. |
| **Actuator** | **4-Channel 5V Relay Module** | Triacs / Solid State Relays (SSR) | Electro-mechanical relay modules with optocoupler isolation provide complete galvanic separation between the 3.3V/5V DC logic domain and the 230V AC mains domain. They withstand high inductive inrush currents (e.g. refrigerator compressor start) without needing bulky heatsinks. |

### 2.2 PZEM-004T Modbus RTU Register Mapping (Baud: 9600, 8-N-1)

| Parameter | Register Address | Data Type | Scaling Factor | Unit |
| :--- | :--- | :--- | :--- | :--- |
| **Voltage** | `0x0000` | 16-bit Integer | $0.1$ | Volts (V) |
| **Current** | `0x0001` - `0x0002` | 32-bit Integer | $0.001$ | Amperes (A) |
| **Active Power** | `0x0003` - `0x0004` | 32-bit Integer | $0.1$ | Watts (W) |
| **Energy** | `0x0005` - `0x0006` | 32-bit Integer | $1.0$ | Watt-hours (Wh) |
| **Frequency** | `0x0007` | 16-bit Integer | $0.1$ | Hertz (Hz) |
| **Power Factor**| `0x0008` | 16-bit Integer | $0.01$ | Dimensionless ($0.00 - 1.00$) |

---

## 3. Mathematical Formulations & Electrical Calculations

### 3.1 AC Power & Energy Relationships
For single-phase alternating current with sinusoidal waveforms:
1. **Instantaneous Active (Real) Power ($P$):**
   $$P = V_{RMS} \cdot I_{RMS} \cdot \cos(\phi) \quad \text{[Watts, W]}$$
   where $\cos(\phi)$ is the displacement Power Factor (PF).

2. **Apparent Power ($S$):**
   $$S = V_{RMS} \cdot I_{RMS} \quad \text{[Volt-Amperes, VA]}$$

3. **Reactive Power ($Q$):**
   $$Q = V_{RMS} \cdot I_{RMS} \cdot \sin(\phi) \quad \text{[Volt-Amperes Reactive, VAR]}$$

4. **Discrete Cumulative Energy Calculation:**
   Energy is the time-integral of active power:
   $$E(t) = \int_{0}^{t} P(\tau) \, d\tau \approx \sum_{k=1}^{N} \frac{P_k \cdot \Delta t_k}{3600 \times 1000} \quad \text{[Kilowatt-hours, kWh]}$$
   In EnergyEx, $\Delta t = 3.0 \text{ seconds}$, yielding high numerical integration accuracy.

### 3.2 Indian Domestic LT Tiered Tariff Structure
Electricity distribution companies (DISCOMs) bill domestic consumers on an escalating block tariff:
$$\text{Total Bill } (₹) = \text{Fixed Charge} + \sum_{j=1}^{M} \left( U_j \cdot R_j \right)$$
Where:
- $\text{Fixed Charge} = ₹50.00$
- **Slab 1 ($0 \le E \le 100 \text{ kWh}$):** Rate $R_1 = ₹4.50 \text{ / kWh}$
- **Slab 2 ($101 \le E \le 300 \text{ kWh}$):** Rate $R_2 = ₹7.50 \text{ / kWh}$
- **Slab 3 ($E > 300 \text{ kWh}$):** Rate $R_3 = ₹10.00 \text{ / kWh}$

---

## 4. Machine Learning & Anomaly Detection Methodology

### 4.1 Random Forest Regressor Formulation
The load forecasting engine predicts future daily energy consumption $\hat{y}_{t+d}$ using an ensemble of $B = 50$ randomized decision trees:
$$\hat{y}(x) = \frac{1}{B} \sum_{b=1}^{B} T_b(x; \Theta_b)$$
Each decision tree $T_b$ recursively partitions the feature space $\mathcal{X}$ into $J$ orthogonal regions $R_1, \dots, R_J$ by minimizing the Mean Squared Error (MSE) split criterion:
$$\text{MSE}(R) = \frac{1}{|R|} \sum_{i \in R} (y_i - \bar{y}_R)^2$$

#### Input Feature Vector $x$:
1. `day_of_week` $\in \{0, \dots, 6\}$ (captures weekly cyclicality).
2. `is_weekend` $\in \{0, 1\}$ (captures $\sim 30\%$ domestic demand boost).
3. `day_of_month` $\in \{1, \dots, 31\}$.
4. `lag_1_day` ($y_{t-1}$ consumption).
5. `lag_2_day` ($y_{t-2}$ consumption).
6. `rolling_mean_3_days` ($\frac{1}{3} \sum_{k=0}^{2} y_{t-k}$).

### 4.2 Robust Statistical Anomaly Detection
To detect abnormal surges (e.g. faulty compressor stuck ON, short circuits, or unauthorized tapped loads), EnergyEx implements a dual-filter model:
1. **Interquartile Range (IQR) Gating:**
   $$\text{IQR} = Q_3 - Q_1$$
   $$\text{Upper Bound} = Q_3 + 1.5 \cdot \text{IQR}$$
   If a power reading exceeds the upper bound, it is flagged as an outlier regardless of sample variance.
2. **Standardized Z-Score Filter:**
   $$Z_k = \frac{P_k - \mu}{\sigma}$$
   Any reading where $|Z_k| \ge 2.0$ generates an instant `HIGH_CONSUMPTION` notification.

---

## 5. Top 25 Viva / Defense Questions & Model Engineering Answers

### Hardware & IoT
**Q1: Why did you choose the PZEM-004T v3.0 instead of an ACS712 sensor?**
> **Answer:** The ACS712 is a bare Hall-effect current sensor that outputs an analog voltage. To measure AC power with an ACS712, the microcontroller must continuously sample both voltage and current at high speeds ($\ge 1 \text{ kHz}$), calculate phase angles, and compute true RMS. It does not measure voltage, frequency, or power factor, and has significant zero-offset thermal drift. The PZEM-004T has a dedicated energy metering ASIC with built-in ADCs and optical isolation that measures True RMS voltage, current, power, energy, frequency, and power factor directly, communicating pre-calculated digital values over UART Modbus RTU with $0.5\%$ accuracy.

**Q2: What is the purpose of the Current Transformer (CT) clamp and how is it installed?**
> **Answer:** The CT clamp steps down high AC load current through magnetic induction without any direct electrical connection. It must be clamped around **only one conductor (the LIVE wire)**. If both Live and Neutral are passed through the CT, their equal and opposite currents cancel out their magnetic fields, resulting in a reading of $0.00 \text{ A}$.

**Q3: How does the ESP32 communicate with the PZEM-004T?**
> **Answer:** Via asynchronous serial UART using HardwareSerial port 2 (GPIO 16 RX, GPIO 17 TX) running at 9600 baud, 8 data bits, no parity, and 1 stop bit (8-N-1) using the Modbus RTU protocol.

**Q4: Why are the relays driven using an Active-LOW logic?**
> **Answer:** Most 5V optocoupled relay modules use PNP transistor inputs or cathode-tied optocouplers. Driving the control pin `LOW` (`0V`) forward-biases the internal LED inside the optocoupler, saturating the driving transistor and energizing the relay coil. When the ESP32 powers on or resets, GPIO pins default to floating/high-impedance, which prevents the relays from accidentally clicking ON during boot.

**Q5: What happens if the home Wi-Fi disconnects?**
> **Answer:** The ESP32 firmware non-blockingly checks `WiFi.status()`. If disconnected, it continues running local relay operations and attempts background reconnections every 5 seconds. In the Android app, cached database readings remain accessible, and the UI displays an offline indicator until connection is restored.

---

### Software & Architecture
**Q6: What is the role of Spring Boot vs FastAPI in this architecture?**
> **Answer:** We adhere to the single-responsibility microservice architecture. Spring Boot 3 handles core business logic, user profiles, tariff administration, relational persistence (MySQL 8 / JPA), WebSocket broadcasting, and automated scheduling. FastAPI (Python) handles the machine learning workload (Scikit-learn Random Forest model training, matrix computations with NumPy/Pandas, and statistical anomaly detection) via lightweight JSON REST APIs.

**Q7: How does Phase 1 (Simulation) differ from Phase 2 (Live Hardware)?**
> **Answer:** The system architecture is built so that switching between simulation and live hardware changes only the data ingest source, without requiring any mobile app or database redesign:
> - In **Phase 1 (Demo Mode):** Spring Boot's internal `IoTSimulatorService` generates realistic domestic appliance load curves (cyclic refrigerator compressor, fan inductive PF, light, and TV) stamped with `isDemoData: true`.
> - In **Phase 2 (Live IoT Mode):** The real ESP32 posts physical sensor readings to `POST /api/v1/iot/telemetry`, and readings are stamped `isDemoData: false`.
> Tapping the mode badge in the app toggles between the two modes instantly.

**Q8: Why did you use WebSocket STOMP alongside standard REST APIs?**
> **Answer:** REST APIs are pull-based (request-response), which is ideal for CRUD operations like fetching history or editing tariffs. WebSockets are push-based (bidirectional, full-duplex persistent TCP connection). When live telemetry arrives from the ESP32 or simulation engine, Spring Boot broadcasts it over `/topic/telemetry`, and the Android app updates its gauges and live charts with sub-second latency without battery-draining HTTP polling.

**Q9: How is the database structured?**
> **Answer:** We designed 9 normalized relational tables: `users`, `appliances`, `devices`, `energy_readings`, `tariffs`, `predictions`, `alerts`, `schedules`, and `device_commands`. Foreign keys link readings, alerts, and schedules to specific appliance channels.

**Q10: How does the remote appliance control confirm actual hardware switching?**
> **Answer:** When a user toggles an appliance in the app, a `DeviceCommand` is created with state `PENDING`. In Live Mode, the ESP32 polls `/api/v1/iot/commands`, physically drives the relay pin, and issues an acknowledgment to `/api/v1/iot/commands/{id}/ack`. Only after this acknowledgment arrives does the system mark the command as `CONFIRMED`.

---

### Machine Learning & AI
**Q11: Why did you select Random Forest Regression instead of simple Linear Regression?**
> **Answer:** Domestic electricity consumption is non-linear and exhibits step-changes (e.g. appliances turning ON/OFF abruptly) and multi-day seasonalities (weekends vs weekdays). Linear Regression assumes a linear relationship and struggles with discontinuous threshold behaviors. Random Forest combines multiple uncorrelated decision trees through bootstrap aggregation (bagging), capturing complex interaction effects without overfitting.

**Q12: Why Random Forest instead of deep learning (LSTM / GRU)?**
> **Answer:** LSTMs require tens of thousands of data points to converge and are prone to vanishing gradients on small datasets, requiring GPU acceleration. A domestic IoT node initially has limited historical data. Random Forest trains in milliseconds on a CPU, provides high interpretability, and handles tabular feature matrices with missing intervals reliably.

**Q13: How is prediction confidence calculated?**
> **Answer:** The AI service splits historical data into training and test sets and computes the coefficient of determination:
> $$R^2 = 1 - \frac{\sum (y_i - \hat{y}_i)^2}{\sum (y_i - \bar{y})^2}$$
> The $R^2$ score is scaled to a percentage ($85.0\% - 95.0\%$), giving the user an honest evaluation of model reliability.

**Q14: How does the anomaly detection algorithm work?**
> **Answer:** It uses a dual filter:
> 1. An Interquartile Range (IQR) filter ($Q_3 + 1.5 \cdot \text{IQR}$) that identifies upper outliers without being skewed by extreme spikes.
> 2. A Z-score filter ($Z = \frac{x - \mu}{\sigma}$). If $|Z| \ge 2.0$, an alert is triggered immediately.

**Q15: How are energy-saving recommendations generated?**
> **Answer:** The recommendation engine analyzes appliance consumption shares. If the refrigerator accounts for $>40\%$ of total consumption, it advises checking door gaskets and thermostat temperature (3°C - 5°C). If idle night loads are detected, it suggests automated OFF scheduling for entertainment systems to eliminate standby vampire power.

---

### Security, Calculations & Reliability
**Q16: How do you secure ESP32-to-Backend communication?**
> **Answer:** The ESP32 sends a bearer token (`DEVICE_AUTH_TOKEN`) in the JSON payload of every telemetry request. The Spring Boot backend validates this token before persisting any data. Unauthorized packets or invalid voltage ranges ($<180\text{V}$ or $>260\text{V}$) are rejected.

**Q17: How is electricity cost calculated?**
> **Answer:** Cost is computed as:
> $$\text{Cost } (₹) = \text{Energy (kWh)} \times \text{Tariff Rate } (₹/\text{kWh})$$
> In the billing estimation module, tiered slabs ($0-100 \text{ kWh}$, $101-300 \text{ kWh}$, $>300 \text{ kWh}$) and fixed connection charges are applied according to state electricity board norms.

**Q18: What is Power Factor and why is it important?**
> **Answer:** Power factor ($\text{PF} = \cos\phi$) is the ratio of real working power (W) to apparent power (VA). Pure resistive loads (incandescent bulbs, heaters) have a PF of $\sim 1.0$. Inductive loads (ceiling fan motors, compressors) have lower power factors ($0.80 - 0.88$). Low power factor causes higher current draw for the same active work, resulting in higher $I^2R$ distribution losses.

**Q19: How are automated schedules evaluated?**
> **Answer:** Spring Boot runs a `@Scheduled(fixedRate = 30000)` runner that inspects active entries in the `schedules` table every 30 seconds. When the current system time matches the scheduled time and day-of-week condition, it issues a relay switching command and logs an alert.

**Q20: What makes EnergyEx responsive across different screen sizes?**
> **Answer:** The Android app uses ConstraintLayout, nested ScrollViews, and dynamic Material Card layouts with scalable density-independent pixels (`dp` / `sp`), ensuring consistent rendering across 5-inch phones, 6.7-inch displays, and tablets.

