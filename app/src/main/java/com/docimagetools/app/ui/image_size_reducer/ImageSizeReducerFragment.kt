package com.docimagetools.app.ui.image_size_reducer

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.databinding.FragmentImageSizeReducerBinding
import kotlinx.coroutines.*
import java.io.File


class ImageSizeReducerFragment : Fragment() {

    private var _binding: FragmentImageSizeReducerBinding? = null
    private val pickImage = 100

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageSizeReducerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val button: Button = binding.selectImage
        button.setOnClickListener {
//            val image_resolution_changer = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
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

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == pickImage) {
            binding.selectedImageView.setImageURI(data?.data)
            uiScope.launch(Dispatchers.IO){
                //asyncOperation
                withContext(Dispatchers.Main){
                    compress(data?.data?.toFile()?.absoluteFile)
                }

            }
        }
    }

    suspend fun compress(data: File?) {
        if (data != null) {
//            val dir = Environment.getStorageDirectory();
//            val yourFile =
//                File(dir, data.path)
//            Log.i("", "${yourFile.absoluteFile}")
            val imageSizeReducerViewModel =
                ViewModelProvider(this).get(ImageSizeReducerViewModel::class.java)
            imageSizeReducerViewModel.compress(binding.root.context, File(data.path))
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