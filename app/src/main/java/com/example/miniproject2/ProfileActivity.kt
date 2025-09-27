package com.example.miniproject2

import android.content.Intent
import android.os.Bundle
import android.widget.Button // You need to import the Button class
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    // 1. Declare the variable for the Button here
    private lateinit var addProductButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 2. Initialize the variable by finding the button by its ID
        addProductButton = findViewById(R.id.add_product_button)

        addProductButton.setOnClickListener {
            val intent = Intent(this, AddNewProductActivity::class.java)
            startActivity(intent)
        }
    }
}