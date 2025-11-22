package com.example.comfyuiremote

import android.app.Application
import com.example.comfyuiremote.data.api.ComfyApiService
import com.example.comfyuiremote.data.repository.ComfyRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ComfyRemoteApp : Application() {

    lateinit var repository: ComfyRepository

    override fun onCreate() {
        super.onCreate()
        
        // TODO: Move base URL to config or settings
        // 10.0.2.2 is localhost for Android Emulator
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ComfyApiService::class.java)
        repository = ComfyRepository(apiService)
    }
}
