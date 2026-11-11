package com.energyex.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.energyex.app.EnergyExApp
import com.energyex.app.R
import com.energyex.app.ui.main.MainActivity
import com.energyex.app.ui.onboarding.FirstTimeSetupActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = EnergyExApp.instance.sessionManager
            if (sessionManager.isFirstLaunch) {
                startActivity(Intent(this, FirstTimeSetupActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 1200)
    }
}

