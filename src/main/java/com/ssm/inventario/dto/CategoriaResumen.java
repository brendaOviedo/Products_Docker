package com.ssm.inventario.dto;

import com.ssm.inventario.model.Categoria;

public record CategoriaResumen(Categoria categoria, long productosActivos, long productosTotales) {

    public boolean puedeInactivarse() {
        return categoria.isActiva() && productosActivos == 0;
    }
}
