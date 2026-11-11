package com.energyex.app

import android.app.Application
import com.energyex.app.data.repository.EnergyRepository
import com.energyex.app.data.repository.SessionManager

class EnergyExApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var repository: EnergyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(this)
        repository = EnergyRepository(this)
    }

    companion object {
        lateinit var instance: EnergyExApp
            private set
    }
}

