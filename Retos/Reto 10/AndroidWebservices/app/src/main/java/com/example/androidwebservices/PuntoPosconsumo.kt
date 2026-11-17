package com.example.androidwebservices

import org.json.JSONObject

/**
 * Modelo de datos para un Punto Posconsumo obtenido del servicio web Socrata en datos.gov.co.
 * Utiliza org.json.JSONObject del SDK de Android para la deserialización manual.
 */
data class PuntoPosconsumo(
    val programa: String,
    val tipoResiduos: String,
    val departamento: String,
    val municipio: String,
    val codigoDane: String,
    val direccion: String,
    val razonSocial: String,
    val rawJsonString: String
) {
    companion object {
        /**
         * Crea una instancia de PuntoPosconsumo procesando el objeto JSON que provee el SDK de Android.
         */
        fun fromJSONObject(jsonObject: JSONObject): PuntoPosconsumo {
            return PuntoPosconsumo(
                programa = jsonObject.optString("programa_de_recolecci_n", "No especificado"),
                tipoResiduos = jsonObject.optString("tipo_de_residuos", "No especificado"),
                departamento = jsonObject.optString("departamento", "Norte de Santander"),
                municipio = jsonObject.optString("municipio", "No especificado"),
                codigoDane = jsonObject.optString("codigo_dane", "N/A"),
                direccion = jsonObject.optString("direccion", "No registrada"),
                razonSocial = jsonObject.optString("raz_n_social", "Sin nombre registrado"),
                rawJsonString = jsonObject.toString(2)
            )
        }
    }
}
