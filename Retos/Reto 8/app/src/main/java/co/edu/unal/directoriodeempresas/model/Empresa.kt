package co.edu.unal.directoriodeempresas.model

data class Empresa(
    val id: Long = 0,
    val nombre: String,
    val urlWeb: String,
    val telefono: String,
    val email: String,
    val servicios: String,
    val clasificacion: String
) {
    companion object {
        val CLASIFICACIONES_DISPONIBLES = listOf(
            "Consultoría",
            "Desarrollo a la medida",
            "Fábrica de software"
        )
    }

    fun getClasificacionesList(): List<String> {
        if (clasificacion.isBlank()) return emptyList()
        return clasificacion.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
