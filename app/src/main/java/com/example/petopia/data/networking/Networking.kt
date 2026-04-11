package com.example.petopia.data.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Networking {
    private val dogRetrofit = Retrofit.Builder()
        .baseUrl("https://dogapi.dog/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val catRetrofit = Retrofit.Builder()
        .baseUrl("https://catfact.ninja/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val dogService: DogFactService = dogRetrofit.create(DogFactService::class.java)
    val catService: CatFactService = catRetrofit.create(CatFactService::class.java)
}
