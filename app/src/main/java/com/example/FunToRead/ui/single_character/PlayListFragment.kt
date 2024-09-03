//ui/single_character/PlayListFragment
package com.example.FunToRead.ui.single_character

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.FunToRead.data.model.Item
import com.example.FunToRead.databinding.ItemPlaylistLayoutBinding
import com.example.FunToRead.ui.ItemsViewModel

class PlayListFragment : Fragment() {

    // View binding for the fragment layout
    private var _binding: ItemPlaylistLayoutBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance for managing UI-related data
    private val viewModel: ItemsViewModel by activityViewModels()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemPlaylistLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the chosen item and update the UI with its data
        viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
            binding.bookTitle.text = item.title

            // Setup WebView with JavaScript enabled
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.webViewClient = WebViewClient()
            binding.webView.webChromeClient = WebChromeClient()

            // Setup spinner with playlist URLs
            setupSpinner(item)

            // Add URL button click listener
            binding.buttonAddUrl.setOnClickListener {
                val newUrl = binding.editTextUrl.text.toString()
                if (newUrl.isNotEmpty()) {
                    addItemToPlaylist(item, newUrl)
                }
            }
        }
    }

    // Set up the spinner with the item's playlist URLs
    private fun setupSpinner(item: Item) {
        val playlist = item.playlist ?: emptyList()
        val playlistAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            playlist
        )
        playlistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUrls.adapter = playlistAdapter

        // Check if the playlist is empty and load the default URL if it is
        if (playlist.isEmpty()) {
            binding.webView.loadUrl("https://www.youtube.com/watch?v=AUw7laSlcbo&t=78s")
        } else {
            binding.spinnerUrls.setSelection(0) // Select first item by default
        }

        // Handle selection of URLs from the spinner
        binding.spinnerUrls.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUrl = parent?.getItemAtPosition(position) as String
                binding.webView.loadUrl(selectedUrl)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    // Add a new URL to the item's playlist
    private fun addItemToPlaylist(item: Item, newUrl: String) {
        val updatedItem = item.addToPlaylist(newUrl)
        viewModel.updateItem(updatedItem) // Ensure the view model handles the update
        setupSpinner(updatedItem) // Refresh the spinner with the updated playlist
        binding.editTextUrl.text.clear() // Clear the input field
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
