package com.docimagetools.app.ui.image_resolution_changer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import java.io.File
import java.net.URI

class ImageResolutionChangerViewModel : ViewModel() {

    var originalImage: File? = null;
    var selectedHeight: String? = null;
    var selectedWidth: String? = null;

    protected val getOriginalSize: Long
        protected get() = originalImage!!.length()

    protected val getSelectedWidth: Int
        protected get() = selectedWidth!!.toInt()

    protected val getSelectedHeight: Int
        protected get() = selectedHeight!!.toInt()


    suspend fun resize(context: Context, file: File): File {

        getImageSize(file.toURI())
        val compressedImageFile = Compressor.compress(context, file) {

            resolution(getSelectedWidth, getSelectedHeight)
            format(Bitmap.CompressFormat.JPEG)
        }
        Log.i("Compressor", "Original   File Size: ${file.length()}")
        Log.i("Compressor", "Compressed File Size: ${compressedImageFile.length()}")
        Log.i("Compressor", "Original   File Resolution: ${getImageSize(file.toURI()).joinToString()}")
        Log.i("Compressor", "Compressed File Resolution: ${getImageSize(compressedImageFile.toURI()).joinToString()}")


        return compressedImageFile

    }

    private fun getImageSize(uri: URI) : Array<Int?> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.getPath()).getAbsolutePath(), options)
        return arrayOf<Int?>(options.outWidth, options.outHeight)
    }
}