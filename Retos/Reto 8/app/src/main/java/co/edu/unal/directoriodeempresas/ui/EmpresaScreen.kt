package co.edu.unal.directoriodeempresas.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.directoriodeempresas.ui.components.ConfirmDeleteDialog
import co.edu.unal.directoriodeempresas.ui.components.EmpresaDialog
import co.edu.unal.directoriodeempresas.ui.components.EmpresaItem
import co.edu.unal.directoriodeempresas.ui.components.FilterBar
import co.edu.unal.directoriodeempresas.ui.viewmodel.EmpresaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaScreen(
    viewModel: EmpresaViewModel = viewModel()
) {
    val empresas by viewModel.empresas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedClasificacion by viewModel.selectedClasificacion.collectAsState()
    val showFormDialog by viewModel.showFormDialog.collectAsState()
    val empresaToEdit by viewModel.empresaToEdit.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val empresaToDelete by viewModel.empresaToDelete.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Directorio de Empresas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Empresa"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                selectedClasificacion = selectedClasificacion,
                onClasificacionSelected = { viewModel.onClasificacionFilterChanged(it) }
            )

            if (empresas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron empresas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = empresas,
                        key = { it.id }
                    ) { empresa ->
                        EmpresaItem(
                            empresa = empresa,
                            onEditClick = { viewModel.openEditDialog(it) },
                            onDeleteClick = { viewModel.openDeleteConfirmation(it) }
                        )
                    }
                }
            }
        }
    }

    // Dialog for Add / Edit Company
    if (showFormDialog) {
        EmpresaDialog(
            empresaToEdit = empresaToEdit,
            onDismiss = { viewModel.closeFormDialog() },
            onSave = { nombre, urlWeb, telefono, email, servicios, clasificaciones ->
                viewModel.saveEmpresa(
                    nombre = nombre,
                    urlWeb = urlWeb,
                    telefono = telefono,
                    email = email,
                    servicios = servicios,
                    clasificaciones = clasificaciones
                )
            }
        )
    }

    // Dialog for Confirming Delete
    val currentEmpresaToDelete = empresaToDelete
    if (showDeleteConfirmDialog && currentEmpresaToDelete != null) {
        ConfirmDeleteDialog(
            empresa = currentEmpresaToDelete,
            onConfirm = { viewModel.confirmDeleteEmpresa() },
            onDismiss = { viewModel.closeDeleteConfirmation() }
        )
    }
}
