package com.example.miniproject2 // Change to your package name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miniproject2.LoginActivity
import com.example.miniproject2.R
import com.example.miniproject2.data.SignUpRequest
import com.example.miniproject2.data.SignUpResponse
import com.example.miniproject2.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoginLink: TextView // For your login link

    private val BASE_URL = "http://10.147.218.84:8000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        etFullName = findViewById(R.id.et_full_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnSignup = findViewById(R.id.button2)
        progressBar = findViewById(R.id.progress_bar)
        tvLoginLink = findViewById(R.id.textView12) // Initialize your TextView

        // The main signup button still calls the network function
        btnSignup.setOnClickListener {
            performSignUp()
        }


        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performSignUp() {
        // Get the text that the user has typed
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val password2 = etConfirmPassword.text.toString()

        // Check if any fields are empty
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the passwords match
        if (password != password2) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Show a loading circle and disable the button
        progressBar.visibility = View.VISIBLE
        btnSignup.isEnabled = false

        // Set up Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // Create the request object with the user's data
        val signUpRequest = SignUpRequest(fullName, email, password, password2)

        // Make the asynchronous network call
        apiService.signup(signUpRequest).enqueue(object : Callback<SignUpResponse> {
            // This is called when the server responds
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                progressBar.visibility = View.GONE
                btnSignup.isEnabled = true

                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    Toast.makeText(this@SignUpActivity, "Account created successfully! User ID: ${signUpResponse?.id}", Toast.LENGTH_LONG).show()

                    // After successful signup, you can automatically go to the login screen
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Optional: finish this activity so the user can't go back to it
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@SignUpActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            // This is called if there is a network error (e.g., server is down)
            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnSignup.isEnabled = true
                Log.e("SignUpActivity", "Network call failed: ${t.message}")
                Toast.makeText(this@SignUpActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}