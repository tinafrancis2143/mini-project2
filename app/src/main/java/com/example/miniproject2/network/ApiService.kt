package com.example.miniproject2.network// Change to your package name

import com.example.miniproject2.data.SignUpRequest
import com.example.miniproject2.data.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

import com.example.miniproject2.data.LoginRequest
import com.example.miniproject2.data.LoginResponse



interface ApiService {
    @POST("api/signup/")
    fun signup(@Body request: SignUpRequest): Call<SignUpResponse>

    // Add this new function for login
    @POST("api/login/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}