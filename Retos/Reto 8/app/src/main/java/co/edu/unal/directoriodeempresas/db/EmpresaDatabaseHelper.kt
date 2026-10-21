package co.edu.unal.directoriodeempresas.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import co.edu.unal.directoriodeempresas.model.Empresa

class EmpresaDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "directorio_empresas.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_EMPRESAS = "empresas"
        const val COLUMN_ID = "_id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_URL_WEB = "url_web"
        const val COLUMN_TELEFONO = "telefono"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_SERVICIOS = "servicios"
        const val COLUMN_CLASIFICACION = "clasificacion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_EMPRESAS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_URL_WEB TEXT,
                $COLUMN_TELEFONO TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_SERVICIOS TEXT,
                $COLUMN_CLASIFICACION TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPRESAS")
        onCreate(db)
    }

    private fun insertInitialData(db: SQLiteDatabase) {
        val sampleEmpresas = listOf(
            Empresa(
                nombre = "SoftDev Solutions",
                urlWeb = "https://www.softdev.com.co",
                telefono = "+57 300 123 4567",
                email = "contacto@softdev.com.co",
                servicios = "Desarrollo de software a la medida, Aplicaciones Móviles, Arquitectura Cloud",
                clasificacion = "Consultoría, Desarrollo a la medida"
            ),
            Empresa(
                nombre = "FabriCode Labs",
                urlWeb = "https://www.fabricode.io",
                telefono = "+57 310 987 6543",
                email = "info@fabricode.io",
                servicios = "Fábrica de software, QA & Testing automatizado, Integración Continua",
                clasificacion = "Fábrica de software"
            ),
            Empresa(
                nombre = "IT Strategy & Consulting",
                urlWeb = "https://www.itstrategy.com",
                telefono = "+57 601 555 0199",
                email = "ventas@itstrategy.com",
                servicios = "Auditoría de sistemas, Transformación digital, Ciberseguridad",
                clasificacion = "Consultoría"
            ),
            Empresa(
                nombre = "Nexus Tech & Factory",
                urlWeb = "https://www.nexustech.co",
                telefono = "+57 315 444 8822",
                email = "contacto@nexustech.co",
                servicios = "Fábrica de código, Apps a la medida, Mantenimiento de software legacy",
                clasificacion = "Desarrollo a la medida, Fábrica de software"
            )
        )

        for (empresa in sampleEmpresas) {
            val values = ContentValues().apply {
                put(COLUMN_NOMBRE, empresa.nombre)
                put(COLUMN_URL_WEB, empresa.urlWeb)
                put(COLUMN_TELEFONO, empresa.telefono)
                put(COLUMN_EMAIL, empresa.email)
                put(COLUMN_SERVICIOS, empresa.servicios)
                put(COLUMN_CLASIFICACION, empresa.clasificacion)
            }
            db.insert(TABLE_EMPRESAS, null, values)
        }
    }

    fun insertEmpresa(empresa: Empresa): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre)
            put(COLUMN_URL_WEB, empresa.urlWeb)
            put(COLUMN_TELEFONO, empresa.telefono)
            put(COLUMN_EMAIL, empresa.email)
            put(COLUMN_SERVICIOS, empresa.servicios)
            put(COLUMN_CLASIFICACION, empresa.clasificacion)
        }
        val id = db.insert(TABLE_EMPRESAS, null, values)
        return id
    }

    fun getAllEmpresas(filterNombre: String? = null, filterClasificacion: String? = null): List<Empresa> {
        val list = mutableListOf<Empresa>()
        val db = readableDatabase

        val selectionClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (!filterNombre.isNullOrBlank()) {
            selectionClauses.add("$COLUMN_NOMBRE LIKE ?")
            selectionArgs.add("%${filterNombre.trim()}%")
        }

        if (!filterClasificacion.isNullOrBlank() && filterClasificacion != "Todas") {
            selectionClauses.add("$COLUMN_CLASIFICACION LIKE ?")
            selectionArgs.add("%${filterClasificacion.trim()}%")
        }

        val selection = if (selectionClauses.isNotEmpty()) {
            selectionClauses.joinToString(" AND ")
        } else null

        val args = if (selectionArgs.isNotEmpty()) {
            selectionArgs.toTypedArray()
        } else null

        val cursor = db.query(
            TABLE_EMPRESAS,
            null,
            selection,
            args,
            null,
            null,
            "$COLUMN_NOMBRE ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                    val nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE))
                    val urlWeb = it.getString(it.getColumnIndexOrThrow(COLUMN_URL_WEB)) ?: ""
                    val telefono = it.getString(it.getColumnIndexOrThrow(COLUMN_TELEFONO)) ?: ""
                    val email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: ""
                    val servicios = it.getString(it.getColumnIndexOrThrow(COLUMN_SERVICIOS)) ?: ""
                    val clasificacion = it.getString(it.getColumnIndexOrThrow(COLUMN_CLASIFICACION)) ?: ""

                    list.add(
                        Empresa(
                            id = id,
                            nombre = nombre,
                            urlWeb = urlWeb,
                            telefono = telefono,
                            email = email,
                            servicios = servicios,
                            clasificacion = clasificacion
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun getEmpresaById(id: Long): Empresa? {
        val db = readableDatabase
        var empresa: Empresa? = null
        val cursor = db.query(
            TABLE_EMPRESAS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val urlWeb = it.getString(it.getColumnIndexOrThrow(COLUMN_URL_WEB)) ?: ""
                val telefono = it.getString(it.getColumnIndexOrThrow(COLUMN_TELEFONO)) ?: ""
                val email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: ""
                val servicios = it.getString(it.getColumnIndexOrThrow(COLUMN_SERVICIOS)) ?: ""
                val clasificacion = it.getString(it.getColumnIndexOrThrow(COLUMN_CLASIFICACION)) ?: ""

                empresa = Empresa(id, nombre, urlWeb, telefono, email, servicios, clasificacion)
            }
        }
        return empresa
    }

    fun updateEmpresa(empresa: Empresa): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre)
            put(COLUMN_URL_WEB, empresa.urlWeb)
            put(COLUMN_TELEFONO, empresa.telefono)
            put(COLUMN_EMAIL, empresa.email)
            put(COLUMN_SERVICIOS, empresa.servicios)
            put(COLUMN_CLASIFICACION, empresa.clasificacion)
        }
        return db.update(
            TABLE_EMPRESAS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(empresa.id.toString())
        )
    }

    fun deleteEmpresa(id: Long): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_EMPRESAS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }
}
