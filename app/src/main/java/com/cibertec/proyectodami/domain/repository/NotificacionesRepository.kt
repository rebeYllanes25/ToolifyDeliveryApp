package com.cibertec.proyectodami.domain.repository

import android.content.ContentValues
import android.content.Context
import com.cibertec.proyectodami.data.dbhelpers.NotificacionesDbHelper
import com.cibertec.proyectodami.domain.model.entities.Notificacion
import com.cibertec.proyectodami.domain.model.enums.TipoNotificacion
import java.util.Date

class NotificacionesRepository(context: Context) {

    private val helper = NotificacionesDbHelper(context)

    fun insertar(notificacion: Notificacion) {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("id", notificacion.id)
            put("tipo", notificacion.tipo.name)
            put("titulo", notificacion.titulo)
            put("mensaje", notificacion.mensaje)
            put("fecha_creacion", notificacion.fechaCreacion.time)
            put("leida", if (notificacion.leida) 1 else 0)
            put("pedido_id", notificacion.pedidoId)
            put("datos", notificacion.datos)
        }
        db.insert("notificaciones", null, cv)
        db.close()
    }

    fun obtenerTodas(): List<Notificacion> {
        val db = helper.readableDatabase
        val items = mutableListOf<Notificacion>()

        val c = db.query(
            "notificaciones",
            arrayOf("id", "tipo", "titulo", "mensaje", "fecha_creacion", "leida", "pedido_id", "datos"),
            null, null, null, null, "fecha_creacion DESC"
        )

        c.use {
            while (it.moveToNext()) {
                items.add(
                    Notificacion(
                        id = it.getString(0),
                        tipo = TipoNotificacion.valueOf(it.getString(1)),
                        titulo = it.getString(2),
                        mensaje = it.getString(3),
                        fechaCreacion = Date(it.getLong(4)),
                        leida = it.getInt(5) == 1,
                        pedidoId = it.getString(6),
                        datos = it.getString(7)
                    )
                )
            }
        }
        db.close()
        return items
    }

    fun obtenerNoLeidas(): List<Notificacion> {
        val db = helper.readableDatabase
        val items = mutableListOf<Notificacion>()

        val c = db.query(
            "notificaciones",
            arrayOf("id", "tipo", "titulo", "mensaje", "fecha_creacion", "leida", "pedido_id", "datos"),
            "leida = ?", arrayOf("0"),
            null, null, "fecha_creacion DESC"
        )

        c.use {
            while (it.moveToNext()) {
                items.add(
                    Notificacion(
                        id = it.getString(0),
                        tipo = TipoNotificacion.valueOf(it.getString(1)),
                        titulo = it.getString(2),
                        mensaje = it.getString(3),
                        fechaCreacion = Date(it.getLong(4)),
                        leida = it.getInt(5) == 1,
                        pedidoId = it.getString(6),
                        datos = it.getString(7)
                    )
                )
            }
        }
        db.close()
        return items
    }

    fun obtenerLeidas(): List<Notificacion> {
        val db = helper.readableDatabase
        val items = mutableListOf<Notificacion>()

        val c = db.query(
            "notificaciones",
            arrayOf("id", "tipo", "titulo", "mensaje", "fecha_creacion", "leida", "pedido_id", "datos"),
            "leida = ?", arrayOf("1"),
            null, null, "fecha_creacion DESC"
        )

        c.use {
            while (it.moveToNext()) {
                items.add(
                    Notificacion(
                        id = it.getString(0),
                        tipo = TipoNotificacion.valueOf(it.getString(1)),
                        titulo = it.getString(2),
                        mensaje = it.getString(3),
                        fechaCreacion = Date(it.getLong(4)),
                        leida = it.getInt(5) == 1,
                        pedidoId = it.getString(6),
                        datos = it.getString(7)
                    )
                )
            }
        }
        db.close()
        return items
    }

    fun contarNoLeidas(): Int {
        val db = helper.readableDatabase
        val c = db.rawQuery(
            "SELECT COUNT(*) FROM notificaciones WHERE leida = 0",
            null
        )
        c.moveToFirst()
        val total = c.getInt(0)
        c.close()
        db.close()
        return total
    }

    fun actualizar(notificacion: Notificacion): Int {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("tipo", notificacion.tipo.name)
            put("titulo", notificacion.titulo)
            put("mensaje", notificacion.mensaje)
            put("fecha_creacion", notificacion.fechaCreacion.time)
            put("leida", if (notificacion.leida) 1 else 0)
            put("pedido_id", notificacion.pedidoId)
            put("datos", notificacion.datos)
        }
        val rows = db.update("notificaciones", cv, "id = ?", arrayOf(notificacion.id))
        db.close()
        return rows
    }

    fun marcarComoLeida(id: String, leida: Boolean): Int {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("leida", if (leida) 1 else 0)
        }
        val rows = db.update("notificaciones", cv, "id = ?", arrayOf(id))
        db.close()
        return rows
    }

    fun marcarTodasComoLeidas(): Int {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("leida", 1)
        }
        val rows = db.update("notificaciones", cv, null, null)
        db.close()
        return rows
    }

    fun eliminar(id: String): Int {
        val db = helper.writableDatabase
        val rows = db.delete("notificaciones", "id = ?", arrayOf(id))
        db.close()
        return rows
    }

    fun eliminarTodas(): Int {
        val db = helper.writableDatabase
        val rows = db.delete("notificaciones", null, null)
        db.close()
        return rows
    }

    fun eliminarLeidas(): Int {
        val db = helper.writableDatabase
        val rows = db.delete("notificaciones", "leida = ?", arrayOf("1"))
        db.close()
        return rows
    }

    fun buscarPorId(id: String): Notificacion? {
        val db = helper.readableDatabase
        val c = db.query(
            "notificaciones",
            arrayOf("id", "tipo", "titulo", "mensaje", "fecha_creacion", "leida", "pedido_id", "datos"),
            "id = ?", arrayOf(id),
            null, null, null
        )

        var notificacion: Notificacion? = null
        c.use {
            if (it.moveToFirst()) {
                notificacion = Notificacion(
                    id = it.getString(0),
                    tipo = TipoNotificacion.valueOf(it.getString(1)),
                    titulo = it.getString(2),
                    mensaje = it.getString(3),
                    fechaCreacion = Date(it.getLong(4)),
                    leida = it.getInt(5) == 1,
                    pedidoId = it.getString(6),
                    datos = it.getString(7)
                )
            }
        }
        db.close()
        return notificacion
    }

    fun obtenerPorPedido(pedidoId: String): List<Notificacion> {
        val db = helper.readableDatabase
        val items = mutableListOf<Notificacion>()

        val c = db.query(
            "notificaciones",
            arrayOf("id", "tipo", "titulo", "mensaje", "fecha_creacion", "leida", "pedido_id", "datos"),
            "pedido_id = ?", arrayOf(pedidoId),
            null, null, "fecha_creacion DESC"
        )

        c.use {
            while (it.moveToNext()) {
                items.add(
                    Notificacion(
                        id = it.getString(0),
                        tipo = TipoNotificacion.valueOf(it.getString(1)),
                        titulo = it.getString(2),
                        mensaje = it.getString(3),
                        fechaCreacion = Date(it.getLong(4)),
                        leida = it.getInt(5) == 1,
                        pedidoId = it.getString(6),
                        datos = it.getString(7)
                    )
                )
            }
        }
        db.close()
        return items
    }

    fun count(): Int {
        val db = helper.readableDatabase
        val c = db.rawQuery(
            "SELECT COUNT(*) FROM notificaciones",
            null
        )
        c.moveToFirst()
        val total = c.getInt(0)
        c.close()
        db.close()
        return total
    }
}