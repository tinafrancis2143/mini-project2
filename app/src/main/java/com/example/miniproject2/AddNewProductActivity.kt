package com.example.miniproject2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNewProductActivity : AppCompatActivity() {

    // Declare variables for all UI elements in XML
    private lateinit var addProductTitleTextView: TextView
    private lateinit var addProductSubtitleTextView: TextView
    private lateinit var productNameEditText: EditText
    private lateinit var brandEditText: EditText
    private lateinit var selectDateButton: Button
    private lateinit var selectedDateTextView: TextView
    private lateinit var saveProductButton: Button

    // A variable to store the selected expiry date
    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        // Initialize all declared variables by finding them in your XML layout
        addProductTitleTextView = findViewById(R.id.add_product_title)
        addProductSubtitleTextView = findViewById(R.id.add_product_subtitle)
        productNameEditText = findViewById(R.id.product_name_edit_text)
        brandEditText = findViewById(R.id.brand_edit_text)
        selectDateButton = findViewById(R.id.select_date_button)
        selectedDateTextView = findViewById(R.id.selected_date_text_view)
        saveProductButton = findViewById(R.id.save_product_button)

        // Set up the click listener for the "Select Expiry Date" button
        selectDateButton.setOnClickListener {
            showDatePicker()
        }

        // Set up the click listener for the "Save Product" button
        saveProductButton.setOnClickListener {
            // For now, this just closes the activity.  will add
            // the logic to save the product details to database.
            finish()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Expiry Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        // listener for when a date is selected
        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)

            // Format the date and display it on the TextView
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            selectedDateTextView.text = dateFormat.format(selectedDate)
        }

        datePicker.show(supportFragmentManager, "DatePicker")
    }
}