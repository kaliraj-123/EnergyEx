package com.energyex.app.ui.main

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.R
import com.energyex.app.databinding.ActivityMainBinding
import com.energyex.app.ui.alerts.AlertsFragment
import com.energyex.app.ui.appliances.ApplianceListFragment
import com.energyex.app.ui.dashboard.DashboardFragment
import com.energyex.app.ui.live.LiveMonitoringFragment
import com.energyex.app.ui.prediction.PredictionFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sessionManager get() = EnergyExApp.instance.sessionManager
    private val repository get() = EnergyExApp.instance.repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateHeader()

        binding.chipSystemMode.setOnClickListener {
            toggleMode()
        }

        binding.btnAlertsIcon.setOnClickListener {
            switchFragment(AlertsFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    switchFragment(DashboardFragment())
                    true
                }
                R.id.nav_appliances -> {
                    switchFragment(ApplianceListFragment())
                    true
                }
                R.id.nav_live_monitor -> {
                    switchFragment(LiveMonitoringFragment())
                    true
                }
                R.id.nav_prediction -> {
                    switchFragment(PredictionFragment())
                    true
                }
                R.id.nav_more -> {
                    switchFragment(MoreToolsFragment())
                    true
                }
                else -> false
            }
        }

        // Default screen is Dashboard
        if (savedInstanceState == null) {
            switchFragment(DashboardFragment())
        }
    }

    fun updateHeader() {
        binding.tvHeaderGreeting.text = "⚡ EnergyEx"
        binding.tvHeaderSub.text = "Hello, ${sessionManager.userName}"

        val isDemo = sessionManager.isDemoMode
        if (isDemo) {
            binding.chipSystemMode.text = "DEMO DATA"
            binding.chipSystemMode.setChipBackgroundColorResource(R.color.status_demo)
        } else {
            binding.chipSystemMode.text = "LIVE IoT MODE"
            binding.chipSystemMode.setChipBackgroundColorResource(R.color.status_online)
        }
    }

    private fun toggleMode() {
        val nextMode = if (sessionManager.isDemoMode) "LIVE_IOT" else "DEMO"
        lifecycleScope.launch {
            val result = repository.switchSystemMode(nextMode)
            result.onSuccess {
                updateHeader()
                Toast.makeText(this@MainActivity, "System switched to ${it.label}", Toast.LENGTH_SHORT).show()
                // Refresh current fragment
                val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (current != null) {
                    supportFragmentManager.beginTransaction().detach(current).attach(current).commit()
                }
            }.onFailure {
                Toast.makeText(this@MainActivity, "Mode change failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

