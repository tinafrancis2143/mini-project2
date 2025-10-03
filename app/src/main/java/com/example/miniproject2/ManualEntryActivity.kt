package com.example.miniproject2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText

class ManualEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        // Set up the toolbar with a back button
        val toolbar: Toolbar = findViewById(R.id.toolbar_manual_entry)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get references to our UI elements
        val ingredientsInput: TextInputEditText = findViewById(R.id.et_ingredients_input)
        val analyzeButton: Button = findViewById(R.id.btn_analyze_manual)

        // Set the click listener for the analyze button
        analyzeButton.setOnClickListener {
            val rawText = ingredientsInput.text.toString()

            if (rawText.isBlank()) {
                Toast.makeText(this, "Please enter some ingredients.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // We reuse the exact same parsing logic from the scanner
            val cleanIngredients = parseIngredientText(rawText)

            if (cleanIngredients.isEmpty()) {
                Toast.makeText(this, "Could not identify any ingredients.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // We also reuse the exact same category selection dialog
            showCategorySelectionDialog(cleanIngredients)
        }
    }

    /**
     * Shows a dialog asking the user to select the product's usage category.
     * This is the same function used in ScannerActivity.
     */
    private fun showCategorySelectionDialog(ingredients: List<String>) {
        val categories = arrayOf("Overall", "Face", "Body", "Hair", "Eyes", "Lips")

        AlertDialog.Builder(this)
            .setTitle("Select Product Category")
            .setItems(categories) { _, which ->
                val selectedCategory = categories[which]

                // Create an Intent to start the ResultsActivity and pass the data
                val intent = Intent(this, ResultsActivity::class.java).apply {
                    putExtra("EXTRA_CATEGORY", selectedCategory)
                    putStringArrayListExtra("EXTRA_INGREDIENTS", ArrayList(ingredients))
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * A robust function to clean up user-entered text. It handles commas,
     * new lines, and other common formatting issues.
     */
    private fun parseIngredientText(rawText: String?): List<String> {
        if (rawText.isNullOrBlank()) {
            return emptyList()
        }

        // Normalize text by replacing new lines with commas
        var processedText = rawText.replace('\n', ',')

        // Remove any text inside parentheses
        processedText = processedText.replace(Regex("\\(.*?\\)"), "")

        // Split by comma and clean up each individual ingredient
        return processedText
            .split(",")
            .map {
                it.trim() // Remove leading/trailing whitespace
                    .replace(Regex("\\s+"), " ") // Replace multiple spaces with a single one
                    .lowercase() // Make it lowercase for matching
            }
            .filter { it.length > 2 } // Remove empty or tiny items
            .distinct() // Remove duplicates
    }

    // This makes the toolbar's back arrow functional
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}