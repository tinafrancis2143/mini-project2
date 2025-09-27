package com.example.miniproject2

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject2.adapter.ResultsAdapter
import com.example.miniproject2.data.Ingredient
import com.example.miniproject2.data.IngredientDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // 1. Get the data that was passed from ScannerActivity
        val selectedCategory = intent.getStringExtra("EXTRA_CATEGORY") ?: "Overall"
        val ingredientNames = intent.getStringArrayListExtra("EXTRA_INGREDIENTS") ?: arrayListOf()

        // Find the views from your layout
        val titleTextView: TextView = findViewById(R.id.tv_results_title)
        val categoryTextView: TextView = findViewById(R.id.tv_selected_category)
        val recyclerView: RecyclerView = findViewById(R.id.rv_ingredients_list)

        // Set the category text on the screen
        categoryTextView.text = "Category: $selectedCategory"

        // 2. Launch a background task to search the database
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = IngredientDatabase.getDatabase(applicationContext).ingredientDao()
            val foundIngredients = mutableListOf<Ingredient>()

            // Loop through each name and find its data in the database
            for (name in ingredientNames) {
                val found = dao.findIngredientByName(name)
                if (found != null) {
                    foundIngredients.add(found)
                } else {
                    // Optional: Handle ingredients that were not found in your database
                    Log.w("ResultsActivity", "Ingredient not found in database: $name")
                }
            }

            // 3. Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                if (foundIngredients.isEmpty()) {
                    titleTextView.text = "No Matching Ingredients Found"
                } else {
                    // 4. Create the adapter and attach it to the RecyclerView
                    val adapter = ResultsAdapter(foundIngredients, selectedCategory)
                    recyclerView.adapter = adapter
                }
            }
        }
    }
}