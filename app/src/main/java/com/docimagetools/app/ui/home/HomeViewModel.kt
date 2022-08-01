package com.docimagetools.app.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import java.io.File

class HomeViewModel : ViewModel() {

    suspend fun compress(context: Context, file: File) {
        val compressedImageFile = Compressor.compress(context, file)
    }
}