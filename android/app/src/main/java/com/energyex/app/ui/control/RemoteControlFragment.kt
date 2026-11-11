package com.energyex.app.ui.control

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.data.model.Appliance
import com.energyex.app.databinding.FragmentRemoteControlBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class RemoteControlFragment : Fragment() {

    private var _binding: FragmentRemoteControlBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private val sessionManager get() = EnergyExApp.instance.sessionManager

    private var currentAppliances = listOf<Appliance>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoteControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvControlModeNote.text = if (sessionManager.isDemoMode) {
            "Demo Mode: Relay switching commands are verified via realistic IoT simulation loop."
        } else {
            "Live Mode: Switching commands queued directly for ESP32 4-Channel Relay Module."
        }

        binding.btnMasterAllOn.setOnClickListener {
            switchAll(true)
        }

        binding.btnMasterAllOff.setOnClickListener {
            switchAll(false)
        }

        loadAppliances()
    }

    private fun loadAppliances() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getAppliances()
            result.onSuccess { list ->
                currentAppliances = list
                renderRelayCards(list)
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to load relay channels", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderRelayCards(appliances: List<Appliance>) {
        val container = binding.layoutRelayCardsContainer
        container.removeAllViews()

        for (app in appliances) {
            val card = MaterialCardView(requireContext()).apply {
                radius = 28f
                elevation = 4f
                setCardBackgroundColor(Color.WHITE)
                strokeColor = Color.parseColor("#E0E7EE")
                strokeWidth = 2
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 24) }
            }

            val cardLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(36, 32, 36, 32)
            }

            val titleRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val tvInfo = TextView(requireContext()).apply {
                text = "${app.name} (Channel ${app.relayChannel})\nRated: ${app.powerRatingWatts}W • Draw: ${app.currentPowerW}W"
                textSize = 15f
                setTextColor(Color.parseColor("#1A202C"))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvState = TextView(requireContext()).apply {
                text = if (app.isOn) "STATE: ON" else "STATE: OFF"
                textSize = 13f
                setTextColor(if (app.isOn) Color.parseColor("#00C853") else Color.parseColor("#78909C"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            titleRow.addView(tvInfo)
            titleRow.addView(tvState)

            val btnToggle = Button(requireContext()).apply {
                text = if (app.isOn) "Switch OFF (Relay CH${app.relayChannel})" else "Switch ON (Relay CH${app.relayChannel})"
                setBackgroundColor(if (app.isOn) Color.parseColor("#D50000") else Color.parseColor("#00897B"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 20 }

                setOnClickListener {
                    val targetState = if (app.isOn) "OFF" else "ON"
                    triggerRelayCommand(app, targetState)
                }
            }

            cardLayout.addView(titleRow)
            cardLayout.addView(btnToggle)
            card.addView(cardLayout)
            container.addView(card)
        }
    }

    private fun triggerRelayCommand(appliance: Appliance, targetState: String) {
        binding.pbCommandPending.visibility = View.VISIBLE
        binding.tvLastCommandStatus.text = "Sending command to Relay Channel ${appliance.relayChannel} -> $targetState..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.controlAppliance(appliance.id, targetState)
            binding.pbCommandPending.visibility = View.GONE

            result.onSuccess { cmd ->
                binding.tvLastCommandStatus.text = "Relay CH${appliance.relayChannel} Confirmed: $targetState"
                binding.tvLastCommandSub.text = "Execution status: ${cmd.executionStatus} (Simulated: ${cmd.isSimulated})"
                Toast.makeText(requireContext(), "${appliance.name} is now $targetState", Toast.LENGTH_SHORT).show()
                loadAppliances()
            }.onFailure {
                binding.tvLastCommandStatus.text = "Command transmission failed"
                Toast.makeText(requireContext(), "Command failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun switchAll(turnOn: Boolean) {
        val target = if (turnOn) "ON" else "OFF"
        viewLifecycleOwner.lifecycleScope.launch {
            for (app in currentAppliances) {
                repository.controlAppliance(app.id, target)
            }
            Toast.makeText(requireContext(), "All appliances commanded $target", Toast.LENGTH_SHORT).show()
            loadAppliances()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

