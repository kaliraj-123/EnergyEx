package com.energyex.app.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private val sessionManager get() = EnergyExApp.instance.sessionManager

    private val chartPoints = mutableListOf<Float>()
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            loadDashboardData()
        }

        binding.btnToggleMode.setOnClickListener {
            toggleSystemMode()
        }

        loadDashboardData()
        startLivePolling()
    }

    private fun startLivePolling() {
        pollingJob?.cancel()
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(3000)
                loadDashboardData(isSilent = true)
            }
        }
    }

    private fun loadDashboardData(isSilent: Boolean = false) {
        if (!isSilent) binding.swipeRefresh.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getDashboardSummary()
            if (!isSilent) binding.swipeRefresh.isRefreshing = false

            result.onSuccess { summary ->
                binding.tvTotalKwh.text = String.format(Locale.US, "%.2f kWh", summary.totalEnergyKwh)
                binding.tvTodayKwh.text = String.format(Locale.US, "Today: %.2f kWh", summary.todayEnergyKwh)
                binding.tvEstimatedBill.text = String.format(Locale.US, "₹ %.2f", summary.estimatedBillInr)
                binding.tvCurrentWatts.text = String.format(Locale.US, "%.1f W", summary.currentPowerWatts)
                binding.tvLiveVoltsAmps.text = String.format(
                    Locale.US,
                    "%.1f V • %.2f A",
                    summary.currentVoltageV,
                    summary.currentCurrentA
                )
                binding.tvConnectedAppliances.text = "${summary.activeAppliances} ON / ${summary.totalAppliances} Total"
                binding.tvTariffRateLabel.text = "Tariff: ₹${sessionManager.tariffRate} / unit"
                binding.tvDeviceStatus.text = "Device: ${summary.deviceStatus ?: "ACTIVE"}"

                // Chart dynamic data
                chartPoints.add(summary.currentPowerWatts.toFloat())
                if (chartPoints.size > 14) chartPoints.removeAt(0)
                binding.energyChartView.setData(chartPoints)

                // Update Mode Banner
                val isDemo = "DEMO".equals(summary.systemMode, ignoreCase = true)
                if (isDemo) {
                    binding.tvModeBannerTitle.text = "DEMO DATA (SIMULATION ACTIVE)"
                    binding.tvModeBannerDesc.text = "Generating realistic domestic loads. Connect ESP32 for live hardware."
                    binding.cardModeBanner.setCardBackgroundColor(Color.parseColor("#F3E5F5"))
                    binding.cardModeBanner.strokeColor = Color.parseColor("#6200EA")
                } else {
                    binding.tvModeBannerTitle.text = "LIVE IoT MODE (ESP32 PZEM-004T)"
                    binding.tvModeBannerDesc.text = "Ingesting actual sensor readings from hardware microcontroller."
                    binding.cardModeBanner.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                    binding.cardModeBanner.strokeColor = Color.parseColor("#00C853")
                }
            }.onFailure { error ->
                if (!isSilent) {
                    Toast.makeText(requireContext(), "Offline: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleSystemMode() {
        val currentMode = sessionManager.systemMode
        val nextMode = if ("DEMO".equals(currentMode, ignoreCase = true)) "LIVE_IOT" else "DEMO"

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.switchSystemMode(nextMode)
            result.onSuccess { mode ->
                Toast.makeText(requireContext(), "Switched to ${mode.label}", Toast.LENGTH_SHORT).show()
                loadDashboardData()
            }.onFailure {
                Toast.makeText(requireContext(), "Mode switch failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingJob?.cancel()
        _binding = null
    }
}

