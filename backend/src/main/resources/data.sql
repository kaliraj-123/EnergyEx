-- Initial Default Data for EnergyEx

INSERT INTO users (id, name, email, role)
SELECT 1, 'Student Engineer', 'user@energyex.local', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

INSERT INTO tariffs (id, name, rate_per_kwh, currency, slab_tier1_limit, slab_tier1_rate, slab_tier2_limit, slab_tier2_rate, slab_tier3_rate, fixed_charge, is_active)
SELECT 1, 'Domestic LT-1 Tariff', 7.50, '₹', 100.0, 4.50, 300.0, 7.50, 10.00, 50.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM tariffs WHERE id = 1);

INSERT INTO devices (id, device_id, device_name, device_type, ip_address, firmware_version, auth_token, status, is_live_mode)
SELECT 1, 'ESP32_ENERGYEX_01', 'Home Energy Monitor (ESP32 + PZEM)', 'ESP32_PZEM004T', '192.168.1.150', 'v1.0.0-PROD', 'ENERGYEX_IOT_SECURE_TOKEN_2026', 'SIMULATED_ACTIVE', FALSE
WHERE NOT EXISTS (SELECT 1 FROM devices WHERE id = 1);

INSERT INTO appliances (id, name, appliance_type, relay_channel, power_rating_watts, is_on, current_power_w, voltage_v, current_a, cumulative_energy_kwh, estimated_cost_inr, connection_status, is_simulated)
SELECT 1, 'Living Room Light', 'LIGHT', 1, 28.00, TRUE, 27.50, 230.20, 0.125, 3.4500, 25.88, 'ONLINE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM appliances WHERE id = 1);

INSERT INTO appliances (id, name, appliance_type, relay_channel, power_rating_watts, is_on, current_power_w, voltage_v, current_a, cumulative_energy_kwh, estimated_cost_inr, connection_status, is_simulated)
SELECT 2, 'Ceiling Fan', 'FAN', 2, 55.00, TRUE, 53.80, 230.20, 0.245, 8.1200, 60.90, 'ONLINE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM appliances WHERE id = 2);

INSERT INTO appliances (id, name, appliance_type, relay_channel, power_rating_watts, is_on, current_power_w, voltage_v, current_a, cumulative_energy_kwh, estimated_cost_inr, connection_status, is_simulated)
SELECT 3, 'Smart LED TV', 'TV', 3, 85.00, FALSE, 0.00, 0.00, 0.000, 12.6000, 94.50, 'ONLINE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM appliances WHERE id = 3);

INSERT INTO appliances (id, name, appliance_type, relay_channel, power_rating_watts, is_on, current_power_w, voltage_v, current_a, cumulative_energy_kwh, estimated_cost_inr, connection_status, is_simulated)
SELECT 4, 'Kitchen Refrigerator', 'REFRIGERATOR', 4, 180.00, TRUE, 145.20, 230.20, 0.680, 48.7500, 365.63, 'ONLINE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM appliances WHERE id = 4);

INSERT INTO alerts (id, title, message, alert_type, severity, appliance_id, is_read, is_demo_alert)
SELECT 1, 'System Initialized in Demo Mode', 'EnergyEx is running with realistic IoT simulation. Actual hardware integration is ready for ESP32 + PZEM-004T.', 'HARDWARE_STATUS', 'INFO', NULL, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM alerts WHERE id = 1);

