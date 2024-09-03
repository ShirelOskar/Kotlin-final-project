//ui/all_character/AllItemFragment
package com.example.FunToRead.ui.all_character

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.FunToRead.R
import com.example.FunToRead.data.alarm.AlarmBroadcastReceiver
import com.example.FunToRead.data.model.Item
import com.example.FunToRead.databinding.AllItemsLayoutBinding
import com.example.FunToRead.ui.ItemsViewModel

class AllItemsFragment : Fragment() {

    // View binding for the fragment layout
    private var _binding: AllItemsLayoutBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance for managing UI-related data
    private val viewModel: ItemsViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesListener: SharedPreferences.OnSharedPreferenceChangeListener

    // Adapters for the different RecyclerViews
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var toReadItemAdapter: ItemAdapter
    private lateinit var alreadyReadItemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Enable options menu in the fragment
        setHasOptionsMenu(true)
        _binding = AllItemsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        sharedPreferences = requireContext().getSharedPreferences("reading_prefs", Context.MODE_PRIVATE)

        // set up the 3 Recyclers for the books: to read, already read, all books
        setupRecyclers()
        // set up Observers to react to data changes
        setupObservers()

        // when clicking the 'add new item' btn
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_allItemsFragment_to_addItemFragment)
        }

        // when clicking the 'timer' btn
        binding.timerBtn.setOnClickListener {
            // Request permission to post notifications before showing the timer dialog
            requestNotificationPermission()
        }

        // Observe total reading time
        viewModel.totalReadingTime.observe(viewLifecycleOwner) { totalTime ->
            binding.totalReadingTimeTextView.text = getString(R.string.total_reading_time, totalTime)
        }

        sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (_binding != null && key == "total_reading_time") {
                val totalTime = sharedPreferences.getInt("total_reading_time", 0)
                binding.totalReadingTimeTextView.text = getString(R.string.total_reading_time, totalTime)
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences("reading_prefs", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("total_reading_time")) {
            with(sharedPreferences.edit()) {
                putInt("total_reading_time", 0)
                apply()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val totalTime = sharedPreferences.getInt("total_reading_time", 0)
        binding.totalReadingTimeTextView.text = getString(R.string.total_reading_time, totalTime)
    }

    // Set up RecyclerViews for "to read", "already read", and "all items"
    private fun setupRecyclers() {
        itemAdapter = ItemAdapter(emptyList(), createItemListener(binding.recycle))
        binding.recycle.adapter = itemAdapter
        binding.recycle.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        toReadItemAdapter = ItemAdapter(emptyList(), createItemListener(binding.toReadRecycle))
        binding.toReadRecycle.adapter = toReadItemAdapter
        binding.toReadRecycle.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        alreadyReadItemAdapter =
            ItemAdapter(emptyList(), createItemListener(binding.alreadyReadRecycle))
        binding.alreadyReadRecycle.adapter = alreadyReadItemAdapter
        binding.alreadyReadRecycle.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    // Set up observers to update the UI when the data changes
    private fun setupObservers() {
        viewModel.items?.observe(viewLifecycleOwner) {
            itemAdapter.setItems(it)
        }

        viewModel.toReadItems?.observe(viewLifecycleOwner) {
            toReadItemAdapter.setItems(it)
        }

        viewModel.alreadyReadItems?.observe(viewLifecycleOwner) {
            alreadyReadItemAdapter.setItems(it)
        }
    }

    // Create an ItemListener for handling click and long click events on RecyclerView items
    private fun createItemListener(recyclerView: RecyclerView): ItemAdapter.ItemListener {
        return object : ItemAdapter.ItemListener {
            override fun onItemClicked(index: Int) {
                val adapter = recyclerView.adapter as ItemAdapter
                viewModel.setItem(adapter.itemAt(index))
                findNavController().navigate(R.id.action_allItemsFragment_to_detailItemFragment)
            }

            override fun onItemLongClicked(index: Int): Boolean {
                val adapter = recyclerView.adapter as ItemAdapter
                val item = adapter.itemAt(index)

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(getString(R.string.confirm_delete))
                    .setMessage(getString(R.string.confirm_delete_message))
                    .setPositiveButton(getString(R.string.YES)) { _, _ ->
                        viewModel.deleteItem(item)
                        Toast.makeText(requireContext(), getString(R.string.item_deleted), Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(getString(R.string.NO)) { dialog, _ ->
                        dialog.dismiss()
                        adapter.notifyItemChanged(index) // Restore item view if deletion is canceled
                    }
                    .show()

                return true
            }
        }
    }

    // inflater options menu, where the title is and the delete-all btn
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // delete-all btn, that's on the options
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.message_when_deleting_all_items))
                .setPositiveButton(getString(R.string.YES)) { _, _ ->
                    viewModel.deleteAll()
                    Toast.makeText(requireContext(), getString(R.string.items_deleted), Toast.LENGTH_SHORT).show()
                }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Handle the result of the permission request
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted, proceed with setting the alarm
                showTimerDialog()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

    // Request permission to post notifications
    private fun requestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED -> {
                // Request POST_NOTIFICATIONS permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Explain why you need these permissions
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.permissions_needed))
                    .setMessage(getString(R.string.permissions_needed_message_for_alarms))
                    .setPositiveButton(getString(R.string.OK)) { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .create()
                    .show()
            }
            else -> {
                // Permissions are already granted, proceed with setting the alarm
                showTimerDialog()
            }
        }
    }

    // Show a dialog for setting the timer duration
    private fun showTimerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_timer, null)
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(getString(R.string.set_timer))

        val dialog = dialogBuilder.create()

        dialogView.findViewById<View>(R.id.btnSetTimer).setOnClickListener {
            // Handle setting the alarm with user-defined duration
            val inputMinutes = dialogView.findViewById<EditText>(R.id.etTimerMinutes).text.toString()
            if (inputMinutes.isNotBlank()) {
                val minutes = inputMinutes.toInt()
                setAlarm(minutes)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), R.string.Please_enter_a_valid_number_of_minutes, Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // Set the alarm with the specified duration in minutes
    private fun setAlarm(minutes: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmBroadcastReceiver::class.java).apply {
            putExtra("reading_time", minutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate trigger time in milliseconds
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // On Android M and above, use setExactAndAllowWhileIdle for exact alarms
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Prior to Android M, use setExact
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarm set for $minutes minutes from now")
        } catch (e: SecurityException) {
            // Handle SecurityException here (e.g., show a toast, log the error)
            Toast.makeText(requireContext(), getString(R.string.Failed_to_set_alarm)+  "${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to set alarm", e)
        }

        Toast.makeText(requireContext(), getString(R.string.Alarm_set_for) + "$minutes " + getString(R.string.minutes_from_now), Toast.LENGTH_SHORT).show()
    }

    // Update the total reading time in shared preferences
    private fun updateTotalReadingTime(minutes: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("reading_prefs", Context.MODE_PRIVATE)
        val totalReadingTime = sharedPreferences.getInt("total_reading_time", 0)
        with(sharedPreferences.edit()) {
            putInt("total_reading_time", totalReadingTime + minutes)
            apply()
        }
    }

    companion object {
        private const val TAG = "AllItemFragment"
    }
}
