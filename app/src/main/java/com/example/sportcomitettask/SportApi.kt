package com.example.sportcomitettask

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SportApi {
    @GET("/api/sports")
    suspend fun getSports(): List<String>

    @GET("/api/search")
    suspend fun searchSections(
        @Query("city") city: String,
        @Query("sport") sport: String
    ): List<Section>
}

object RetrofitClient {
    // ВАЖНО: 10.0.2.2 - это адрес localhost компьютера для Эмулятора Android.
    // Если запускаешь на реальном телефоне, здесь должен быть IP компьютера (например 192.168.1.5)
    private const val BASE_URL = "http://192.168.1.190:5000/"

    val api: SportApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SportApi::class.java)
    }
}
