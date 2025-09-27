package com.example.miniproject2

import java.io.Serializable

data class TrendingIngredient(
    val name: String,
    val imageResId: Int,
    val benefits: String,
    val usageTips: String,
    val safetyDetails: String
) : Serializable