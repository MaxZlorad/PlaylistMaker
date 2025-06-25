package com.practicum.playlistmaker

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ItunesApiService {
    @GET("/search?entity=song")
    fun search(
        @Query(value = "term", encoded = true) text: String
    ): Call<SearchResponse>
}