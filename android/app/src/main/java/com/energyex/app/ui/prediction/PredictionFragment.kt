package com.energyex.app.ui.prediction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.energyex.app.EnergyExApp
import com.energyex.app.databinding.FragmentPredictionBinding
import kotlinx.coroutines.launch
import java.util.Locale

class PredictionFragment : Fragment() {

    private var _binding: FragmentPredictionBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshPrediction.setOnRefreshListener {
            loadAiPredictions()
        }

        loadAiPredictions()
    }

    private fun loadAiPredictions() {
        binding.swipeRefreshPrediction.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getPredictions()
            binding.swipeRefreshPrediction.isRefreshing = false

            result.onSuccess { data ->
                binding.tvForecastTotalKwh.text = String.format(Locale.US, "%.2f kWh", data.totalForecastedKwh)
                binding.tvForecastTotalCost.text = String.format(Locale.US, "₹ %.2f", data.totalForecastedCostInr)
                binding.tvModelConfidence.text = "Model Evaluation Confidence: ${data.confidenceScore}% (${data.modelVersion})"

                val demoFlag = if (data.isDemoPrediction) "DEMO DATA FORECAST" else "TRAINED HARDWARE FORECAST"
                binding.tvAiModelBadge.text = "${data.modelVersion} • $demoFlag"

                // Setup chart
                val points = data.forecastDays.map { it.predictedConsumptionKwh.toFloat() }
                val labels = data.forecastDays.map {
                    val parts = it.forecastDate.split("-")
                    if (parts.size == 3) "${parts[2]}/${parts[1]}" else it.forecastDate
                }
                if (points.isNotEmpty()) {
                    binding.predictionChart.setData(points, labels)
                }

                if (data.energySavingTips.isNotEmpty()) {
                    binding.tvRecommendations.text = data.energySavingTips.joinToString("\n\n") { "• $it" }
                }

                if (data.detectedAnomalies.isNotEmpty()) {
                    binding.tvAnomaliesSummary.text = data.detectedAnomalies.joinToString("\n") { "• $it" }
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Forecast: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

