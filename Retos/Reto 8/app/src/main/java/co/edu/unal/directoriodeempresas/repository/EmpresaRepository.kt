package co.edu.unal.directoriodeempresas.repository

import android.content.Context
import co.edu.unal.directoriodeempresas.db.EmpresaDatabaseHelper
import co.edu.unal.directoriodeempresas.model.Empresa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmpresaRepository(context: Context) {
    private val dbHelper = EmpresaDatabaseHelper(context.applicationContext)

    suspend fun getAllEmpresas(filterNombre: String? = null, filterClasificacion: String? = null): List<Empresa> {
        return withContext(Dispatchers.IO) {
            dbHelper.getAllEmpresas(filterNombre, filterClasificacion)
        }
    }

    suspend fun getEmpresaById(id: Long): Empresa? {
        return withContext(Dispatchers.IO) {
            dbHelper.getEmpresaById(id)
        }
    }

    suspend fun insertEmpresa(empresa: Empresa): Long {
        return withContext(Dispatchers.IO) {
            dbHelper.insertEmpresa(empresa)
        }
    }

    suspend fun updateEmpresa(empresa: Empresa): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.updateEmpresa(empresa)
        }
    }

    suspend fun deleteEmpresa(id: Long): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.deleteEmpresa(id)
        }
    }
}
