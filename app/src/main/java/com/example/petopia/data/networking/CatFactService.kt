package com.example.petopia.data.networking

import com.example.petopia.data.model.CatFactResponse
import retrofit2.http.GET

interface CatFactService {
    @GET("fact")
    suspend fun getCatFact(): CatFactResponse
}
