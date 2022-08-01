package com.docimagetools.app.ui.image_size_reducer

import android.content.Context
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import java.io.File

class ImageSizeReducerViewModel : ViewModel() {

    suspend fun compress(context: Context, file: File) {
        val compressedImageFile = Compressor.compress(context, file)
    }
}