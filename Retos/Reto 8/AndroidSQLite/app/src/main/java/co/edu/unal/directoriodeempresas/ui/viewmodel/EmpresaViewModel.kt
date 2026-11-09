package co.edu.unal.directoriodeempresas.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.directoriodeempresas.model.Empresa
import co.edu.unal.directoriodeempresas.repository.EmpresaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmpresaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmpresaRepository(application)

    private val _empresas = MutableStateFlow<List<Empresa>>(emptyList())
    val empresas: StateFlow<List<Empresa>> = _empresas.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedClasificacion = MutableStateFlow("Todas")
    val selectedClasificacion: StateFlow<String> = _selectedClasificacion.asStateFlow()

    private val _showFormDialog = MutableStateFlow(false)
    val showFormDialog: StateFlow<Boolean> = _showFormDialog.asStateFlow()

    private val _empresaToEdit = MutableStateFlow<Empresa?>(null)
    val empresaToEdit: StateFlow<Empresa?> = _empresaToEdit.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    private val _empresaToDelete = MutableStateFlow<Empresa?>(null)
    val empresaToDelete: StateFlow<Empresa?> = _empresaToDelete.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        loadEmpresas()
    }

    fun loadEmpresas() {
        viewModelScope.launch {
            _empresas.value = repository.getAllEmpresas(
                filterNombre = _searchQuery.value,
                filterClasificacion = _selectedClasificacion.value
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        loadEmpresas()
    }

    fun onClasificacionFilterChanged(clasificacion: String) {
        _selectedClasificacion.value = clasificacion
        loadEmpresas()
    }

    fun openAddDialog() {
        _empresaToEdit.value = null
        _showFormDialog.value = true
    }

    fun openEditDialog(empresa: Empresa) {
        _empresaToEdit.value = empresa
        _showFormDialog.value = true
    }

    fun closeFormDialog() {
        _showFormDialog.value = false
        _empresaToEdit.value = null
    }

    fun saveEmpresa(
        nombre: String,
        urlWeb: String,
        telefono: String,
        email: String,
        servicios: String,
        clasificaciones: List<String>
    ) {
        val clasificacionStr = clasificaciones.joinToString(", ")
        val currentToEdit = _empresaToEdit.value

        viewModelScope.launch {
            if (currentToEdit == null) {
                // Create
                val newEmpresa = Empresa(
                    nombre = nombre.trim(),
                    urlWeb = urlWeb.trim(),
                    telefono = telefono.trim(),
                    email = email.trim(),
                    servicios = servicios.trim(),
                    clasificacion = clasificacionStr
                )
                val id = repository.insertEmpresa(newEmpresa)
                if (id > 0) {
                    _userMessage.value = "Empresa '$nombre' creada exitosamente"
                } else {
                    _userMessage.value = "Error al crear la empresa"
                }
            } else {
                // Update
                val updatedEmpresa = currentToEdit.copy(
                    nombre = nombre.trim(),
                    urlWeb = urlWeb.trim(),
                    telefono = telefono.trim(),
                    email = email.trim(),
                    servicios = servicios.trim(),
                    clasificacion = clasificacionStr
                )
                val rows = repository.updateEmpresa(updatedEmpresa)
                if (rows > 0) {
                    _userMessage.value = "Empresa '$nombre' actualizada exitosamente"
                } else {
                    _userMessage.value = "Error al actualizar la empresa"
                }
            }
            closeFormDialog()
            loadEmpresas()
        }
    }

    fun openDeleteConfirmation(empresa: Empresa) {
        _empresaToDelete.value = empresa
        _showDeleteConfirmDialog.value = true
    }

    fun closeDeleteConfirmation() {
        _showDeleteConfirmDialog.value = false
        _empresaToDelete.value = null
    }

    fun confirmDeleteEmpresa() {
        val empresa = _empresaToDelete.value ?: return
        viewModelScope.launch {
            val rows = repository.deleteEmpresa(empresa.id)
            if (rows > 0) {
                _userMessage.value = "Empresa '${empresa.nombre}' eliminada"
            } else {
                _userMessage.value = "Error al eliminar la empresa"
            }
            closeDeleteConfirmation()
            loadEmpresas()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
