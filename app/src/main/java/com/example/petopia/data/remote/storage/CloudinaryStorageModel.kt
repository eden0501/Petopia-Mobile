package com.example.petopia.data.remote.storage

import android.content.Context
import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy
import java.io.File

class CloudinaryStorageModel {

    companion object {
        private var isInitialized = false
        fun initCloudinary(context: Context) {
            if (isInitialized) return
            val config = mapOf(
                "cloud_name" to "dptpnlxtw",
                "api_key" to "425322147648144",
                "api_secret" to "hOejfOMOK0zX8E9ihJjPtNG0n9Y"
            )
            MediaManager.init(context.applicationContext, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
                .maxConcurrentRequests(3)
                .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                .build()
            isInitialized = true
        }
    }

    fun uploadImage(context: Context, image: Bitmap, path: String, completion: (String?) -> Unit) {
        initCloudinary(context)
        val file = bitmapToFile(image, context)

        MediaManager.get().upload(file.path)
            .option("public_id", path)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    completion(url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    completion(null)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun bitmapToFile(image: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            image.compress(Bitmap.CompressFormat.JPEG, 100, it)
            it.flush()
        }
        return file
    }
}
