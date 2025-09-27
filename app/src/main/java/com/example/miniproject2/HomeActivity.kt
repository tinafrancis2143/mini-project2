package com.example.miniproject2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.miniproject2.data.IngredientDatabase

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val profileImage:ImageView = findViewById(R.id.profile)

        // Set an OnClickListener to start the ProfileActivity
        profileImage.setOnClickListener {
            Log.d("HomeActivity", "Profile image clicked! Starting ProfileActivity.")

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Find the RecyclerView on your home page layout..to display a list of items
        val trendingRecyclerView: RecyclerView = findViewById(R.id.trendingRecyclerView)

        // Create an instance of your custom adapter with the data source...bridge b/w data and recyclerview
        val adapter = TrendingIngredientAdapter(TrendingIngredientData.ingredients)

        // Set the layout manager for horizontal scrolling
        trendingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Connect the adapter to the RecyclerView
        trendingRecyclerView.adapter = adapter

        val myTextView2 = findViewById<TextView>(R.id.button4)
        myTextView2.setOnClickListener {
            val intent = Intent(this, ManualEntryActivity::class.java)
            startActivity(intent)
        }

        val scannerButton = findViewById<Button>(R.id.button3)
        scannerButton.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }
    }
}