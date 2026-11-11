package com.energyex.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.data.api.ApiClient
import com.energyex.app.databinding.FragmentSettingsBinding
import com.energyex.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val sessionManager get() = EnergyExApp.instance.sessionManager
    private val repository get() = EnergyExApp.instance.repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etSettingsUserName.setText(sessionManager.userName)
        binding.etSettingsTariff.setText(sessionManager.tariffRate.toString())
        binding.etSettingsServerUrl.setText(sessionManager.serverUrl)

        binding.btnSaveSettings.setOnClickListener {
            val name = binding.etSettingsUserName.text.toString().trim()
            val tariff = binding.etSettingsTariff.text.toString().toDoubleOrNull() ?: 7.50
            val serverUrl = binding.etSettingsServerUrl.text.toString().trim()

            if (name.isNotEmpty()) {
                sessionManager.userName = name
                sessionManager.tariffRate = tariff.toFloat()
                sessionManager.serverUrl = serverUrl
                ApiClient.reset()

                viewLifecycleOwner.lifecycleScope.launch {
                    repository.updateTariffRate(tariff)
                    repository.setupFirstTimeUser(name, tariff)
                    (activity as? MainActivity)?.updateHeader()
                    Toast.makeText(requireContext(), "Settings successfully saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

