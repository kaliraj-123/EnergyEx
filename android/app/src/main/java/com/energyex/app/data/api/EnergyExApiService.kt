package com.energyex.app.data.api

import com.energyex.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface EnergyExApiService {

    // Dashboard
    @GET("api/v1/dashboard")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @GET("api/v1/dashboard/history")
    suspend fun getConsumptionHistory(@Query("range") range: String = "daily"): Response<List<EnergyReading>>

    // Appliances
    @GET("api/v1/appliances")
    suspend fun getAppliances(): Response<List<Appliance>>

    @GET("api/v1/appliances/{id}")
    suspend fun getApplianceById(@Path("id") id: Long): Response<Appliance>

    @POST("api/v1/appliances")
    suspend fun createAppliance(@Body appliance: Appliance): Response<Appliance>

    @PUT("api/v1/appliances/{id}")
    suspend fun updateAppliance(@Path("id") id: Long, @Body appliance: Appliance): Response<Appliance>

    @DELETE("api/v1/appliances/{id}")
    suspend fun deleteAppliance(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/appliances/{id}/control")
    suspend fun controlAppliance(
        @Path("id") id: Long,
        @Body request: Map<String, String>
    ): Response<ControlCommandResult>

    // Billing & Tariff
    @GET("api/v1/billing/estimate")
    suspend fun getBillEstimate(): Response<BillEstimation>

    @PUT("api/v1/billing/tariff")
    suspend fun updateTariff(@Body tariff: Map<String, Any>): Response<Map<String, Any>>

    // AI Predictions
    @GET("api/v1/predictions")
    suspend fun getPredictions(): Response<PredictionResponse>

    // Alerts
    @GET("api/v1/alerts")
    suspend fun getAlerts(): Response<List<AlertItem>>

    @PUT("api/v1/alerts/{id}/read")
    suspend fun markAlertAsRead(@Path("id") id: Long): Response<AlertItem>

    @DELETE("api/v1/alerts/{id}")
    suspend fun dismissAlert(@Path("id") id: Long): Response<Unit>

    // Schedules
    @GET("api/v1/schedules")
    suspend fun getSchedules(): Response<List<ScheduleItem>>

    @POST("api/v1/schedules")
    suspend fun createSchedule(@Body schedule: ScheduleItem): Response<ScheduleItem>

    @DELETE("api/v1/schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: Long): Response<Unit>

    // System & IoT Setup
    @GET("api/v1/system/mode")
    suspend fun getSystemMode(): Response<SystemMode>

    @POST("api/v1/system/mode")
    suspend fun switchSystemMode(@Body mode: Map<String, String>): Response<SystemMode>

    @GET("api/v1/system/device")
    suspend fun getDeviceStatus(): Response<DeviceSetup>

    @POST("api/v1/system/device/setup")
    suspend fun configureDevice(@Body device: DeviceSetup): Response<DeviceSetup>

    @POST("api/v1/system/user/setup")
    suspend fun setupFirstTimeUser(@Body userSetup: Map<String, Any>): Response<Map<String, Any>>
}

