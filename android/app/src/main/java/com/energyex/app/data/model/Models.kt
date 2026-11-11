
package com.energyex.app.data.model

import com.google.gson.annotations.SerializedName

data class DashboardSummary(
    val systemMode: String? = "DEMO",
    val dataLabel: String? = "DEMO DATA (SIMULATION)",
    val totalEnergyKwh: Double = 0.0,
    val todayEnergyKwh: Double = 0.0,
    val currentPowerWatts: Double = 0.0,
    val currentVoltageV: Double = 230.0,
    val currentCurrentA: Double = 0.0,
    val frequencyHz: Double = 50.0,
    val powerFactor: Double = 0.94,
    val estimatedBillInr: Double = 0.0,
    val totalAppliances: Int = 0,
    val activeAppliances: Int = 0,
    val deviceStatus: String? = "SIMULATED",





    val lastDataUpdate: String? = null,
    val appliances: List<Appliance>? = emptyList(),
    val recentAlerts: List<AlertItem>? = emptyList(),
    val upcomingPredictions: List<PredictionDay>? = emptyList()
)

data class Appliance(
    val id: Long = 0,
    val name: String = "",
    val applianceType: String = "LIGHT",
    val relayChannel: Int = 1,
    val sensorChannel: Int = 1,
    val powerRatingWatts: Double = 100.0,
    @SerializedName("on")
    val isOn: Boolean = false,
    val currentPowerW: Double = 0.0,
    val voltageV: Double = 0.0,
    val currentA: Double = 0.0,
    val cumulativeEnergyKwh: Double = 0.0,
    val estimatedCostInr: Double = 0.0,
    val connectionStatus: String = "ONLINE",
    val isSimulated: Boolean = true
)

data class EnergyReading(
    val id: Long = 0,
    val voltageV: Double = 0.0,
    val currentA: Double = 0.0,
    val activePowerW: Double = 0.0,
    val energyKwh: Double = 0.0,
    val frequencyHz: Double = 50.0,
    val powerFactor: Double = 0.95,
    val costInr: Double = 0.0,
    val isDemoData: Boolean = true,
    val readingSource: String = "SIMULATION",
    val timestamp: String = ""
)

data class BillEstimation(
    val currentEnergyKwh: Double = 0.0,
    val defaultTariffRate: Double = 7.50,
    val fixedCharge: Double = 50.00,
    val calculatedBaseCost: Double = 0.0,
    val calculatedSlabCost: Double = 0.0,
    val projectedMonthlyKwh: Double = 0.0,
    val projectedMonthlyBillInr: Double = 0.0,
    val disclaimer: String = "Estimated bill. Actual utility bills may include municipal taxes and surcharges.",
    val slabBreakdown: List<SlabDetail> = emptyList()
)

data class SlabDetail(
    val slabName: String = "",
    val unitsInSlab: Double = 0.0,
    val ratePerKwh: Double = 0.0,
    val amountInr: Double = 0.0
)

data class PredictionResponse(
    val predictionType: String = "DAILY_7_DAYS",
    val forecastDays: List<PredictionDay> = emptyList(),
    val totalForecastedKwh: Double = 0.0,
    val totalForecastedCostInr: Double = 0.0,
    val confidenceScore: Double = 85.0,
    val isDemoPrediction: Boolean = true,
    val modelVersion: String = "RandomForestRegressor-v1.0",
    val energySavingTips: List<String> = emptyList(),
    val detectedAnomalies: List<String> = emptyList()
)

data class PredictionDay(
    val id: Long? = 0,
    val predictionType: String = "DAILY_7_DAYS",
    val forecastDate: String = "",
    val predictedConsumptionKwh: Double = 0.0,
    val predictedCostInr: Double = 0.0,
    val confidenceScore: Double = 85.0,
    val isDemoPrediction: Boolean = true,
    val modelVersion: String = "RandomForestRegressor-v1.0",
    val notes: String? = null
)

data class AlertItem(
    val id: Long = 0,
    val title: String = "",
    val message: String = "",
    val alertType: String = "INFO",
    val severity: String = "WARNING",
    val isRead: Boolean = false,
    val isDemoAlert: Boolean = true,
    val createdAt: String? = null
)

data class ScheduleItem(
    val id: Long = 0,
    val applianceId: Long = 0,
    val applianceName: String? = null,
    val scheduleName: String = "",
    val actionType: String = "TURN_OFF",
    val scheduledTime: String = "23:00",
    val repeatDays: String = "DAILY",
    val isActive: Boolean = true
)

data class DeviceSetup(
    val deviceId: String = "ESP32_ENERGYEX_01",
    val deviceName: String = "ESP32 Energy Monitor",
    val ipAddress: String? = "192.168.1.150",
    val authToken: String = "ENERGYEX_IOT_SECURE_TOKEN_2026",
    val status: String = "CONNECTED",
    val isLiveMode: Boolean = false,
    val lastSeenAt: String? = null,
    val wifiSsid: String? = "Home-WiFi"
)

data class SystemMode(
    val mode: String = "DEMO",
    val label: String = "DEMO DATA (SIMULATION MODE)",
    val simulationActive: Boolean = true,
    val message: String = ""
)

data class ControlCommandResult(
    val id: Long = 0,
    val relayChannel: Int = 1,
    val commandState: String = "ON",
    val executionStatus: String = "CONFIRMED",
    val isSimulated: Boolean = true
)

