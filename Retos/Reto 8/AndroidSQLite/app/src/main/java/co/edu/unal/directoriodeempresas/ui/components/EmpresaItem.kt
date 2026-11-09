package co.edu.unal.directoriodeempresas.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import co.edu.unal.directoriodeempresas.model.Empresa

@Composable
fun EmpresaItem(
    empresa: Empresa,
    onEditClick: (Empresa) -> Unit,
    onDeleteClick: (Empresa) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Company Name + Edit/Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = empresa.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = { onEditClick(empresa) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar empresa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDeleteClick(empresa) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar empresa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Classifications (Chips)
            val clasificaciones = empresa.getClasificacionesList()
            if (clasificaciones.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    clasificaciones.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(text = tag, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Products / Services
            if (empresa.servicios.isNotBlank()) {
                Text(
                    text = "Productos y Servicios:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = empresa.servicios,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Contact info: Phone
            if (empresa.telefono.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${empresa.telefono.trim()}"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Teléfono",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = empresa.telefono,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Contact info: Email
            if (empresa.email.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${empresa.email.trim()}"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = empresa.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Contact info: Website
            if (empresa.urlWeb.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clickable {
                            try {
                                val formattedUrl = if (!empresa.urlWeb.startsWith("http://") && !empresa.urlWeb.startsWith("https://")) {
                                    "https://${empresa.urlWeb.trim()}"
                                } else {
                                    empresa.urlWeb.trim()
                                }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Web",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = empresa.urlWeb,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}
