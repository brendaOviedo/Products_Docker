package com.ssm.inventario.model;

public enum Estado {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
