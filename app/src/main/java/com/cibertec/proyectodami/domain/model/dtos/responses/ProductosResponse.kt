package com.cibertec.proyectodami.domain.model.dtos.responses

import com.cibertec.proyectodami.domain.model.entities.Categoria
import com.cibertec.proyectodami.domain.model.entities.Producto

data class ProductosResponse(
    val productos: PageResponse<Producto>,
    val categorias: List<Categoria>
)

data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)