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
import java.lang.Exception
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

    protected  val getOriginalAspectRatio: Double
        protected  get() {
            val resolution = getImageResolution(originalImage!!.toURI())
            Log.i("Resolution Changer", "Resolution: ${resolution[0]}x${resolution[1]}")
            Log.i("Resolution Changer", "Aspect Ratio: ${(resolution[0]!!).toDouble()/resolution[1]!!.toDouble()}")
            return (resolution[0]!!).toDouble()/resolution[1]!!.toDouble()

        }


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

    fun getResolution(width: String?= null, height: String? = null) : Array<String>? {

        if(width == null && height == null) {
            throw RuntimeException("Both Width and Height can't be null")

        } else if(originalImage == null) {
            Log.i("Resolution Changer", "Original Image is Null")
            return null;
        } else {

                if (width != null) {
                    val height = (width.toDouble() / getOriginalAspectRatio).toString()
                    Log.i("Resolution Changer", "${width}x${height}")
                    return arrayOf(width, height)
                } else if(height != null ){
                    val width = (height.toDouble() * getOriginalAspectRatio).toString()
                    Log.i("Resolution Changer", "${width}x${height}")
                    return arrayOf(width, height)
                } else {
                    Log.i("Resolution Changer", "This shouldn't happen")
                }

        }
        return null;
    }

    private fun getImageResolution(uri: URI) : Array<Int?> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.getPath()).getAbsolutePath(), options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        return arrayOf<Int?>(imageWidth, imageHeight)
    }

    private fun getImageSize(uri: URI) : Array<Int?> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.getPath()).getAbsolutePath(), options)
        return arrayOf<Int?>(options.outWidth, options.outHeight)
    }
}