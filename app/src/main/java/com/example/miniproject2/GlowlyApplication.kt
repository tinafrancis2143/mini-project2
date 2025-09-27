package com.example.miniproject2

import android.app.Application
import com.example.miniproject2.data.IngredientDatabase

class GlowlyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the database as soon as the application is created.
        // This ensures it's ready before any activity needs it.
        IngredientDatabase.getDatabase(this)
    }
}