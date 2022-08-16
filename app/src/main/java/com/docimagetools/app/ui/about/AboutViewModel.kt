package com.docimagetools.app.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Image Size Reducer helps you to quickly and securely resize your image documents to " +
                 "a specified size or specified resolution as per the requirements without " +
                "even having an internet connection. \n\nThe sole purpose of this app is to " +
                "help users quickly resize documents without worrying " +
                "about their documents. Currently, we are completely offline and ad-free. " 
    }
    val text: LiveData<String> = _text
}