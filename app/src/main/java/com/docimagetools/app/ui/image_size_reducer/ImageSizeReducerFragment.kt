package com.docimagetools.app.ui.image_size_reducer

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.R
import com.docimagetools.app.databinding.FragmentImageSizeReducerBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException


class ImageSizeReducerFragment : Fragment() {

    private var _binding: FragmentImageSizeReducerBinding? = null
    private val pickImage = 100

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

        val root: View = binding.root

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

        val imageSizeUnits = resources.getStringArray(R.array.imageSizeUnits)

        // access the spinner
        val spinner = root.findViewById<Spinner>(R.id.sizeDropDown)
        spinner.setSelection(0, true);
        spinner.setPrompt("KB")

        if (spinner != null) {
            val adapter = ArrayAdapter<String>(
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
                    imageSizeReducerViewModel.selectedUnit = position;
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        root.findViewById<EditText>(R.id.editText)?.addTextChangedListener(
            onTextChanged = { s, start, before, count ->
                imageSizeReducerViewModel.selectedSize = s.toString()
            }
        )

        root.findViewById<Button>(R.id.processImage)?.setOnClickListener {
            if (imageSizeReducerViewModel.originalImage == null ) {
                Toast.makeText(root.context, "Please select your Image first!", Toast.LENGTH_SHORT).show()
            } else if(imageSizeReducerViewModel.selectedSize == null) {
                 Toast.makeText(root.context, "Please select size for output image!", Toast.LENGTH_SHORT).show()
            } else if(imageSizeReducerViewModel.selectedUnit == null) {
                Toast.makeText(root.context, "Please select unit for output file!", Toast.LENGTH_SHORT).show()
            } else {
                uiScope.launch(Dispatchers.IO) {
                    //asyncOperation
                    withContext(Dispatchers.Main) {
                        compress()
                    }
                }
            }
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == RESULT_OK) {
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
                    val imageSizeReducerViewModel =
                        ViewModelProvider(this).get(ImageSizeReducerViewModel::class.java)
                    imageSizeReducerViewModel.originalImage = actualImage
                    binding.selectedImageView.setImageURI(actualImage.toUri())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun compress() {
        val imageSizeReducerViewModel =
            ViewModelProvider(this).get(ImageSizeReducerViewModel::class.java)

        try {
            binding.compressedImage.setImageURI(null)
            val compressedImage = imageSizeReducerViewModel.compress(binding.root.context, imageSizeReducerViewModel.originalImage!!)
            binding.compressedImage.setImageURI(compressedImage.toUri())
        } catch (exception: RuntimeException) {
            Toast.makeText(this.context, exception.message, Toast.LENGTH_SHORT).show()
        }


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