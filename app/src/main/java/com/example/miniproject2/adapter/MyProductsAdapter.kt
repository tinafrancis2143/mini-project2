package com.example.miniproject2.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject2.R
import com.example.miniproject2.data.MyProduct
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyProductsAdapter(private var products: List<MyProduct>) : RecyclerView.Adapter<MyProductsAdapter.ProductViewHolder>() {

    // This class holds the views for a single product card.
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
        val productName: TextView = itemView.findViewById(R.id.product_name_text_view)
        val brandName: TextView = itemView.findViewById(R.id.brand_text_view)
        // Correctly finds BOTH TextViews from your final layout
        val expiryDate: TextView = itemView.findViewById(R.id.expiry_date_text_view)
        val daysLeft: TextView = itemView.findViewById(R.id.days_left_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // IMPORTANT: Make sure this name matches your card layout file exactly
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        // This line fixes the bug where deleted items leave a red background
        holder.itemView.setBackgroundColor(Color.TRANSPARENT)

        val product = products[position]

        // Set the product and brand name
        holder.productName.text = product.productName
        holder.brandName.text = product.brand

        // --- THIS IS THE CORRECTED LOGIC ---
        // 1. Format and set the full expiry date text
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.expiryDate.text = "Expires: ${sdf.format(Date(product.expiryDate))}"

        // 2. Calculate and set the "days left" indicator and status colors
        val currentDate = System.currentTimeMillis()
        val expiryDateMillis = product.expiryDate
        val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(expiryDateMillis - currentDate)

        val indicatorColor: Int
        val daysLeftText: String
        val daysLeftBgColor: Int

        when {
            daysUntilExpiry < 0 -> {
                daysLeftText = "Expired"
                indicatorColor = Color.parseColor("#D32F2F") // Red
                daysLeftBgColor = Color.parseColor("#D32F2F")
            }
            daysUntilExpiry <= 30 -> {
                daysLeftText = "$daysUntilExpiry days"
                indicatorColor = Color.parseColor("#FFA000") // Yellow
                daysLeftBgColor = Color.parseColor("#FFA000")
            }
            else -> {
                daysLeftText = "$daysUntilExpiry days"
                indicatorColor = Color.parseColor("#388E3C") // Green
                daysLeftBgColor = Color.parseColor("#388E3C")
            }
        }

        // Set the color for the small circle indicator
        (holder.statusIndicator.background as? GradientDrawable)?.setColor(indicatorColor)

        // Set the text and background color for the "days left" pill
        holder.daysLeft.text = daysLeftText
        (holder.daysLeft.background as? GradientDrawable)?.setColor(daysLeftBgColor)
    }

    // A helper function to update the list of products when the data changes.
    fun updateProducts(newProducts: List<MyProduct>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    // A helper function to get the product that was swiped for deletion.
    fun getProductAt(position: Int): MyProduct {
        return products[position]
    }
}