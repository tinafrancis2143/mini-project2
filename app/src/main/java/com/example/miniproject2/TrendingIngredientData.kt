package com.example.miniproject2

object TrendingIngredientData {
    val ingredients: List<TrendingIngredient> = listOf(
        TrendingIngredient(
            "Glycolic Acid",
            R.drawable.glycolic_acid,
            "Exfoliates skin, improves texture, and reduces fine lines.",
            "Start with a low concentration and use 2-3 times per week. Always use sunscreen the following day.",
            "Generally safe, but may cause irritation or increased sun sensitivity for some users."
        ),
        TrendingIngredient(
            "Hyaluronic Acid",
            R.drawable.hyaluronic_acid,
            "Provides intense hydration, plumps skin, and reduces the appearance of wrinkles.",
            "Apply to damp skin for better absorption. Can be used daily.",
            "Very safe and well-tolerated by most skin types."
        ),
        TrendingIngredient(
            "Niacinamide",
            R.drawable.niacinamide,
            "Reduces redness, minimizes pores, and regulates oil.",
            "Can be used daily, morning and night. Works well with most other ingredients.",
            "Generally safe, but can cause mild flushing in some individuals."
        ),
        TrendingIngredient(
            "Salicylic Acid",
            R.drawable.salicylic_acid,
            "Exfoliates pores, reduces acne, and controls oil production.",
            "Start with a low concentration. Use on targeted areas or 2-3 times per week.",
            "May cause dryness or peeling. Use with caution if you have dry or sensitive skin."
        )
    )
}