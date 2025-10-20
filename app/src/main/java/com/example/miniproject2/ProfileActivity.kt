package com.example.miniproject2

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject2.adapter.MyProductsAdapter
import com.example.miniproject2.data.IngredientDatabase
import com.example.miniproject2.data.MyProduct
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var productsAdapter: MyProductsAdapter
    private lateinit var addProductButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        addProductButton = findViewById(R.id.add_product_button)
        productsRecyclerView = findViewById(R.id.products_recycler_view)

        setupRecyclerView()

        addProductButton.setOnClickListener {
            val intent = Intent(this, AddNewProductActivity::class.java)
            startActivity(intent)
        }

        observeProducts()
    }

    private fun setupRecyclerView() {
        productsAdapter = MyProductsAdapter(emptyList())
        productsRecyclerView.adapter = productsAdapter
        productsRecyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(productsRecyclerView)
    }

    private fun observeProducts() {
        val productDao = IngredientDatabase.getDatabase(applicationContext).myProductDao()
        lifecycleScope.launch {
            productDao.getAllProducts().collectLatest { productList ->
                productsAdapter.updateProducts(productList)
            }
        }
    }

    // This object contains all the logic for handling swipe gestures
    private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        // --- THIS FUNCTION WAS MISSING ---
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            // We don't need to handle moving items up and down in this app, so we just return false.
            return false
        }

        // --- THIS FUNCTION WAS MISSING ---
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // This is called when an item is fully swiped away.
            val position = viewHolder.adapterPosition
            val productToDelete = productsAdapter.getProductAt(position)
            deleteProduct(productToDelete)
        }

        // This function draws the red background and delete icon as you swipe
        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            // This part is for the visual feedback (red background and icon)
            val itemView = viewHolder.itemView
            val icon = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.ic_delete)!!
            val backgroundColor = ContextCompat.getColor(this@ProfileActivity, R.color.red)

            itemView.setBackgroundColor(backgroundColor)

            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + icon.intrinsicHeight

            if (dX > 0) { // Swiping right
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + icon.intrinsicWidth
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            } else if (dX < 0) { // Swiping left
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            } else { // view is not being swiped
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            icon.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun deleteProduct(product: MyProduct) {
        lifecycleScope.launch(Dispatchers.IO) {
            val productDao = IngredientDatabase.getDatabase(applicationContext).myProductDao()
            productDao.deleteProductById(product.id)
        }

        Snackbar.make(productsRecyclerView, "${product.productName} deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                undoDelete(product)
            }
            .show()
    }

    private fun undoDelete(product: MyProduct) {
        lifecycleScope.launch(Dispatchers.IO) {
            val productDao = IngredientDatabase.getDatabase(applicationContext).myProductDao()
            productDao.insertProduct(product)
        }
    }
}