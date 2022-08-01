package com.docimagetools.app.ui.image_resolution_changer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageResolutionChangerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Image Resolution Changer Fragment"
    }
    val text: LiveData<String> = _text
}