
package com.example.miniproject2

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

import com.example.miniproject2.data.LoginRequest
import com.example.miniproject2.data.LoginResponse
import com.example.miniproject2.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToSignup: TextView
    private lateinit var progressBar: ProgressBar

    private val BASE_URL = "http://192.168.1.41:8000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        etEmail = findViewById(R.id.et_login_email)
        etPassword = findViewById(R.id.et_login_password)
        btnLogin = findViewById(R.id.button)
        tvGoToSignup = findViewById(R.id.textView6)
        progressBar = findViewById(R.id.progress_bar_login)

        btnLogin.setOnClickListener {
            performLogin()
        }

        tvGoToSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        val retrofit = Retrofit.Builder() //to send HTTP requests to your server
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())  //to convert between JSON and kotlin objects
            .build() //creates the object
        val apiService = retrofit.create(ApiService::class.java)

        // Send the email in the 'username' field as required by the backend
        val loginRequest = LoginRequest(username = email, password = password)

        //login:
        apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                    Log.d("LoginActivity", "Access Token: ${loginResponse?.access}")

                    // Navigate to HomeActivity on success
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e("LoginActivity", "Network call failed: ${t.message}")
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}