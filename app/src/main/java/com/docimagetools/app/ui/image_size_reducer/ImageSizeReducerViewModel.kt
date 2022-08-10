package com.docimagetools.app.ui.image_size_reducer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import java.io.File

class ImageSizeReducerViewModel : ViewModel() {
    var originalImage: File? = null;

    suspend fun compress(context: Context, file: File): File {
        val compressedImageFile = Compressor.compress(context, file) {
//            resolution(1280, 720)
//            quality(80)
            format(Bitmap.CompressFormat.JPEG)
            size(100* 1024, maxIteration = 10) // 1 KB
        }
        Log.i("", "File Size: ${compressedImageFile.length()}")
        return compressedImageFile

    }
}