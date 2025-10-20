package com.example.miniproject2

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.miniproject2.data.IngredientDatabase
import com.example.miniproject2.data.MyProduct
import com.example.miniproject2.notifications.NotificationReceiver
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddNewProductActivity : AppCompatActivity() {

    private lateinit var productNameEditText: TextInputEditText
    private lateinit var brandEditText: TextInputEditText
    private lateinit var selectDateButton: Button
    private lateinit var selectedDateTextView: TextView
    private lateinit var saveProductButton: Button

    private var selectedExpiryDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        val toolbar: Toolbar = findViewById(R.id.toolbar_add_product)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        productNameEditText = findViewById(R.id.product_name_edit_text)
        brandEditText = findViewById(R.id.brand_edit_text)
        selectDateButton = findViewById(R.id.select_date_button)
        selectedDateTextView = findViewById(R.id.selected_date_text_view)
        saveProductButton = findViewById(R.id.save_product_button)

        selectDateButton.setOnClickListener { showDatePickerDialog() }
        saveProductButton.setOnClickListener { saveProduct() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedExpiryDate.set(year, month, day)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        selectedDateTextView.text = sdf.format(selectedExpiryDate.time)
    }

    private fun saveProduct() {
        val productName = productNameEditText.text.toString().trim()
        val brandName = brandEditText.text.toString().trim()

        if (productName.isEmpty() || brandName.isEmpty() || selectedDateTextView.text == "No date selected") {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = IngredientDatabase.getDatabase(applicationContext).myProductDao()
            val newProduct = MyProduct(
                productName = productName,
                brand = brandName,
                expiryDate = selectedExpiryDate.timeInMillis
            )

            val newProductId = dao.insertProduct(newProduct)
            scheduleNotificationsForProduct(newProduct.copy(id = newProductId.toInt()))
        }

        Toast.makeText(this, "Product Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    val testMode = false // Set true for testing, false for production

    private fun scheduleNotificationsForProduct(product: MyProduct) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)

        val productName = product.productName
        val productId = product.id

        // --- THIS IS THE CORRECTED LOGIC ---
        // Get the expiry date, but set the time to 9:00 AM to be consistent
        val expiryCalendar = Calendar.getInstance().apply { timeInMillis = product.expiryDate; set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0) }
        val expiryTime = expiryCalendar.timeInMillis

        // For testing the "today" notification, we'll set it for 30 seconds from now
        val today = Calendar.getInstance()
        val expiryDay = Calendar.getInstance().apply { timeInMillis = product.expiryDate }
        val isToday = today.get(Calendar.YEAR) == expiryDay.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == expiryDay.get(Calendar.DAY_OF_YEAR)

        val todayNotificationTime = if (isToday) {
            // If expiry is today, set the alarm for 30 seconds from now for easy testing
            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)
        } else {
            expiryTime
        }

        val notifications = if (testMode) {
            val now = System.currentTimeMillis()
            listOf(
                Pair(now + 5000, "Test: '$productName' notification in 5 seconds"),
                Pair(now + 10000, "Test: '$productName' notification in 10 seconds"),
                Pair(now + 15000, "Test: '$productName' notification in 15 seconds"),
                Pair(now + 20000, "Test: '$productName' notification in 20 seconds")
            )
        } else {
            listOf(
                Pair(expiryTime - TimeUnit.DAYS.toMillis(30), "Heads-up! Your '$productName' is expiring in a month."),
                Pair(expiryTime - TimeUnit.DAYS.toMillis(7), "Reminder: Your '$productName' expires in one week."),
                Pair(todayNotificationTime, "Final Alert: Your '$productName' expires today.😭"),
                Pair(expiryTime + TimeUnit.DAYS.toMillis(7), "Cleanup: Your '$productName' is now expired.")
            )
        }


        notifications.forEachIndexed { index, (timeInMillis, message) ->
            val requestCode = productId * 10 + index

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent.apply {
                    putExtra("notificationId", requestCode)
                    putExtra("message", message)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (timeInMillis > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    Log.d("NotificationScheduler", "Scheduled EXACT notification for '$productName' at ${Date(timeInMillis)}")
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    Log.d("NotificationScheduler", "Scheduled INEXACT notification for '$productName' at ${Date(timeInMillis)}")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}