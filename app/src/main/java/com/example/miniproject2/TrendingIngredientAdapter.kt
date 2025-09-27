package com.example.miniproject2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable
import com.example.miniproject2.TrendingIngredient

// The adapter class, which takes a list of ingredients
class TrendingIngredientAdapter(private val ingredients: List<TrendingIngredient>) :
    RecyclerView.Adapter<TrendingIngredientAdapter.TrendingIngredientViewHolder>() {  //connects data with recyclerview to display items

    // A nested class that holds references to the views in our item layout.
    class TrendingIngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientImage: ImageView = itemView.findViewById(R.id.imageView2)
        val ingredientName: TextView = itemView.findViewById(R.id.textView13)
    }

    // This method is called when the RecyclerView needs a new ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingIngredientViewHolder {

        val view = LayoutInflater.from(parent.context) //layout inflater to xml file to view
            .inflate(R.layout.item_ingredient, parent, false)
        return TrendingIngredientViewHolder(view)
    }

    // This method is called to bind data to a ViewHolder.
    override fun onBindViewHolder(holder: TrendingIngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]

        // Set the image and text based on the data.
        holder.ingredientImage.setImageResource(ingredient.imageResId)
        holder.ingredientName.text = ingredient.name

        // Set up a click listener for the entire card.
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, IngredientDetailActivity::class.java).apply {
                putExtra("INGREDIENT_DATA", ingredient as Serializable)  //converts to a format that can be send b/w contexts
            }
            context.startActivity(intent)
        }
    }

    // This method returns the total number of items in the data list.
    override fun getItemCount(): Int {
        return ingredients.size
    }
}