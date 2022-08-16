package com.docimagetools.app.ui.image_size_reducer

import FileUtil
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.R
import com.docimagetools.app.databinding.FragmentImageSizeReducerBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.OutputStream


class ImageSizeReducerFragment : Fragment() {

    private var _binding: FragmentImageSizeReducerBinding? = null
    private val TAG = "Size Reducer"
    private val pickImage = 100
    private val saveCode = 200

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageSizeReducerBinding.inflate(inflater, container, false)
        val imageSizeReducerViewModel =
            ViewModelProvider(this).get(ImageSizeReducerViewModel::class.java)

//        val root: View = binding.root

        (activity as AppCompatActivity).supportActionBar?.title = "Image Size Reducer"

        val saveButton: Button = binding.root.findViewById<Button>(R.id.saveImage)
        saveButton.isVisible = false

        val button: Button = binding.selectImage
        button.setOnClickListener {
            val pickIntent = Intent()
            pickIntent.type = "image/*"
            pickIntent.action = Intent.ACTION_GET_CONTENT

            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val pickTitle = "Select or take a new Picture"

            val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
            )
            startActivityForResult(chooserIntent, pickImage)
        }

        val imageSizeUnits = resources.getStringArray(R.array.imageSizeUnits)

        // access the spinner
        val spinner = binding.root.findViewById<Spinner>(R.id.sizeDropDown)
        spinner.setSelection(0, true)
        spinner.prompt = "KB"

        if (spinner != null) {
            val adapter = ArrayAdapter(
                this.requireContext(),
                android.R.layout.simple_spinner_item, imageSizeUnits
            )
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View, position: Int, id: Long
                ) {
                    imageSizeReducerViewModel.selectedUnit = position
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        binding.root.findViewById<EditText>(R.id.editText)?.addTextChangedListener(
            onTextChanged = { s, _, _, _ ->
                imageSizeReducerViewModel.selectedSize = s.toString()
            }
        )

        binding.root.findViewById<Button>(R.id.processImage)?.setOnClickListener {
            if (imageSizeReducerViewModel.originalImage == null) {
                Toast.makeText(binding.root.context, "Please select your Image first!", Toast.LENGTH_SHORT)
                    .show()
            } else if (imageSizeReducerViewModel.selectedSize == null) {
                Toast.makeText(
                    binding.root.context,
                    "Please select size for output image!",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (imageSizeReducerViewModel.selectedUnit == null) {
                Toast.makeText(
                    binding.root.context,
                    "Please select unit for output file!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                uiScope.launch(Dispatchers.IO) {
                    //asyncOperation
                    withContext(Dispatchers.Main) {
                        compress()
                    }
                }
            }
        }

        saveButton.setOnClickListener {

            if (imageSizeReducerViewModel.compressedImage != null) {
                showSharingDialogAsKotlinWithURL(imageSizeReducerViewModel.compressedImage!!)
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Please compress your image first!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageSizeReducerViewModel =
            ViewModelProvider(this).get(ImageSizeReducerViewModel::class.java)
        if (requestCode == pickImage && resultCode == RESULT_OK) {
            if (data == null || data.data == null) {
                Log.i(TAG, "Data is null")
                return
            }
            try {
                val actualImage: File = data.data!!.let {
                    FileUtil.from(this.requireContext(), it)
                }
                imageSizeReducerViewModel.originalImage = actualImage
                binding.selectedImageView.setImageURI(actualImage.toUri())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == saveCode && resultCode == RESULT_OK) {
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
                        output.write(imageSizeReducerViewModel.compressedImage!!.readBytes())
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

    private suspend fun compress() {
        val imageSizeReducerViewModel =
            ViewModelProvider(this).get(ImageSizeReducerViewModel::class.java)

        try {
            binding.compressedImage.setImageURI(null)
            val compressedImage = imageSizeReducerViewModel.compress(
                binding.root.context,
                imageSizeReducerViewModel.originalImage!!
            )
            binding.compressedImage.setImageURI(compressedImage.toUri())
            val saveButton: Button = binding.root.findViewById<Button>(R.id.saveImage)
            saveButton.isVisible = true
        } catch (exception: RuntimeException) {
            Toast.makeText(this.context, exception.message, Toast.LENGTH_SHORT).show()
        }
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
        sendIntent.putExtra(Intent.EXTRA_TITLE, "compressed.jpg")
        startActivityForResult(sendIntent, saveCode)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}