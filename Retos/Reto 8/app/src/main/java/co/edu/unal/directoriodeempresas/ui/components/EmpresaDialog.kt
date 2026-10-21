package co.edu.unal.directoriodeempresas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.directoriodeempresas.model.Empresa

@Composable
fun EmpresaDialog(
    empresaToEdit: Empresa?,
    onDismiss: () -> Unit,
    onSave: (
        nombre: String,
        urlWeb: String,
        telefono: String,
        email: String,
        servicios: String,
        clasificaciones: List<String>
    ) -> Unit
) {
    var nombre by remember { mutableStateOf(empresaToEdit?.nombre ?: "") }
    var urlWeb by remember { mutableStateOf(empresaToEdit?.urlWeb ?: "") }
    var telefono by remember { mutableStateOf(empresaToEdit?.telefono ?: "") }
    var email by remember { mutableStateOf(empresaToEdit?.email ?: "") }
    var servicios by remember { mutableStateOf(empresaToEdit?.servicios ?: "") }

    val initialClasificaciones = remember(empresaToEdit) {
        empresaToEdit?.getClasificacionesList()?.toSet() ?: emptySet()
    }
    var selectedClasificaciones by remember { mutableStateOf(initialClasificaciones) }

    var isNombreError by remember { mutableStateOf(false) }

    val isEditing = empresaToEdit != null
    val dialogTitle = if (isEditing) "Editar Empresa" else "Agregar Empresa"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        if (it.isNotBlank()) isNombreError = false
                    },
                    label = { Text("Nombre de la empresa *") },
                    isError = isNombreError,
                    supportingText = {
                        if (isNombreError) {
                            Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // URL Web
                OutlinedTextField(
                    value = urlWeb,
                    onValueChange = { urlWeb = it },
                    label = { Text("URL de la página web") },
                    placeholder = { Text("https://www.ejemplo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono de contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email de contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Productos y Servicios
                OutlinedTextField(
                    value = servicios,
                    onValueChange = { servicios = it },
                    label = { Text("Productos y Servicios") },
                    placeholder = { Text("Ej: Desarrollo móvil, Consultoría cloud...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Clasificación Checkboxes
                Text(
                    text = "Clasificación de la empresa:",
                    style = MaterialTheme.typography.titleSmall
                )

                Empresa.CLASIFICACIONES_DISPONIBLES.forEach { clasificacionOption ->
                    val isChecked = selectedClasificaciones.contains(clasificacionOption)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedClasificaciones = if (checked) {
                                    selectedClasificaciones + clasificacionOption
                                } else {
                                    selectedClasificaciones - clasificacionOption
                                }
                            }
                        )
                        Text(
                            text = clasificacionOption,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isBlank()) {
                        isNombreError = true
                    } else {
                        onSave(
                            nombre,
                            urlWeb,
                            telefono,
                            email,
                            servicios,
                            selectedClasificaciones.toList()
                        )
                    }
                }
            ) {
                Text(if (isEditing) "Guardar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
