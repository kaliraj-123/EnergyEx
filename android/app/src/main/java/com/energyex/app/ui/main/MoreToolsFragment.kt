package com.energyex.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.energyex.app.R
import com.energyex.app.databinding.FragmentMoreToolsBinding
import com.energyex.app.ui.alerts.AlertsFragment
import com.energyex.app.ui.billing.BillEstimationFragment
import com.energyex.app.ui.control.RemoteControlFragment
import com.energyex.app.ui.history.ConsumptionHistoryFragment
import com.energyex.app.ui.iotsetup.IoTSetupFragment
import com.energyex.app.ui.scheduling.SchedulingFragment
import com.energyex.app.ui.settings.SettingsFragment

class MoreToolsFragment : Fragment() {

    private var _binding: FragmentMoreToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardNavHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ConsumptionHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardNavBilling.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BillEstimationFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardNavControl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RemoteControlFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardNavScheduling.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SchedulingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardNavAlerts.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AlertsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardNavIoTSetup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, IoTSetupFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardNavSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

