package com.docimagetools.app.ui.image_size_reducer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import java.io.File
import java.net.URI


class ImageSizeReducerViewModel : ViewModel() {
    var originalImage: File? = null;
    var selectedSize: String? = null;
    var selectedUnit: Int? = null;

    var imageHeight: Int? = null;
    var imageWidth: Int? = null;

    protected val getOriginalSize: Long
        protected get() = originalImage!!.length()

    protected val getTargetSize: Int
        protected get() {
            if(selectedUnit == 0 ) {
                return selectedSize!!.toInt()*1024
            } else if(selectedUnit == 1) {
                return selectedSize!!.toInt()*1024*1024
            }
            return 0
        }

    suspend fun compress(context: Context, file: File): File {
        if (selectedSize!!.toInt() < 10 && selectedUnit  == 0) {
            throw RuntimeException("Size must be between 10 KB to 5 MB")
        } else if(selectedSize!!.toInt() > 5 && selectedUnit == 1) {
            throw RuntimeException("Size must be between 10 KB to 5 MB")
        } else if (selectedSize!!.toInt() <= 0) {
            throw RuntimeException("Size must be between 10 KB to 5 MB")
        } else {
            getImageSize(file.toURI())
            val compressedImageFile = Compressor.compress(context, file) {

                if(getOriginalSize.toInt() > getTargetSize) {
                    if (getOriginalSize/ getTargetSize >= 2) {
                        resolution(360, (360.0/(imageWidth!!.toFloat()/ imageHeight!!.toFloat())).toInt())
                    }

                    quality(80)
                    format(Bitmap.CompressFormat.JPEG)
                    size(getTargetSize.toLong(), maxIteration = 10)
                 }
            }
            Log.i("Compressor", "Original   File Size: ${file.length()}")
            Log.i("Compressor", "Compressed File Size: ${compressedImageFile.length()}")
            return compressedImageFile
        }
    }

    private fun getImageSize(uri: URI) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.getPath()).getAbsolutePath(), options)
        imageHeight = options.outHeight
        imageWidth = options.outWidth
    }
}