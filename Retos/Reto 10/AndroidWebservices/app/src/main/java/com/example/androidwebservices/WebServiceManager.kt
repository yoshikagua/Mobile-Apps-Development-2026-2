package com.example.androidwebservices

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Manager encargado de realizar las peticiones HTTP al servicio web de Datos Abiertos (datos.gov.co)
 * y procesar la respuesta en formato JSON utilizando el paquete org.json.* nativo del SDK de Android.
 */
object WebServiceManager {

    private const val BASE_URL = "https://www.datos.gov.co/resource/c2fr-ezse.json"

    /**
     * Realiza la consulta HTTP GET con los parámetros enviados por el usuario y
     * procesa el resultado devuelto utilizando JSONArray y JSONObject del SDK de Android.
     *
     * @param programa Filtro por programa de recolección ("Todos" para omitir)
     * @param municipio Filtro por municipio ("Todos" para omitir)
     * @param busqueda Texto de búsqueda libre (tipo de residuos / razón social)
     * @param limite Cantidad máxima de registros a solicitar ($limit)
     * @return Lista de objetos [PuntoPosconsumo] procesados a partir del JSON devuelto
     */
    suspend fun consultarPuntosPosconsumo(
        programa: String?,
        municipio: String?,
        busqueda: String?,
        limite: Int = 50
    ): List<PuntoPosconsumo> = withContext(Dispatchers.IO) {
        val queryParams = mutableListOf<String>()

        // Límite de resultados ($limit Socrata query parameter)
        queryParams.add("\$limit=$limite")

        val filters = mutableListOf<String>()
        if (!programa.isNullOrBlank() && programa != "Todos" && programa != "Todos los Programas") {
            filters.add("upper(programa_de_recolecci_n) like upper('%${escapeLike(programa)}%')")
        }
        if (!municipio.isNullOrBlank() && municipio != "Todos" && municipio != "Todos los Municipios") {
            filters.add("upper(municipio) like upper('%${escapeLike(municipio)}%')")
        }
        if (!busqueda.isNullOrBlank()) {
            filters.add(
                "(upper(tipo_de_residuos) like upper('%${escapeLike(busqueda)}%') " +
                    "OR upper(programa_de_recolecci_n) like upper('%${escapeLike(busqueda)}%'))"
            )
        }
        if (filters.isNotEmpty()) {
            queryParams.add("\$where=" + URLEncoder.encode(filters.joinToString(" AND "), "UTF-8"))
        }

        val urlString = if (queryParams.isNotEmpty()) {
            "$BASE_URL?${queryParams.joinToString("&")}"
        } else {
            BASE_URL
        }

        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "AndroidWebservicesApp/1.0")
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseBuilder.append(line)
                }
                reader.close()

                val jsonResponseString = responseBuilder.toString()

                // PROCESAMIENTO EXPLICITO CON org.json.JSONArray DEL SDK DE ANDROID
                val jsonArray = JSONArray(jsonResponseString)
                val puntosList = mutableListOf<PuntoPosconsumo>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    puntosList.add(PuntoPosconsumo.fromJSONObject(jsonObject))
                }

                return@withContext puntosList
            } else {
                throw Exception("Error del servidor HTTP. Código: $responseCode")
            }
        } finally {
            connection.disconnect()
        }

    }

    private fun escapeLike(value: String): String =
        value.trim().replace("'", "''")
}
