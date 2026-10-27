-- =========================================================
--  Sistema de Inventario y Ventas - Ferretería
--  Script completo de la base de datos (MySQL 8)
--  Se puede ejecutar varias veces: no borra datos existentes.
-- =========================================================

CREATE DATABASE IF NOT EXISTS ferreteria
    CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE ferreteria;

-- ---------------------------------------------------------
--  Catálogos
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS marca (
    id_marca INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------
--  Personas (Persona es abstracta: una tabla por cada hija)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    celular VARCHAR(15),
    limite_credito DECIMAL(12,2) NOT NULL DEFAULT 0,
    dias_credito INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (limite_credito >= 0),
    CHECK (dias_credito >= 0)
);

CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    celular VARCHAR(15),
    nit VARCHAR(15) NOT NULL UNIQUE,
    empresa VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    celular VARCHAR(15),
    nombre_usuario VARCHAR(40) NOT NULL UNIQUE,
    contrasena CHAR(64) NOT NULL,              -- SHA-256 en hexadecimal
    rol ENUM('ADMINISTRADOR', 'VENDEDOR') NOT NULL DEFAULT 'VENDEDOR',
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------
--  Productos y kits
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    unidad VARCHAR(20) NOT NULL,
    presentacion VARCHAR(100),
    id_marca INT NULL,
    id_categoria INT NULL,
    precio_compra DECIMAL(12,2) NOT NULL DEFAULT 0,
    precio_venta DECIMAL(12,2) NOT NULL DEFAULT 0,
    cantidad_actual DECIMAL(12,3) NOT NULL DEFAULT 0,
    existencia_minima DECIMAL(12,3) NOT NULL DEFAULT 1,
    foto VARCHAR(260),
    es_kit BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_marca FOREIGN KEY (id_marca) REFERENCES marca(id_marca),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria),
    CHECK (precio_compra >= 0),
    CHECK (precio_venta >= 0),
    CHECK (cantidad_actual >= 0),
    CHECK (existencia_minima > 0)
);

CREATE TABLE IF NOT EXISTS componente_kit (
    id_componente_kit INT AUTO_INCREMENT PRIMARY KEY,
    id_kit INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad_piezas INT NOT NULL,
    peso DECIMAL(10,4) NOT NULL DEFAULT 1,
    CONSTRAINT fk_componente_kit FOREIGN KEY (id_kit) REFERENCES producto(id_producto) ON DELETE CASCADE,
    CONSTRAINT fk_componente_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT uq_componente UNIQUE (id_kit, id_producto),
    CHECK (cantidad_piezas > 0),
    CHECK (peso > 0)
);

-- ---------------------------------------------------------
--  Ventas y créditos
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    id_cliente INT NULL,                       -- NULL = consumidor final
    id_usuario INT NOT NULL,
    es_credito BOOLEAN NOT NULL DEFAULT FALSE,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    anulada BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad DECIMAL(12,3) NOT NULL,
    precio_final DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detventa_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_detventa_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CHECK (cantidad > 0),
    CHECK (precio_final >= 0)
);

CREATE TABLE IF NOT EXISTS credito (
    id_credito INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL UNIQUE,
    fecha_vencimiento DATE NOT NULL,
    CONSTRAINT fk_credito_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS abono (
    id_abono INT AUTO_INCREMENT PRIMARY KEY,
    id_credito INT NOT NULL,
    fecha DATETIME NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    id_usuario INT NOT NULL,
    CONSTRAINT fk_abono_credito FOREIGN KEY (id_credito) REFERENCES credito(id_credito) ON DELETE CASCADE,
    CONSTRAINT fk_abono_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CHECK (monto > 0)
);

-- ---------------------------------------------------------
--  Compras
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS compra (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    id_proveedor INT NOT NULL,
    id_usuario INT NOT NULL,
    es_credito BOOLEAN NOT NULL DEFAULT FALSE,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_compra_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor),
    CONSTRAINT fk_compra_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS detalle_compra (
    id_detalle_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad DECIMAL(12,3) NOT NULL,
    precio_costo DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detcompra_compra FOREIGN KEY (id_compra) REFERENCES compra(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_detcompra_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CHECK (cantidad > 0),
    CHECK (precio_costo >= 0)
);

-- ---------------------------------------------------------
--  Bajas de inventario (pérdidas, daños, vencidos)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS baja_inventario (
    id_baja INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    cantidad DECIMAL(12,3) NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    id_usuario INT NOT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_baja_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT fk_baja_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CHECK (cantidad > 0)
);

-- ---------------------------------------------------------
--  Usuario administrador inicial
--  usuario: admin   contraseña: admin123  (cámbiela después)
-- ---------------------------------------------------------
INSERT IGNORE INTO usuario (nombre, nombre_usuario, contrasena, rol)
VALUES ('Administrador', 'admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'ADMINISTRADOR');
