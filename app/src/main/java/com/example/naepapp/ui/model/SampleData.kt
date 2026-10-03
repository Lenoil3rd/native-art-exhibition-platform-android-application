package com.example.naepapp.ui.model

import com.example.naepapp.ui.model.CategoryUi

// ui/model/SampleData.kt  — MODEL: fake data so every screen has something to show
object SampleData {
    val categories = listOf(
        CategoryUi(1, "Paintings"), CategoryUi(2, "Pottery and Ceramics"), CategoryUi(3, "Carvings (Wood works)"),
        CategoryUi(4, "Sculptures"), CategoryUi(5, "Drawings (Sketches and Realism)"),
    )

    val artisans = listOf(
        ArtisanUi("a1", "Chikondi Banda", "Lilongwe", "I paint the lake shore life I grew up with, using bold colors and local pigments.",
            1, "Paintings", "+265991000001"),
        ArtisanUi("a2", "Tadala Phiri", "Zomba", "Third-generation potter shaping clay from the Shire valley.",
            2, "Pottery and Ceramics", "+265991000002"),
    )
    const val myArtisanId = "a1"        // the demo artisan who is "signed in" when you choose Artisan

    val creations = listOf(
        CreationUi("c1", "Sunrise at Cape Maclear", 1, "Paintings", "a1", "Chikondi Banda", 150000.0, available = true, likes = 42, views = 310, imageUrl = "",
            dimensions = "60 cm x 90 cm", description = "Acrylic on canvas capturing the first light over the lake."),
        CreationUi("c2", "Market Day", 1, "Paintings", "a1", "Chikondi Banda", 0.0, available = false, likes = 18, views = 120, imageUrl = "",
            dimensions = "50 cm x 70 cm", description = "Oil on board. A busy morning at the local market."),
        CreationUi("c3", "Water Pot (Mphika)", 2, "Pottery and Ceramics", "a2", "Tadala Phiri", 25000.0, available = true, likes = 30, views = 210, imageUrl = "",
            dimensions = "35 cm tall", description = "Hand-coiled and pit-fired using traditional methods."),
        CreationUi("c4", "Serving Bowl Set", 2, "Pottery and Ceramics", "a2", "Tadala Phiri", 40000.0, available = true, likes = 12, views = 95, imageUrl = "",
            dimensions = "Set of 3", description = "Glazed stoneware bowls for everyday use."),
        CreationUi("c5", "Mother and Child", 3, "Carvings (Wood works)", "a2", "Tadala Phiri", 220000.0, available = true, likes = 55, views = 400, imageUrl = "",
            dimensions = "45 cm tall", description = "Carved from a single block of mahogany."),
        CreationUi("c6", "Fisherman Study", 5, "Drawings (Sketches and Realism)", "a1", "Chikondi Banda", 35000.0, available = false, likes = 8, views = 60, imageUrl = "",
            dimensions = "A3", description = "Graphite realism study from a sketchbook."),
    )

    fun creation(id: String) = creations.first { it.id == id }
    fun artisan(id: String) = artisans.first { it.id == id }
    fun creationsOf(artisanId: String) = creations.filter { it.artisanId == artisanId }

    val enthusiast = UserUi("Mercy Mwale", "mercy@example.com", "+265991234567", role = "Enthusiast", onboardingDone = false)  // first login => role prompt
}