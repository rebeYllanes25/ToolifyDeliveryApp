package com.cibertec.proyectodami.domain.model.dtos
import android.os.Parcelable
import com.cibertec.proyectodami.domain.model.entities.Producto
import kotlinx.parcelize.Parcelize

@Parcelize
data class CarroItem(
    val producto: Producto,
    var cantidad: Int = 1,
    val fechaAgregado: Long = System.currentTimeMillis()
) : Parcelable {
    val subtotal: Double
        get() = producto.precio * cantidad

    fun incrementar(): Boolean {
        return if (cantidad < producto.stock) {
            cantidad++
            true
        } else {
            false
        }
    }

    fun decrementar(): Boolean {
        return if (cantidad > 1) {
            cantidad--
            true
        } else {
            false
        }
    }
}