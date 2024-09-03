//ui/add_character/AddItemFragment
package com.example.FunToRead.ui.add_character

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.FunToRead.R
import com.example.FunToRead.data.model.Item
import com.example.FunToRead.databinding.AddItemLayoutBinding
import com.example.FunToRead.ui.ItemsViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddItemFragment : Fragment() {

    // Binding for the layout
    private var _binding: AddItemLayoutBinding? = null
    private val binding get() = _binding!!

    // ViewModel to share data between fragments
    private val viewModel: ItemsViewModel by activityViewModels()

    // URI for the image
    private var imageUri: Uri? = Uri.parse("default")
    private lateinit var currentPhotoPath: String

    // Launcher to pick an image from the gallery
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

    // Launcher to request camera permission
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(requireContext(), getString(R.string.Camera_permission_is_required), Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher to take a picture with the camera
    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.resultImage.setImageURI(imageUri)
            } else {
                Toast.makeText(requireContext(), getString(R.string.Failed_to_take_picture), Toast.LENGTH_SHORT).show()
            }
        }

    // Inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddItemLayoutBinding.inflate(inflater, container, false)

        // Set click listener for the finish button
        binding.finishBtn.setOnClickListener {
            val title = binding.itemTitle.text.toString()
            val description = binding.itemDescription.text.toString()
            val status = if (binding.radioAlreadyRead.isChecked) getString(R.string.already_read) else getString(R.string.to_read)
            val rating =
                if (binding.readDetailsLayout.visibility == View.VISIBLE) binding.ratingBar.rating else null
            val review =
                if (binding.readDetailsLayout.visibility == View.VISIBLE) binding.reviewText.text.toString() else null
            val item = Item(0, title, description, status, rating, review, imageUri?.toString(),null)
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.missing_title), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addItem(item)
                findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment)
            }
        }

        // Set click listener for the image button
        binding.imageBtn.setOnClickListener {
            // Open a dialog or menu to choose between gallery and camera
            showImageSourceDialog()
        }

        // Set checked change listener for the radio group
        binding.radioGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_already_read) {
                binding.readDetailsLayout.visibility = View.VISIBLE
            } else {
                binding.readDetailsLayout.visibility = View.GONE
            }
        }

        return binding.root
    }

    // Show dialog to choose between gallery and camera
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

    // Check camera permission and take picture if granted
    private fun checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            dispatchTakePictureIntent()
        }
    }

    // Dispatch an intent to take a picture
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
            imageUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    // Create an image file
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

    // Clean up binding when view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Log lifecycle events for debugging
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
        // Start any foreground services or register receivers
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        // Resume any paused UI updates or threads
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        // Pause any ongoing operations or UI updates
        // Save UI state changes to ViewModel
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
        // Stop any foreground services or unregister receivers
        // Release resources that are not needed while the activity is stopped
    }

    companion object {
        private const val TAG = "AddItemFragment"
    }
}


