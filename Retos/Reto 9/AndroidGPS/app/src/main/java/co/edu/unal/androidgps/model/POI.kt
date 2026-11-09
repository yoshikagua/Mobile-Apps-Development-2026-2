package co.edu.unal.androidgps.model

data class POI(
    val id: String,
    val name: String,
    val category: String, // "Hospital", "Lugar Turístico", "Parque", "Restaurante", etc.
    val latitude: Double,
    val longitude: Double,
    val description: String
)
