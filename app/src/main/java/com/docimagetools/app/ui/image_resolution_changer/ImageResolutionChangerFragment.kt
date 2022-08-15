package com.docimagetools.app.ui.image_resolution_changer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.R
import com.docimagetools.app.databinding.FragmentImageResolutionChangerBinding
import com.docimagetools.app.ui.image_size_reducer.ImageSizeReducerViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception

class ImageResolutionChangerFragment : Fragment() {

    private var _binding: FragmentImageResolutionChangerBinding? = null
    private val TAG = "Resolution Changer"
    private val pickImage = 100
    private val saveCode = 200

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)

        _binding = FragmentImageResolutionChangerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val saveButton: Button = binding.root.findViewById<Button>(R.id.saveImage)
        saveButton.isVisible = false

        (activity as AppCompatActivity).supportActionBar?.title = "Image Resolution Changer"

        val button: Button = binding.selectImage
        button.setOnClickListener {
            val pickIntent = Intent()
            pickIntent.type = "image/*"
            pickIntent.action = Intent.ACTION_GET_CONTENT

            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val pickTitle = "Select or take a new Picture" // Or get from strings.xml

            val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
            )
            startActivityForResult(chooserIntent, pickImage)
        }

        val selectedWidth: EditText = root.findViewById(R.id.selectedWidth);
        val selectedHeight: EditText = root.findViewById(R.id.selectedHeight);

        selectedWidth.addTextChangedListener(
            onTextChanged = { s, start, before, count ->
                if (selectedWidth.tag == null) {
                    val width: String = s.toString()
                    imageResolutionChangerViewModel.selectedWidth = width

                    if (width.isEmpty()) {
                        imageResolutionChangerViewModel.selectedWidth = ""
                        imageResolutionChangerViewModel.selectedHeight = ""

                        selectedHeight.tag = "auto"
                        selectedHeight.setText(imageResolutionChangerViewModel.selectedHeight)
                        selectedHeight.tag = null

                    } else {
                        try {
                            calculateNewHeight(width = width)
                        } catch (e: Exception) {
                            Log.i(TAG, e.message.toString());

                        }
                    }


                }
            }
        )


        selectedHeight.addTextChangedListener(
            onTextChanged = { s, start, before, count ->

                if (selectedHeight.tag == null) {
                    val height: String = s.toString()
                    imageResolutionChangerViewModel.selectedHeight = s.toString()

                    if (height.isEmpty()) {
                        imageResolutionChangerViewModel.selectedWidth = ""
                        imageResolutionChangerViewModel.selectedHeight = ""

                        selectedWidth.tag = "auto"
                        selectedWidth.setText(imageResolutionChangerViewModel.selectedWidth)
                        selectedWidth.tag = null

                    } else {
                        try {
                            calculateNewWidth(height = height)
                        } catch (e: Exception) {
                            Log.i(TAG, e.message.toString());

                        }
                    }
                }
            }

        )

        root.findViewById<Button>(R.id.processImage)?.setOnClickListener {
            if (imageResolutionChangerViewModel.originalImage == null) {
                Toast.makeText(root.context, "Please select your Image first!", Toast.LENGTH_SHORT)
                    .show()
            } else if (imageResolutionChangerViewModel.selectedWidth == null) {
                Toast.makeText(
                    root.context,
                    "Please select width for output image!",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (imageResolutionChangerViewModel.selectedHeight == null) {
                Toast.makeText(
                    root.context,
                    "Please select height for output file!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                uiScope.launch(Dispatchers.IO) {
                    //asyncOperation
                    withContext(Dispatchers.Main) {
                        resize()
                    }
                }
            }
        }

        root.findViewById<Button>(R.id.saveImage)?.setOnClickListener {

            if (imageResolutionChangerViewModel.resizedImage != null) {
                showSharingDialogAsKotlinWithURL(imageResolutionChangerViewModel.resizedImage!!)
            } else {
                Toast.makeText(root.context, "Please resize your image first!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return root
    }

    private suspend fun resize() {
        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)

        try {
            binding.resizedImage.setImageURI(null)
            val compressedImage = imageResolutionChangerViewModel.resize(
                binding.root.context,
                imageResolutionChangerViewModel.originalImage!!
            )
            binding.resizedImage.setImageURI(compressedImage.toUri())

            val saveButton: Button = binding.root.findViewById<Button>(R.id.saveImage)
            saveButton.isVisible = true
        } catch (exception: RuntimeException) {
            Toast.makeText(this.context, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateNewWidth(height: String) {
        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)
        val newResolution = imageResolutionChangerViewModel.getResolution(height = height)

        imageResolutionChangerViewModel.selectedWidth =
            if (newResolution == null) "" else newResolution[0].toDouble().toInt().toString()

        val selectedWidth: EditText = binding.root.findViewById(R.id.selectedWidth);
        selectedWidth.tag = "auto"
        selectedWidth.setText(imageResolutionChangerViewModel.selectedWidth)
        selectedWidth.tag = null
    }

    private fun calculateNewHeight(width: String) {
        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)
        val newResolution = imageResolutionChangerViewModel.getResolution(width = width)

        imageResolutionChangerViewModel.selectedHeight =
            if (newResolution == null) "" else newResolution[1].toDouble().toInt().toString()

        val selectedHeight: EditText = binding.root.findViewById(R.id.selectedHeight);
        selectedHeight.tag = "auto"
        selectedHeight.setText(imageResolutionChangerViewModel.selectedHeight)
        selectedHeight.tag = null
    }

    private fun showSharingDialogAsKotlinWithURL(file: File) {

        val imageUri = FileProvider.getUriForFile(
            binding.root.context,
            "com.docimagetools.app.provider",
            file
        )
        Log.i(TAG, file.absolutePath)
        Log.i(TAG, imageUri.toString())
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_CREATE_DOCUMENT
        sendIntent.type = "image/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        sendIntent.putExtra(Intent.EXTRA_TITLE, "resized_image.jpg")
        startActivityForResult(sendIntent, saveCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.i(TAG, "Data is null")
                return
            }
            try {
                val actualImage: File? = data.data?.let {
                    FileUtil.from(this.requireContext(), it)
                }
                if (actualImage == null) {
                    Log.i(TAG, "Image is null")
                } else {
                    imageResolutionChangerViewModel.originalImage = actualImage
                    binding.selectedImageView.setImageURI(actualImage.toUri())

                    if (imageResolutionChangerViewModel.selectedWidth != null && imageResolutionChangerViewModel.selectedWidth!!.isNotEmpty()) {
                        calculateNewHeight(width = imageResolutionChangerViewModel.selectedWidth!!)
                    } else if (imageResolutionChangerViewModel.selectedHeight != null && imageResolutionChangerViewModel.selectedHeight!!.isNotEmpty()) {
                        calculateNewWidth(height = imageResolutionChangerViewModel.selectedHeight!!)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == saveCode && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                Log.i(TAG, "Data is null")
                return
            }

            val uri: Uri? = data.data

            if (uri != null) {
                try {
                    val output: OutputStream? =
                        binding.root.context.contentResolver.openOutputStream(uri)

                    if (output != null) {
                        output.write(imageResolutionChangerViewModel.resizedImage!!.readBytes())
                        output.flush()
                        output.close()
                    } else {
                        Log.i(TAG, "Output Stream is null")
                    }

                } catch (e: IOException) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.i(TAG, "File URI is null")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}