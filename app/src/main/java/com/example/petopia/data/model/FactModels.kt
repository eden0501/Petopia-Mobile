package com.example.petopia.data.model

import com.google.gson.annotations.SerializedName

data class DogFactResponse(
    @SerializedName("data") val data: List<DogFactData>?
)

data class DogFactData(
    @SerializedName("attributes") val attributes: DogFactAttributes?
)

data class DogFactAttributes(
    @SerializedName("body") val body: String?
)

data class CatFactResponse(
    @SerializedName("fact") val fact: String?,
    @SerializedName("length") val length: Int?
)
