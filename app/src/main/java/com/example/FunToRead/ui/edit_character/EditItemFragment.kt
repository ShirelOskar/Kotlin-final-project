// ui/edit_character/EditItemFragment
package com.example.FunToRead.ui.edit_character

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.FunToRead.R
import com.example.FunToRead.databinding.EditItemLayoutBinding
import com.example.FunToRead.ui.ItemsViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditItemFragment : Fragment() {

    // View binding for the fragment layout
    private var _binding: EditItemLayoutBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance for managing UI-related data
    private  val viewModel: ItemsViewModel by activityViewModels()

    // URIs for storing image data
    private var imageUri: Uri? = Uri.parse("default")
    private var imageUri_temp: Uri? = Uri.parse("default")
    private lateinit var currentPhotoPath: String

    // Launcher for picking an image from the gallery
    private val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.resultImage.setImageURI(it)
            it?.let { uri ->
                requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = uri
            }
        }

    // Launcher for requesting camera permission
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher for taking a picture with the camera
    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.resultImage.setImageURI(imageUri_temp)
                imageUri=imageUri_temp
            } else {
                binding.resultImage.setImageURI(imageUri)
                Toast.makeText(requireContext(), "Failed to take picture", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EditItemLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the chosen item and populate the UI with its data
        viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
            binding.itemTitle.setText(item.title)
            binding.itemDescription.setText(item.description)
            if (item.status == getString(R.string.already_read)) {
                binding.radioAlreadyRead.isChecked = true
                binding.readDetailsLayout.visibility = View.VISIBLE
                binding.ratingBar.rating = item.rating ?: 0f
                binding.reviewText.setText(item.review)
            } else {
                binding.radioToRead.isChecked = true
                binding.readDetailsLayout.visibility = View.GONE
            }
            if (item.photo == "default") {
                Glide.with(binding.root).load(R.drawable.default_pic).circleCrop().into(binding.resultImage)
            } else {
                imageUri = Uri.parse(item.photo)
                Glide.with(binding.root).load(item.photo).circleCrop().into(binding.resultImage)
            }
        }

        // Handle changes in the status radio group
        binding.radioGroupStatus.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.radio_already_read) {
                binding.readDetailsLayout.visibility = View.VISIBLE
            } else {
                binding.readDetailsLayout.visibility = View.GONE
            }
        }

        // Handle the finish button click to save the updated item
        binding.finishBtn.setOnClickListener {
            var updatedTitle= "No Title"
            if (binding.itemTitle.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.missing_title), Toast.LENGTH_SHORT).show()
            }else{
                updatedTitle = binding.itemTitle.text.toString()
                val updatedDescription = binding.itemDescription.text.toString()
                val updatedStatus = if (binding.radioAlreadyRead.isChecked) getString(R.string.already_read) else getString(R.string.to_read)
                val updatedRating = if (updatedStatus == getString(R.string.already_read)) binding.ratingBar.rating else null
                val updatedReview = if (updatedStatus == getString(R.string.already_read)) binding.reviewText.text.toString() else null

                val updatedImg = if (imageUri.toString() == "default") viewModel.chosenItem.value?.photo else imageUri.toString()

                // Create updated item with new data including the potentially updated photo
                val updatedItem = viewModel.chosenItem.value?.copy(
                    title = updatedTitle,
                    description = updatedDescription,
                    status = updatedStatus,
                    rating = updatedRating,
                    review = updatedReview,
                    photo = updatedImg
                )

                if (updatedItem != null) {
                    viewModel.updateItem(updatedItem)
                    viewModel.setItem(updatedItem) // Update chosen item in the ViewModel
                    findNavController().navigateUp()
                }
            }

        }

        // Handle the image button click to choose between gallery and camera
        binding.imageBtn.setOnClickListener {
            // Open a dialog or menu to choose between gallery and camera
            showImageSourceDialog()
        }

        // Handle changes in the status radio group
        binding.radioGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_already_read) {
                binding.readDetailsLayout.visibility = View.VISIBLE
            } else {
                binding.readDetailsLayout.visibility = View.GONE
            }
        }
    }

    // Show a dialog to choose between picking an image from the gallery or taking a picture
    private fun showImageSourceDialog() {
        val options = arrayOf(getString(R.string.choose_from_gallery), getString(R.string.take_a_picture))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.select_image_source))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageLauncher.launch(arrayOf("image/*"))
                    1 -> checkCameraPermissionAndTakePicture()
                }
            }
        builder.show()
    }

    // Check if the camera permission is granted and launch the camera if it is
    private fun checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            dispatchTakePictureIntent()
        }
    }

    // Dispatch an intent to take a picture with the camera
    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.FunToRead.provider",
                it
            )
            imageUri_temp = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    // Create a file for storing the captured image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // Clean up view binding when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}