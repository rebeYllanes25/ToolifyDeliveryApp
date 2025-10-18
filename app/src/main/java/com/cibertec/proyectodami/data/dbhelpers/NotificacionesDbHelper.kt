package com.cibertec.proyectodami.data.dbhelpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotificacionesDbHelper(context: Context) :
    SQLiteOpenHelper(context, "notificaciones.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE notificaciones(
                id TEXT PRIMARY KEY,
                tipo TEXT NOT NULL,
                titulo TEXT NOT NULL,
                mensaje TEXT NOT NULL,
                fecha_creacion INTEGER NOT NULL,
                leida INTEGER NOT NULL DEFAULT 0,
                pedido_id TEXT,
                datos TEXT
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX idx_notif_fecha ON notificaciones(fecha_creacion DESC)")
        db.execSQL("CREATE INDEX idx_notif_leida ON notificaciones(leida)")
        db.execSQL("CREATE INDEX idx_notif_pedido ON notificaciones(pedido_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        const val TABLE_NAME = "notificaciones"
        const val COLUMN_ID = "id"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_TITULO = "titulo"
        const val COLUMN_MENSAJE = "mensaje"
        const val COLUMN_FECHA_CREACION = "fecha_creacion"
        const val COLUMN_LEIDA = "leida"
        const val COLUMN_PEDIDO_ID = "pedido_id"
        const val COLUMN_DATOS = "datos"
    }
}