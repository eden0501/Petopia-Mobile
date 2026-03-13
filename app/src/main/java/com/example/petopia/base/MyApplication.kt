package com.example.petopia.base

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.ktx.Firebase

class MyApplication: Application() {

    companion object Globals {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = Firebase.appCheck
        firebaseAppCheck.installAppCheckProviderFactory(
            if (com.example.petopia.BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )
    }
}