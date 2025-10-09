package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.SearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {
    @GET("/search?entity=song")
    fun search(
        @Query(value = "term", encoded = true) text: String
    ): Call<SearchResponseDto>
}