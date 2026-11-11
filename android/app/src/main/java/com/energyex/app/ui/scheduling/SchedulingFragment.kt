package com.energyex.app.ui.scheduling

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.energyex.app.EnergyExApp
import com.energyex.app.data.model.Appliance
import com.energyex.app.data.model.ScheduleItem
import com.energyex.app.databinding.FragmentSchedulingBinding
import com.energyex.app.databinding.ItemScheduleBinding
import kotlinx.coroutines.launch

class SchedulingFragment : Fragment() {

    private var _binding: FragmentSchedulingBinding? = null
    private val binding get() = _binding!!

    private val repository get() = EnergyExApp.instance.repository
    private lateinit var adapter: SchedulesAdapter
    private var availableAppliances = listOf<Appliance>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SchedulesAdapter(emptyList()) { schedule ->
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteSchedule(schedule.id)
                loadSchedules()
            }
        }

        binding.rvSchedules.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSchedules.adapter = adapter

        binding.swipeRefreshSchedules.setOnRefreshListener {
            loadSchedules()
        }

        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }

        loadAppliances()
        loadSchedules()
    }

    private fun loadAppliances() {
        viewLifecycleOwner.lifecycleScope.launch {
            val res = repository.getAppliances()
            res.onSuccess { availableAppliances = it }
        }
    }

    private fun loadSchedules() {
        binding.swipeRefreshSchedules.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getSchedules()
            binding.swipeRefreshSchedules.isRefreshing = false

            result.onSuccess { list ->
                if (list.isEmpty()) {
                    binding.tvEmptySchedules.visibility = View.VISIBLE
                    binding.rvSchedules.visibility = View.GONE
                } else {
                    binding.tvEmptySchedules.visibility = View.GONE
                    binding.rvSchedules.visibility = View.VISIBLE
                    adapter.update(list)
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to load schedules", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddScheduleDialog() {
        if (availableAppliances.isEmpty()) {
            Toast.makeText(requireContext(), "No appliances available to schedule", Toast.LENGTH_SHORT).show()
            return
        }

        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(context).apply { hint = "Schedule Name (e.g. Turn off TV at night)" }
        val etTime = EditText(context).apply { hint = "Time in 24hr format HH:mm (e.g. 23:30)" }

        val spinnerAppliance = Spinner(context)
        val appNames = availableAppliances.map { "${it.name} (CH${it.relayChannel})" }
        spinnerAppliance.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, appNames)

        val spinnerAction = Spinner(context)
        spinnerAction.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("TURN_OFF", "TURN_ON"))

        layout.addView(etName)
        layout.addView(spinnerAppliance)
        layout.addView(spinnerAction)
        layout.addView(etTime)

        AlertDialog.Builder(context)
            .setTitle("Create Automatic Schedule")
            .setView(layout)
            .setPositiveButton("Create") { _, _ ->
                val name = etName.text.toString().trim()
                val time = etTime.text.toString().trim()
                val selectedIndex = spinnerAppliance.selectedItemPosition
                val selectedApp = availableAppliances[selectedIndex]
                val action = spinnerAction.selectedItem.toString()

                if (name.isNotEmpty() && time.matches(Regex("\\d{2}:\\d{2}"))) {
                    val newSchedule = ScheduleItem(
                        applianceId = selectedApp.id,
                        applianceName = selectedApp.name,
                        scheduleName = name,
                        actionType = action,
                        scheduledTime = time,
                        repeatDays = "DAILY",
                        isActive = true
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.createSchedule(newSchedule)
                        loadSchedules()
                    }
                } else {
                    Toast.makeText(context, "Please enter valid name and HH:mm time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SchedulesAdapter(
        private var items: List<ScheduleItem>,
        private val onDelete: (ScheduleItem) -> Unit
    ) : RecyclerView.Adapter<SchedulesAdapter.ViewHolder>() {

        fun update(newItems: List<ScheduleItem>) {
            this.items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemScheduleBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ScheduleItem) {
                binding.tvScheduleName.text = item.scheduleName
                val appLabel = item.applianceName ?: "Appliance #${item.applianceId}"
                binding.tvScheduleDetails.text = "$appLabel • ${item.actionType} at ${item.scheduledTime} (${item.repeatDays})"

                binding.btnDeleteSchedule.setOnClickListener {
                    onDelete(item)
                }
            }
        }
    }
}

