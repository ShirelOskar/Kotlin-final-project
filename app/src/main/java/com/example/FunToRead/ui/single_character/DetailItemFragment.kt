// ui/single_character/DetailItemFragment
package com.example.FunToRead.ui.single_character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.FunToRead.R
import com.example.FunToRead.databinding.DetailItemLayoutBinding
import com.example.FunToRead.ui.ItemsViewModel
import com.example.FunToRead.data.model.Item

class DetailItemFragment : Fragment() {

    // View binding for the fragment layout
    private var _binding: DetailItemLayoutBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance for managing UI-related data
    private val viewModel: ItemsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DetailItemLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the chosen item and update the UI with its data
        viewModel.chosenItem.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        // Navigate to EditItemFragment when the edit button is clicked
        binding.editBtn.setOnClickListener {
            findNavController().navigate(R.id.action_detailItemFragment_to_editItemFragment)
        }

        // Navigate to PlayListFragment when the playlist button is clicked
        binding.playlistBtn.setOnClickListener {
            findNavController().navigate(R.id.action_detailItemFragment_to_playListFragment)
        }
    }

    // Update the UI with the item data
    private fun updateUI(item: Item) {
        binding.itemTitle.text = item.title
        binding.itemDescription.text = item.description
        binding.itemStatus.text = item.status
        if (item.status == getString(R.string.already_read) || item.status == "Already Read"  || item.status == "כבר נקרא") {
            binding.itemRating.rating = item.rating ?: 0f
            binding.itemRating.visibility = View.VISIBLE
            binding.itemReview.text = getString(R.string.review) + "\n${item.review}"
            binding.itemReview.visibility = View.VISIBLE
        } else {
            binding.itemRating.visibility = View.GONE
            binding.itemReview.visibility = View.GONE
        }

        if (item.photo == "default") {
            Glide.with(binding.root).load(R.drawable.default_pic).circleCrop().into(binding.itemImage)
        } else {
            Glide.with(binding.root).load(item.photo).circleCrop().into(binding.itemImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "DetailItemFragment"
    }
}
