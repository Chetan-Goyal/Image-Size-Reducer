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
    var compressedImage: File? = null;
    var selectedSize: String? = null;
    var selectedUnit: Int? = null;

    var imageHeight: Int? = null;
    var imageWidth: Int? = null;


    val getOriginalSize: Long
        get() = originalImage!!.length()

    val getTargetSize: Int
        get() {
            if (selectedUnit == 0) {
                return selectedSize!!.toInt() * 1024
            } else if (selectedUnit == 1) {
                return selectedSize!!.toInt() * 1024 * 1024
            }
            return 0
        }

    suspend fun compress(context: Context, file: File): File {
        if (selectedSize!!.toInt() < 10 && selectedUnit == 0) {
            throw RuntimeException("Size must be between 10 KB to 5 MB")
        } else if (selectedSize!!.toInt() > 5 && selectedUnit == 1) {
            throw RuntimeException("Size must be between 10 KB to 5 MB")
        } else if (selectedSize!!.toInt() <= 0) {
            throw RuntimeException("Size must be between 10 KB to 5 MB")
        } else {
            getImageSize(file.toURI())
            val compressedImageFile = Compressor.compress(context, file) {

                if (getOriginalSize.toInt() > getTargetSize) {
                    if (getTargetSize < 20 * 1024) {
                        resolution(
                            50,
                            (50.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getTargetSize < 30 * 1024) {
                        resolution(
                            100,
                            (100.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getTargetSize < 35 * 1024) {
                        resolution(
                            150,
                            (150.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getTargetSize < 40 * 1024) {
                        resolution(
                            200,
                            (200.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getTargetSize < 60 * 1024) {
                        resolution(
                            250,
                            (250.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getTargetSize < 70 * 1024) {
                        resolution(
                            300,
                            (300.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getTargetSize <= 100 * 1024) {
                        resolution(
                            400,
                            (400.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    } else if (getOriginalSize / getTargetSize >= 2) {
                        resolution(
                            500,
                            (500.0 / (imageWidth!!.toFloat() / imageHeight!!.toFloat())).toInt()
                        )
                    }

                    quality(90)
                    format(Bitmap.CompressFormat.JPEG)
                    size(getTargetSize.toLong(), maxIteration = 30)
                }
            }
            Log.i("Compressor", "Original   File Size: ${file.length()}")
            Log.i("Compressor", "Compressed File Size: ${compressedImageFile.length()}")
            Log.i(
                "Compressor",
                "Original   File Resolution: ${getImageSize(file.toURI()).joinToString()}"
            )
            Log.i(
                "Compressor",
                "Compressed File Resolution: ${getImageSize(compressedImageFile.toURI()).joinToString()}"
            )

            compressedImage = compressedImageFile
            return compressedImageFile
        }
    }

    private fun getImageSize(uri: URI): Array<Int?> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.getPath()).getAbsolutePath(), options)
        imageHeight = options.outHeight
        imageWidth = options.outWidth
        return arrayOf<Int?>(imageWidth, imageHeight)
    }
}