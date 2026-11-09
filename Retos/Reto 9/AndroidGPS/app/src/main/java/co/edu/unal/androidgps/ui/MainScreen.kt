package co.edu.unal.androidgps.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import co.edu.unal.androidgps.model.POI
import co.edu.unal.androidgps.ui.viewmodel.GpsViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: GpsViewModel) {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Mapa, 1: Lista POIs

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchCurrentLocation { _, _ -> }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android GPS & Puntos de Interés") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                    label = { Text("Mapa") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Puntos") },
                    label = { Text("Puntos Cercanos") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.currentAddressInput,
                onValueChange = { viewModel.setAddressInput(it) },
                label = { Text("Dirección (ej. Calle 26, Bogotá)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { viewModel.searchByAddress() }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar dirección")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            viewModel.fetchCurrentLocation { _, _ -> }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS Actual")
                }

                OutlinedButton(
                    onClick = { viewModel.searchByAddress() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Buscar Dirección")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Estado: ${viewModel.statusMessage}", style = MaterialTheme.typography.bodyMedium)
                    Text("Radio actual: ${String.format(Locale.US, "%.1f", viewModel.radiusKm)} km | POIs encontrados: ${viewModel.nearbyPOIs.size}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds()
                ) {
                    if (!viewModel.hasLocation) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ingrese una dirección o presione 'GPS Actual' para ver el mapa.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        var mapViewRef by remember { mutableStateOf<MapView?>(null) }
                        AndroidView(
                            factory = { ctx ->
                                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(15.0)
                                    mapViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { mapView ->
                                mapViewRef = mapView
                                mapView.overlays.clear()
                                val userGeoPoint = GeoPoint(viewModel.currentLatitude, viewModel.currentLongitude)
                                mapView.controller.setCenter(userGeoPoint)

                                val userMarker = Marker(mapView).apply {
                                    position = userGeoPoint
                                    title = "Mi Ubicación"
                                    setSnippet("Lat: ${viewModel.currentLatitude}, Lon: ${viewModel.currentLongitude}")
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    val iconDrawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)?.mutate()
                                    if (iconDrawable != null) {
                                        DrawableCompat.setTint(iconDrawable, Color.BLUE)
                                        icon = iconDrawable
                                    }
                                }
                                mapView.overlays.add(userMarker)

                                viewModel.nearbyPOIs.forEach { poi ->
                                    val poiMarker = Marker(mapView).apply {
                                        position = GeoPoint(poi.latitude, poi.longitude)
                                        title = poi.name
                                        setSnippet("${poi.category}: ${poi.description}")
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }
                                    mapView.overlays.add(poiMarker)
                                }
                                mapView.invalidate()
                            }
                        )

                        FloatingActionButton(
                            onClick = {
                                mapViewRef?.controller?.setCenter(GeoPoint(viewModel.currentLatitude, viewModel.currentLongitude))
                                mapViewRef?.controller?.setZoom(15.0)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Centrar mapa")
                        }
                    }
                }
            } else {
                if (!viewModel.hasLocation) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingrese una dirección o presione 'GPS Actual' para ver puntos de interés.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (viewModel.nearbyPOIs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay puntos de interés en el radio de ${String.format(Locale.US, "%.1f", viewModel.radiusKm)} km.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.nearbyPOIs) { poi ->
                            PoiCard(poi)
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            currentRadius = viewModel.radiusKm,
            onDismiss = { showSettings = false },
            onSave = { newRadius ->
                viewModel.setRadius(newRadius)
            }
        )
    }
}

@Composable
fun PoiCard(poi: POI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(poi.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Badge { Text(poi.category) }
            Spacer(modifier = Modifier.height(4.dp))
            Text(poi.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lat: ${poi.latitude}, Lon: ${poi.longitude}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
