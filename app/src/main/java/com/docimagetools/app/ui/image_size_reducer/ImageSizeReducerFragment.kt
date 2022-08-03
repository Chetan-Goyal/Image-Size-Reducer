package com.docimagetools.app.ui.image_size_reducer

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentResolverCompat.query
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.databinding.FragmentImageSizeReducerBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException


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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == pickImage) {
//            binding.selectedImageView.setImageURI(data?.data)
//            Log.i("", "Initial");
//
//
//            Log.i("",data!!.data. toString());
//            Log.i("", "Initial");
//
//
//            Log.i("",data!!.data?.toFile()?.path!!);
//            Log.i("", "Initial");
//            data!!.data?.toFile();
//            Log.i("", "Initial");
//
////            Log.i("",File(data!!.data?.path));
//
//            uiScope.launch(Dispatchers.IO){
//                //asyncOperation
//                withContext(Dispatchers.Main){
//
//
//                    compress(data!!.data?.toFile());
//                }
//
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == RESULT_OK) {
            if (data == null) {
//                showError("Failed to open picture!")
                return
            }
            try {
                 val actualImage: File? = data.data?.let {
                    FileUtil.from(this.requireContext(), it)?.also {

            //                    actualImageView.setImageBitmap(loadBitmap(it))
            //                    actualSizeTextView.text = String.format("Size : %s", getReadableFileSize(it.length()))
            //                    clearImage()
                    }
                }
                if (actualImage == null) {
                    Log.i("", "Image is null")
                } else {
                    uiScope.launch(Dispatchers.IO){
                //asyncOperation
                withContext(Dispatchers.Main){


                    compress(actualImage!!);
                }

            }

                }

            } catch (e: IOException) {
//                showError("Failed to read picture data!")
                e.printStackTrace()
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
            val compressedImage = imageSizeReducerViewModel.compress(binding.root.context, data)
            binding.selectedImageView.setImageURI(compressedImage.toUri())
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