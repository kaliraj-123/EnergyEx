package com.energyex.app.data.repository

import android.content.Context
import com.energyex.app.data.api.ApiClient
import com.energyex.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnergyRepository(private val context: Context) {

    private val apiService get() = ApiClient.getService(context)
    val sessionManager = SessionManager(context)

    suspend fun getDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch dashboard: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConsumptionHistory(range: String = "daily"): Result<List<EnergyReading>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getConsumptionHistory(range)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("History error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppliances(): Result<List<Appliance>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAppliances()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Appliances error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppliance(appliance: Appliance): Result<Appliance> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createAppliance(appliance)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Create appliance error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun controlAppliance(applianceId: Long, state: String): Result<ControlCommandResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.controlAppliance(applianceId, mapOf("state" to state))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Control error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBillEstimate(): Result<BillEstimation> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBillEstimate()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Bill estimation error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTariffRate(newRate: Double): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTariff(mapOf("ratePerKwh" to newRate))
            if (response.isSuccessful) {
                sessionManager.tariffRate = newRate.toFloat()
                Result.success(true)
            } else {
                Result.failure(Exception("Tariff update error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPredictions(): Result<PredictionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPredictions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Prediction error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlerts(): Result<List<AlertItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAlerts()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Alerts error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAlertAsRead(alertId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAlertAsRead(alertId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dismissAlert(alertId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.dismissAlert(alertId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSchedules(): Result<List<ScheduleItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSchedules()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Schedules error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSchedule(schedule: ScheduleItem): Result<ScheduleItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createSchedule(schedule)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Create schedule error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(scheduleId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteSchedule(scheduleId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemMode(): Result<SystemMode> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSystemMode()
            if (response.isSuccessful && response.body() != null) {
                sessionManager.systemMode = response.body()!!.mode
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("System mode error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun switchSystemMode(mode: String): Result<SystemMode> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.switchSystemMode(mapOf("mode" to mode))
            if (response.isSuccessful && response.body() != null) {
                sessionManager.systemMode = response.body()!!.mode
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Mode switch error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviceStatus(): Result<DeviceSetup> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDeviceStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Device status error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun configureDevice(device: DeviceSetup): Result<DeviceSetup> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.configureDevice(device)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Configure device error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setupFirstTimeUser(name: String, tariffRate: Double): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.setupFirstTimeUser(mapOf(
                "name" to name,
                "tariffRate" to tariffRate
            ))
            if (response.isSuccessful) {
                sessionManager.userName = name
                sessionManager.tariffRate = tariffRate.toFloat()
                sessionManager.isFirstLaunch = false
                Result.success(true)
            } else {
                Result.failure(Exception("User setup error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

