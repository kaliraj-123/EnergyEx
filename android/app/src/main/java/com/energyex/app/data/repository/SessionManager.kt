package com.energyex.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "energyex_session_prefs"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_TARIFF_RATE = "tariff_rate"
        private const val KEY_SERVER_URL = "server_base_url"
        private const val KEY_SYSTEM_MODE = "system_mode"
        
        private val DEFAULT_URL: String
            get() {
                val isEmulator = Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                return if (isEmulator) "http://10.0.2.2:8080/" else "http://localhost:8080/"
            }
    }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Student Engineer") ?: "Student Engineer"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var tariffRate: Float
        get() = prefs.getFloat(KEY_TARIFF_RATE, 7.50f)
        set(value) = prefs.edit().putFloat(KEY_TARIFF_RATE, value).apply()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
        set(value) {
            val formatted = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_SERVER_URL, formatted).apply()
        }

    var systemMode: String
        get() = prefs.getString(KEY_SYSTEM_MODE, "DEMO") ?: "DEMO"
        set(value) = prefs.edit().putString(KEY_SYSTEM_MODE, value).apply()

    val isDemoMode: Boolean
        get() = "DEMO".equals(systemMode, ignoreCase = true)
}

