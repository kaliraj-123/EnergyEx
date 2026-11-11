package com.energyex.app.ui.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.databinding.FragmentBillingBinding
import kotlinx.coroutines.launch
import java.util.Locale

class BillEstimationFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private val sessionManager get() = EnergyExApp.instance.sessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etEditTariff.setText(sessionManager.tariffRate.toString())

        binding.btnUpdateTariff.setOnClickListener {
            val newRate = binding.etEditTariff.text.toString().toDoubleOrNull()
            if (newRate != null && newRate > 0) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = repository.updateTariffRate(newRate)
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Tariff updated to ₹$newRate / kWh", Toast.LENGTH_SHORT).show()
                        loadBillEstimation()
                    }.onFailure {
                        Toast.makeText(requireContext(), "Failed to update tariff", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid rate", Toast.LENGTH_SHORT).show()
            }
        }

        loadBillEstimation()
    }

    private fun loadBillEstimation() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getBillEstimate()
            result.onSuccess { est ->
                binding.tvBillingUnits.text = String.format(Locale.US, "%.2f kWh", est.currentEnergyKwh)
                binding.tvBillingRate.text = String.format(Locale.US, "₹ %.2f / kWh", est.defaultTariffRate)
                binding.tvFixedCharge.text = String.format(Locale.US, "₹ %.2f", est.fixedCharge)
                binding.tvBaseCost.text = String.format(Locale.US, "₹ %.2f", est.calculatedBaseCost)
                binding.tvProjectedBill.text = String.format(Locale.US, "₹ %.2f", est.projectedMonthlyBillInr)
                binding.tvProjectedKwh.text = String.format(
                    Locale.US,
                    "Based on current daily consumption rate (~%.1f kWh/month)",
                    est.projectedMonthlyKwh
                )

                if (est.slabBreakdown.isNotEmpty()) {
                    val breakdown = est.slabBreakdown.joinToString("\n") { slab ->
                        "• ${slab.slabName}: ${String.format(Locale.US, "%.2f", slab.unitsInSlab)} units @ ₹${slab.ratePerKwh} = ₹${String.format(Locale.US, "%.2f", slab.amountInr)}"
                    } + "\n• Fixed Grid Charges: ₹${est.fixedCharge}\n\nTotal Estimated Slab Bill: ₹${est.calculatedSlabCost}"
                    binding.tvSlabBreakdownText.text = breakdown
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Bill error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

