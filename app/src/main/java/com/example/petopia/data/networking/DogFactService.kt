package com.example.petopia.data.networking

import com.example.petopia.data.model.DogFactResponse
import retrofit2.http.GET

interface DogFactService {
    @GET("api/v2/facts")
    suspend fun getDogFact(): DogFactResponse
}
