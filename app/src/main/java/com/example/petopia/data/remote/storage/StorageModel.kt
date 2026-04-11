package com.example.petopia.data.remote.storage

import android.content.Context
import android.graphics.Bitmap

class StorageModel {
    private val cloudinaryStorage = CloudinaryStorageModel()

    fun uploadImage(context: Context, image: Bitmap, path: String, completion: (String?) -> Unit) {
        cloudinaryStorage.uploadImage(context, image, path, completion)
    }
}
