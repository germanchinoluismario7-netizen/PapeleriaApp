package com.example.papeleriaapp.core.model

/**
 * Categorias usadas en la app de papeleria:
 * - storeCategories: para el registro de la papeleria (vendedor).
 * - productCategories: para filtrar el catalogo de productos (cliente).
 */
object StationeryCategories {

    val storeCategories : List<String> = listOf(
        "Papelería escolar",
        "Papelería de oficina",
        "Librería",
        "Arte y manualidades",
        "Útiles escolares",
        "Regalos y decoración"
    )

    val productCategories : List<String> = listOf(
        "Todos",
        "Escritura",
        "Cuadernos y libretas",
        "Útiles escolares",
        "Arte y manualidades",
        "Libros y revistas",
        "Oficina",
        "Regalos"
    )
}
