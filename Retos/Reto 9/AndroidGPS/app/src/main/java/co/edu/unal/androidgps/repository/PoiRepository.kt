package co.edu.unal.androidgps.repository

import co.edu.unal.androidgps.model.POI

object PoiRepository {
    val samplePOIs = listOf(
        POI("1", "Hospital Universitario San Ignacio", "Hospital", 4.6281, -74.0645, "Hospital general de alta complejidad"),
        POI("2", "Hospital Militar Central", "Hospital", 4.6294, -74.0683, "Hospital de referencia militar"),
        POI("3", "Monserrate", "Lugar Turístico", 4.6058, -74.0558, "Santuario y cerro tutelar con vista panorámica"),
        POI("4", "Museo del Oro", "Lugar Turístico", 4.6014, -74.0721, "Museo arqueológico de orfebrería prehispánica"),
        POI("5", "Parque Nacional Enrique Olaya Herrera", "Parque", 4.6225, -74.0650, "Parque metropolitano histórico"),
        POI("6", "Plaza de Bolívar", "Lugar Turístico", 4.5981, -74.0758, "Plaza principal de la ciudad"),
        POI("7", "Jardín Botánico de Bogotá", "Parque", 4.6684, -74.1026, "Jardín botánico y centro de investigación"),
        POI("8", "Fundación Santa Fe de Bogotá", "Hospital", 4.7012, -74.0321, "Centro médico e investigación de alta calidad"),
        POI("9", "Parque Metropolitano Simón Bolívar", "Parque", 4.6583, -74.0931, "Gran parque urbano para recreación"),
        POI("10", "Museo Botero", "Lugar Turístico", 4.5968, -74.0729, "Museo de arte con obras de Fernando Botero")
    )
}
