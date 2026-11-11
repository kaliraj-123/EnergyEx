-- EnergyEx Relational Database Schema (MySQL 8 / Compatible)
-- Supports users, appliances, devices, energy readings, tariffs, predictions, alerts, schedules, device commands

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(30) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tariffs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rate_per_kwh DECIMAL(10, 2) NOT NULL DEFAULT 7.50,
    currency VARCHAR(10) DEFAULT '₹',
    slab_tier1_limit DECIMAL(10, 2) DEFAULT 100.0,
    slab_tier1_rate DECIMAL(10, 2) DEFAULT 5.00,
    slab_tier2_limit DECIMAL(10, 2) DEFAULT 300.0,
    slab_tier2_rate DECIMAL(10, 2) DEFAULT 7.50,
    slab_tier3_rate DECIMAL(10, 2) DEFAULT 10.00,
    fixed_charge DECIMAL(10, 2) DEFAULT 50.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) UNIQUE NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50) DEFAULT 'ESP32_PZEM004T',
    ip_address VARCHAR(45),
    mac_address VARCHAR(20),
    firmware_version VARCHAR(20) DEFAULT '1.0.0',
    auth_token VARCHAR(128) NOT NULL,
    status VARCHAR(20) DEFAULT 'DISCONNECTED', -- CONNECTED, DISCONNECTED, CONNECTING
    is_live_mode BOOLEAN DEFAULT FALSE,
    last_seen_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS appliances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    appliance_type VARCHAR(50) NOT NULL, -- LIGHT, FAN, TV, REFRIGERATOR, AC, OTHER
    relay_channel INT NOT NULL DEFAULT 1, -- Relay channel 1 to 4 on 4-channel module
    power_rating_watts DECIMAL(10, 2) NOT NULL DEFAULT 100.0,
    is_on BOOLEAN DEFAULT FALSE,
    current_power_w DECIMAL(10, 2) DEFAULT 0.0,
    voltage_v DECIMAL(10, 2) DEFAULT 0.0,
    current_a DECIMAL(10, 3) DEFAULT 0.0,
    cumulative_energy_kwh DECIMAL(12, 4) DEFAULT 0.0,
    estimated_cost_inr DECIMAL(12, 2) DEFAULT 0.0,
    connection_status VARCHAR(20) DEFAULT 'ONLINE',
    is_simulated BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS energy_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appliance_id BIGINT NULL,
    device_id VARCHAR(64) NULL,
    voltage_v DECIMAL(8, 2) NOT NULL,
    current_a DECIMAL(8, 3) NOT NULL,
    active_power_w DECIMAL(10, 2) NOT NULL,
    energy_kwh DECIMAL(12, 4) NOT NULL,
    frequency_hz DECIMAL(6, 2) DEFAULT 50.00,
    power_factor DECIMAL(4, 2) DEFAULT 0.95,
    cost_inr DECIMAL(10, 2) DEFAULT 0.0,
    is_demo_data BOOLEAN DEFAULT TRUE,
    reading_source VARCHAR(30) DEFAULT 'SIMULATION', -- SIMULATION or HARDWARE_PZEM
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reading_appliance (appliance_id),
    INDEX idx_reading_timestamp (timestamp),
    CONSTRAINT fk_readings_appliance FOREIGN KEY (appliance_id) REFERENCES appliances(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prediction_type VARCHAR(50) DEFAULT 'DAILY_7_DAYS', -- DAILY_7_DAYS, MONTHLY_BILL
    forecast_date DATE NOT NULL,
    predicted_consumption_kwh DECIMAL(10, 2) NOT NULL,
    predicted_cost_inr DECIMAL(10, 2) NOT NULL,
    confidence_score DECIMAL(5, 2) DEFAULT 85.00,
    is_demo_prediction BOOLEAN DEFAULT TRUE,
    model_version VARCHAR(50) DEFAULT 'RandomForestRegressor-v1.0',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    alert_type VARCHAR(50) NOT NULL, -- HIGH_CONSUMPTION, ANOMALY, VOLTAGE_FLUCTUATION, HARDWARE_STATUS, SCHEDULE_TRIGGER
    severity VARCHAR(20) DEFAULT 'WARNING', -- INFO, WARNING, CRITICAL
    appliance_id BIGINT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_demo_alert BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerts_appliance FOREIGN KEY (appliance_id) REFERENCES appliances(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appliance_id BIGINT NOT NULL,
    schedule_name VARCHAR(100) NOT NULL,
    action_type VARCHAR(20) NOT NULL, -- TURN_ON, TURN_OFF
    scheduled_time TIME NOT NULL,
    repeat_days VARCHAR(50) DEFAULT 'DAILY', -- DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    is_active BOOLEAN DEFAULT TRUE,
    last_triggered_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedules_appliance FOREIGN KEY (appliance_id) REFERENCES appliances(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS device_commands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL,
    appliance_id BIGINT NOT NULL,
    relay_channel INT NOT NULL,
    command_state VARCHAR(20) NOT NULL, -- ON, OFF
    execution_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, EXECUTED, CONFIRMED, FAILED
    is_simulated BOOLEAN DEFAULT TRUE,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP NULL,
    CONSTRAINT fk_commands_appliance FOREIGN KEY (appliance_id) REFERENCES appliances(id) ON DELETE CASCADE
);

