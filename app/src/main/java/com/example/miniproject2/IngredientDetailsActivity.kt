// In IngredientDetailActivity.kt
package com.example.miniproject2
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IngredientDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_detail)

        val ingredient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("INGREDIENT_DATA", TrendingIngredient::class.java)
        } else {
            intent.getSerializableExtra("INGREDIENT_DATA") as? TrendingIngredient
        }

        if (ingredient != null) {
            val nameTextView: TextView = findViewById(R.id.ingredientName)
            val imageView: ImageView = findViewById(R.id.ingredientImage)
            val benefitsTextView: TextView = findViewById(R.id.benefitsText)
            val usageTextView: TextView = findViewById(R.id.usageText)
            val safetyTextView: TextView = findViewById(R.id.safetyText)

            nameTextView.text = ingredient.name
            imageView.setImageResource(ingredient.imageResId)
            benefitsTextView.text = ingredient.benefits
            usageTextView.text = ingredient.usageTips
            safetyTextView.text = ingredient.safetyDetails
        }
    }
}