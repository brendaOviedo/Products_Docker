SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS inventario_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE inventario_db;

DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS categorias;

CREATE TABLE categorias (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    estado         VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_categorias PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nombre UNIQUE (nombre),
    CONSTRAINT ck_categorias_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE productos (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    categoria_id   BIGINT        NOT NULL,
    nombre         VARCHAR(150)  NOT NULL,
    precio         DECIMAL(10,2) NOT NULL,
    stock          INT           NOT NULL DEFAULT 0,
    estado         VARCHAR(10)   NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_productos PRIMARY KEY (id),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_productos_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT ck_productos_precio CHECK (precio > 0),
    CONSTRAINT ck_productos_stock CHECK (stock >= 0),
    CONSTRAINT ck_productos_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_productos_nombre ON productos (nombre);
CREATE INDEX idx_productos_estado ON productos (estado);

INSERT INTO categorias (nombre, estado) VALUES
    ('Electrónica', 'ACTIVO'),
    ('Ropa', 'ACTIVO'),
    ('Alimentos', 'ACTIVO'),
    ('Hogar', 'ACTIVO'),
    ('Juguetes', 'INACTIVO');

INSERT INTO productos (categoria_id, nombre, precio, stock, estado) VALUES
    (1, 'Laptop Lenovo IdeaPad 15"', 5899.00, 12, 'ACTIVO'),
    (1, 'Mouse inalámbrico Logitech M185', 145.50, 4, 'ACTIVO'),
    (1, 'Audífonos Bluetooth JBL Tune 510', 399.99, 0, 'ACTIVO'),
    (1, 'Monitor Samsung 24" Full HD', 1250.00, 7, 'ACTIVO'),
    (2, 'Camisa formal manga larga', 189.00, 25, 'ACTIVO'),
    (2, 'Pantalón de lona azul', 245.00, 5, 'ACTIVO'),
    (2, 'Chaqueta impermeable', 520.00, 3, 'INACTIVO'),
    (3, 'Café molido 1 lb', 65.00, 40, 'ACTIVO'),
    (3, 'Arroz blanco 5 lb', 38.75, 2, 'ACTIVO'),
    (3, 'Aceite de oliva 500 ml', 89.90, 15, 'ACTIVO'),
    (4, 'Juego de sartenes antiadherentes', 349.00, 6, 'ACTIVO'),
    (4, 'Lámpara de escritorio LED', 159.00, 1, 'ACTIVO'),
    (5, 'Rompecabezas 1000 piezas', 120.00, 8, 'INACTIVO');
