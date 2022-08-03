package com.docimagetools.app.ui.image_size_reducer

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import java.io.File

class ImageSizeReducerViewModel : ViewModel() {

    suspend fun compress(context: Context, file: File): File {
        val compressedImageFile = Compressor.compress(context, file) {
            resolution(1280, 720)
            quality(80)
            format(Bitmap.CompressFormat.WEBP)
            size(1024) // 1 KB
        }
        return compressedImageFile
//        Log.i("","After Compression")
    }
}