package com.energyex.app.ui.iotsetup

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.data.model.DeviceSetup
import com.energyex.app.databinding.FragmentIotSetupBinding
import com.energyex.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class IoTSetupFragment : Fragment() {

    private var _binding: FragmentIotSetupBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private val sessionManager get() = EnergyExApp.instance.sessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIotSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetDemoMode.setOnClickListener {
            switchMode("DEMO")
        }

        binding.btnSetLiveMode.setOnClickListener {
            switchMode("LIVE_IOT")
        }

        binding.btnSaveDeviceSettings.setOnClickListener {
            saveDeviceConfig()
        }

        loadDeviceStatus()
    }

    private fun loadDeviceStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getDeviceStatus()
            result.onSuccess { dev ->
                binding.tvDeviceName.text = dev.deviceName
                binding.tvDeviceIp.text = "IP: ${dev.ipAddress ?: "Awaiting DHCP"} • Wi-Fi: ${dev.wifiSsid ?: "Home-WiFi"}"
                binding.etDeviceId.setText(dev.deviceId)
                binding.etDeviceToken.setText(dev.authToken)

                val isLive = dev.isLiveMode
                if (isLive) {
                    binding.tvDeviceHealthBadge.text = "● ${dev.status}"
                    binding.tvDeviceHealthBadge.setTextColor(Color.parseColor("#00C853"))
                } else {
                    binding.tvDeviceHealthBadge.text = "● SIMULATED ACTIVE (DEMO MODE)"
                    binding.tvDeviceHealthBadge.setTextColor(Color.parseColor("#6200EA"))
                }

                binding.tvDeviceLastSeen.text = "Last seen: ${dev.lastSeenAt?.take(16)?.replace("T", " ") ?: "Now"}"
            }
        }
    }

    private fun switchMode(mode: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.switchSystemMode(mode)
            result.onSuccess {
                Toast.makeText(requireContext(), "System mode set to ${it.label}", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.updateHeader()
                loadDeviceStatus()
            }.onFailure {
                Toast.makeText(requireContext(), "Mode switch failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDeviceConfig() {
        val deviceId = binding.etDeviceId.text.toString().trim()
        val token = binding.etDeviceToken.text.toString().trim()

        if (deviceId.isNotEmpty() && token.isNotEmpty()) {
            val config = DeviceSetup(
                deviceId = deviceId,
                authToken = token,
                deviceName = "ESP32 Energy Monitor",
                isLiveMode = !sessionManager.isDemoMode
            )
            viewLifecycleOwner.lifecycleScope.launch {
                val res = repository.configureDevice(config)
                res.onSuccess {
                    Toast.makeText(requireContext(), "Device settings saved successfully", Toast.LENGTH_SHORT).show()
                    loadDeviceStatus()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

