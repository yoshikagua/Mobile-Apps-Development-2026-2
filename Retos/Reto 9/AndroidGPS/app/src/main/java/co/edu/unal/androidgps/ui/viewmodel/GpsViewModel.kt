package co.edu.unal.androidgps.ui.viewmodel

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.androidgps.model.POI
import co.edu.unal.androidgps.repository.PoiRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

class GpsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val sharedPreferences = context.getSharedPreferences("gps_prefs", Context.MODE_PRIVATE)

    var currentLatitude by mutableStateOf(4.6097) // Default fallback
        private set
    var currentLongitude by mutableStateOf(-74.0817)
        private set
    var hasLocation by mutableStateOf(false)
        private set
    var currentAddressInput by mutableStateOf("")
        private set
    var radiusKm by mutableStateOf(sharedPreferences.getFloat("radius_km", 5.0f).toDouble())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("Ingrese una dirección o use el GPS para comenzar")
        private set
    var nearbyPOIs by mutableStateOf<List<POI>>(emptyList())
        private set

    init {
        // Do not load POIs until location or address is set
    }

    fun setAddressInput(address: String) {
        currentAddressInput = address
    }

    fun setRadius(newRadius: Double) {
        radiusKm = newRadius
        sharedPreferences.edit().putFloat("radius_km", newRadius.toFloat()).apply()
        updateNearbyPOIs()
    }

    fun fetchCurrentLocation(onSuccess: (Double, Double) -> Unit) {
        isLoading = true
        statusMessage = "Obteniendo ubicación GPS..."
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                isLoading = false
                if (location != null) {
                    hasLocation = true
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    statusMessage = "Ubicación GPS obtenida: ${String.format(Locale.US, "%.4f", currentLatitude)}, ${String.format(Locale.US, "%.4f", currentLongitude)}"
                    updateNearbyPOIs()
                    onSuccess(currentLatitude, currentLongitude)
                } else {
                    statusMessage = "No se pudo obtener la última ubicación GPS. Intente ingresar dirección."
                }
            }.addOnFailureListener { e ->
                isLoading = false
                statusMessage = "Error GPS: ${e.localizedMessage}"
            }
        } catch (e: SecurityException) {
            isLoading = false
            statusMessage = "Permiso de ubicación denegado."
        }
    }

    fun searchByAddress() {
        if (currentAddressInput.isBlank()) {
            statusMessage = "Ingrese una dirección válida."
            return
        }
        isLoading = true
        statusMessage = "Buscando dirección..."
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(currentAddressInput, 1)
                }
                isLoading = false
                if (!results.isNullOrEmpty()) {
                    hasLocation = true
                    val address = results[0]
                    currentLatitude = address.latitude
                    currentLongitude = address.longitude
                    statusMessage = "Dirección encontrada: ${address.getAddressLine(0) ?: currentAddressInput}"
                    updateNearbyPOIs()
                } else {
                    statusMessage = "No se encontró la dirección especificada."
                }
            } catch (e: Exception) {
                isLoading = false
                statusMessage = "Error al buscar dirección: ${e.localizedMessage}"
            }
        }
    }

    fun updateNearbyPOIs() {
        if (!hasLocation) {
            nearbyPOIs = emptyList()
            return
        }
        val allPOIs = PoiRepository.samplePOIs
        val filtered = allPOIs.map { poi ->
            val dist = calculateDistance(currentLatitude, currentLongitude, poi.latitude, poi.longitude)
            Pair(poi, dist)
        }.filter { (_, dist) ->
            dist <= radiusKm
        }.sortedBy { (_, dist) ->
            dist
        }.map { (poi, _) ->
            poi
        }
        nearbyPOIs = filtered
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
