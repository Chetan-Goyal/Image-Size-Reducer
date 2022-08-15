package com.docimagetools.app.ui.image_resolution_changer

import android.app.Activity
import android.content.Intent
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
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.R
import com.docimagetools.app.databinding.FragmentImageResolutionChangerBinding
import com.docimagetools.app.ui.image_size_reducer.ImageSizeReducerViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.lang.Exception

class ImageResolutionChangerFragment : Fragment() {

    private var _binding: FragmentImageResolutionChangerBinding? = null
    private val pickImage = 100

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
        val selectedHeight : EditText = root.findViewById(R.id.selectedHeight);

        selectedWidth.addTextChangedListener(
            onTextChanged = { s, start, before, count ->
                if(selectedWidth.tag == null) {
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
                            calculateNewHeight(width=width)
                        } catch (e: Exception) {
                            Log.i("Resolution Changer", e.message.toString());

                        }
                    }


                }
            }
        )


        selectedHeight.addTextChangedListener(
            onTextChanged = { s, start, before, count ->

                if (selectedHeight.tag == null) {
                    val height : String = s.toString()
                    imageResolutionChangerViewModel.selectedHeight = s.toString()

                    if(height.isEmpty()) {
                        imageResolutionChangerViewModel.selectedWidth = ""
                        imageResolutionChangerViewModel.selectedHeight = ""

                        selectedWidth.tag = "auto"
                        selectedWidth.setText(imageResolutionChangerViewModel.selectedWidth)
                        selectedWidth.tag = null

                    } else {
                        try {
                            calculateNewWidth(height=height)
                        } catch(e: Exception) {
                            Log.i("Resolution Changer", e.message.toString());

                        }
                    }
                }
                }

        )

        root.findViewById<Button>(R.id.processImage)?.setOnClickListener {
            if (imageResolutionChangerViewModel.originalImage == null ) {
                Toast.makeText(root.context, "Please select your Image first!", Toast.LENGTH_SHORT).show()
            } else if(imageResolutionChangerViewModel.selectedWidth == null) {
                Toast.makeText(root.context, "Please select width for output image!", Toast.LENGTH_SHORT).show()
            } else if(imageResolutionChangerViewModel.selectedHeight == null) {
                Toast.makeText(root.context, "Please select height for output file!", Toast.LENGTH_SHORT).show()
            } else {
                uiScope.launch(Dispatchers.IO) {
                    //asyncOperation
                    withContext(Dispatchers.Main) {
                        resize()
                    }
                }
            }
        }

        return root
    }

    private suspend fun resize() {
        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)

        try {
            binding.resizedImage.setImageURI(null)
            val compressedImage = imageResolutionChangerViewModel.resize(binding.root.context, imageResolutionChangerViewModel.originalImage!!)
            binding.resizedImage.setImageURI(compressedImage.toUri())
        } catch (exception: RuntimeException) {
            Toast.makeText(this.context, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateNewWidth(height: String) {
        val imageResolutionChangerViewModel = ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)
        val newResolution = imageResolutionChangerViewModel.getResolution(height=height)

        imageResolutionChangerViewModel.selectedWidth = if(newResolution == null) "" else newResolution[0].toDouble().toInt().toString()

        val selectedWidth: EditText = binding.root.findViewById(R.id.selectedWidth);
        selectedWidth.tag = "auto"
        selectedWidth.setText(imageResolutionChangerViewModel.selectedWidth)
        selectedWidth.tag = null
    }

    private fun calculateNewHeight(width: String) {
        val imageResolutionChangerViewModel = ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)
        val newResolution = imageResolutionChangerViewModel.getResolution(width=width)

        imageResolutionChangerViewModel.selectedHeight = if(newResolution == null) "" else newResolution[1].toDouble().toInt().toString()

        val selectedHeight: EditText = binding.root.findViewById(R.id.selectedHeight);
        selectedHeight.tag = "auto"
        selectedHeight.setText(imageResolutionChangerViewModel.selectedHeight)
        selectedHeight.tag = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.i("", "Data is null")
                return
            }
            try {
                val actualImage: File? = data.data?.let {
                    FileUtil.from(this.requireContext(), it)
                }
                if (actualImage == null) {
                    Log.i("", "Image is null")
                } else {
                    val imageResolutionChangerViewModel =
                        ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)
                    imageResolutionChangerViewModel.originalImage = actualImage
                    binding.selectedImageView.setImageURI(actualImage.toUri())

                    if (imageResolutionChangerViewModel.selectedWidth != null && imageResolutionChangerViewModel.selectedWidth!!.isNotEmpty()) {
                        calculateNewHeight(width = imageResolutionChangerViewModel.selectedWidth!!)
                    } else if(imageResolutionChangerViewModel.selectedHeight != null && imageResolutionChangerViewModel.selectedHeight!!.isNotEmpty()) {
                        calculateNewWidth(height = imageResolutionChangerViewModel.selectedHeight!!)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}