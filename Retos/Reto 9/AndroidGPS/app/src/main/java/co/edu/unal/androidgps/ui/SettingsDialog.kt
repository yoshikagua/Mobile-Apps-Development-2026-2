package co.edu.unal.androidgps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentRadius: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var sliderValue by remember { mutableStateOf(currentRadius.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Radio de Búsqueda") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Radio seleccionado: ${String.format(java.util.Locale.US, "%.1f", sliderValue)} km")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..50f,
                    steps = 49
                )
                Text("Deslice para ajustar el radio de búsqueda (1 km a 50 km).", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(sliderValue.toDouble())
                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
