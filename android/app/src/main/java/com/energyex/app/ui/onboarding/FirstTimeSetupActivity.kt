package com.energyex.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.databinding.ActivityFirstTimeSetupBinding
import com.energyex.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class FirstTimeSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstTimeSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstTimeSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = EnergyExApp.instance.sessionManager
        val repository = EnergyExApp.instance.repository

        binding.etUserName.setText(sessionManager.userName)
        binding.etTariffRate.setText(sessionManager.tariffRate.toString())
        binding.etServerUrl.setText(sessionManager.serverUrl)

        binding.btnGetStarted.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val tariffStr = binding.etTariffRate.text.toString().trim()
            val serverUrl = binding.etServerUrl.text.toString().trim()

            if (name.isEmpty()) {
                binding.etUserName.error = "Please enter your name"
                return@setOnClickListener
            }

            val tariff = tariffStr.toDoubleOrNull() ?: 7.50

            sessionManager.userName = name
            sessionManager.tariffRate = tariff.toFloat()
            sessionManager.serverUrl = serverUrl

            binding.btnGetStarted.isEnabled = false
            binding.btnGetStarted.text = "Configuring..."

            lifecycleScope.launch {
                // Attempt to register setup with backend
                repository.setupFirstTimeUser(name, tariff)

                sessionManager.isFirstLaunch = false
                Toast.makeText(this@FirstTimeSetupActivity, "Welcome to EnergyEx, $name!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@FirstTimeSetupActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}

