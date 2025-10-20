package com.example.miniproject2

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject2.adapter.ResultsAdapter
import com.example.miniproject2.data.Ingredient
import com.example.miniproject2.data.IngredientDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val toolbar: Toolbar = findViewById(R.id.toolbar_results)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get the data passed from ScannerActivity
        val selectedCategory = intent.getStringExtra("EXTRA_CATEGORY") ?: "Overall"
        val ingredientNames = intent.getStringArrayListExtra("EXTRA_INGREDIENTS") ?: arrayListOf()

        val categoryTextView: TextView = findViewById(R.id.tv_selected_category)
        val recyclerView: RecyclerView = findViewById(R.id.rv_ingredients_list)

        categoryTextView.text = "Analysis for: $selectedCategory"

        // Launch a background task to search the database
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = IngredientDatabase.getDatabase(applicationContext).ingredientDao()
            val foundIngredients = mutableListOf<Ingredient>()

            for (name in ingredientNames) {
                val found = dao.findIngredientByName(name)
                if (found != null) {
                    foundIngredients.add(found)
                } else {
                    Log.w("ResultsActivity", "Ingredient not found: $name")
                }
            }

            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                if (foundIngredients.isEmpty()) {
                    findViewById<TextView>(R.id.tv_overall_summary).text = "No ingredients from your scan could be found in our database."
                    findViewById<TextView>(R.id.tv_overall_score_value).text = "-"
                } else {
                    // Set up the detailed list
                    val adapter = ResultsAdapter(foundIngredients, selectedCategory)
                    recyclerView.adapter = adapter

                    // Calculate and display the overall product score
                    displayOverallProductScore(foundIngredients, selectedCategory)
                }
            }
        }
    }

    //Calculates the overall product score using the Hybrid Method and updates the UI.

    private fun displayOverallProductScore(ingredients: List<Ingredient>, category: String) {
        val scoreValueTextView: TextView = findViewById(R.id.tv_overall_score_value)
        val summaryTextView: TextView = findViewById(R.id.tv_overall_summary)

        // 1. Get a simple list of the relevant scores for each ingredient
        val scores = ingredients.map { getScoreForCategory(it, category) }

        // 2. Find the highest score (the "Worst Offender")
        val worstOffenderScore = scores.maxOrNull()?.toDouble() ?: 0.0

        // 3. Calculate the average score
        val averageScore = scores.average()

        // 4. Apply the Hybrid Score Formula: 70% Worst Offender, 30% Average
        val finalScore = (worstOffenderScore * 0.7) + (averageScore * 0.3)

        // Format the score to one decimal place
        val df = DecimalFormat("#.#")
        scoreValueTextView.text = df.format(finalScore)

        // 5. Update the summary text and color based on the final score
        val colorRes: Int
        val summaryText: String


        // 5. Update the summary text and color based on the final score
        when (finalScore.toInt()) {
            in 0..1 -> {
                summaryText = "Low Concern"
                colorRes = R.color.green
            }
            in 2..3 -> {
                summaryText = "Moderate Concern"
                colorRes = R.color.yellow
            }
            else -> { // 4-5 and above
                summaryText = "High Concern"
                colorRes = R.color.red
            }
        }
        summaryTextView.text = summaryText
        scoreValueTextView.setTextColor(ContextCompat.getColor(this, colorRes))
    }


     // Helper function to get the correct score based on the selected category.

    private fun getScoreForCategory(ingredient: Ingredient, category: String): Int {
        return when (category.lowercase()) {
            "face" -> ingredient.skinScore
            "body" -> ingredient.bodyScore
            "hair" -> ingredient.hairScore
            "eyes" -> ingredient.eyesScore
            "lips" -> ingredient.lipsScore
            else -> ingredient.overallScore // Default to "Overall"
        }
    }

    // This makes the toolbar's back arrow functional
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}