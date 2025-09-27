package com.example.miniproject2.data // Change to your package name

import com.google.gson.annotations.SerializedName

// JSON object send to the server
data class SignUpRequest(
    @SerializedName("full_name")
    val fullName: String,
    val email: String,
    val password: String,
    val password2: String
)

// JSON object you  RECEIVE from the server
data class SignUpResponse(
    val id: Int,
    val username: String,
    val email: String
)

data class LoginRequest(
    val username: String, // what we send to login
    val password: String
)

// This class represents the JSON you RECEIVE on successful login
data class LoginResponse(
    val refresh: String,
    val access: String
)