package com.energyex.app.ui.alerts

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.energyex.app.EnergyExApp
import com.energyex.app.data.model.AlertItem
import com.energyex.app.databinding.FragmentAlertsBinding
import com.energyex.app.databinding.ItemAlertBinding
import kotlinx.coroutines.launch

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private lateinit var adapter: AlertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AlertsAdapter(emptyList()) { alert ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.dismissAlert(alert.id)
                loadAlerts()
            }
        }

        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter

        binding.swipeRefreshAlerts.setOnRefreshListener {
            loadAlerts()
        }

        loadAlerts()
    }

    private fun loadAlerts() {
        binding.swipeRefreshAlerts.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getAlerts()
            binding.swipeRefreshAlerts.isRefreshing = false

            result.onSuccess { list ->
                if (list.isEmpty()) {
                    binding.tvEmptyAlerts.visibility = View.VISIBLE
                    binding.rvAlerts.visibility = View.GONE
                } else {
                    binding.tvEmptyAlerts.visibility = View.GONE
                    binding.rvAlerts.visibility = View.VISIBLE
                    adapter.update(list)
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to fetch alerts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AlertsAdapter(
        private var items: List<AlertItem>,
        private val onDismiss: (AlertItem) -> Unit
    ) : RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {

        fun update(newItems: List<AlertItem>) {
            this.items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemAlertBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(alert: AlertItem) {
                binding.tvAlertTitle.text = alert.title
                binding.tvAlertMessage.text = alert.message
                binding.tvAlertSeverityBadge.text = alert.severity

                if ("CRITICAL".equals(alert.severity, ignoreCase = true)) {
                    binding.tvAlertSeverityBadge.setTextColor(Color.parseColor("#D50000"))
                } else {
                    binding.tvAlertSeverityBadge.setTextColor(Color.parseColor("#FFA000"))
                }

                binding.tvAlertTime.text = alert.createdAt?.take(16)?.replace("T", " ") ?: "Recent"

                binding.btnDismissAlert.setOnClickListener {
                    onDismiss(alert)
                }
            }
        }
    }
}

