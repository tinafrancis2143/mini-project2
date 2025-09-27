package com.example.miniproject2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject2.R
import com.example.miniproject2.data.Ingredient

class ResultsAdapter(
    private val ingredients: List<Ingredient>,
    private val category: String
) : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

    // This class holds the views for a single list item.
    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val scoreIndicator: View = itemView.findViewById(R.id.view_score_indicator)
        val ingredientName: TextView = itemView.findViewById(R.id.tv_ingredient_name)
        val ingredientScore: TextView = itemView.findViewById(R.id.tv_ingredient_score)
    }

    // Called when RecyclerView needs a new ViewHolder (a new row).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_result, parent, false)
        return ResultViewHolder(view)
    }

    // Returns the total number of items in the list.
    override fun getItemCount(): Int {
        return ingredients.size
    }

    // This is the most important function. It binds the data to the views for each row.
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val ingredient = ingredients[position]

        // 1. Set the ingredient name
        holder.ingredientName.text = ingredient.name.replaceFirstChar { it.uppercase() }

        // 2. Determine which score to display based on the selected category
        val score = when (category.lowercase()) {
            "face" -> ingredient.skinScore
            "body" -> ingredient.bodyScore
            "hair" -> ingredient.hairScore
            "eyes" -> ingredient.eyesScore
            "lips" -> ingredient.lipsScore
            else -> ingredient.overallScore // Default to "Overall"
        }

        // 3. Set the score text
        holder.ingredientScore.text = score.toString()

        // 4. Set the color of the score and the indicator circle based on the score value
        when (score) {
            in 0..2 -> { // Low hazard (Green)
                holder.ingredientScore.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
                holder.scoreIndicator.setBackgroundResource(R.drawable.circle_background_green)
            }
            in 3..6 -> { // Moderate hazard (Yellow)
                holder.ingredientScore.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
                holder.scoreIndicator.setBackgroundResource(R.drawable.circle_background_yellow)
            }
            else -> { // High hazard (Red)
                holder.ingredientScore.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
                holder.scoreIndicator.setBackgroundResource(R.drawable.circle_background_red)
            }
        }
    }
}