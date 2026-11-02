SET NAMES utf8mb4;


CREATE DATABASE IF NOT EXISTS ferreteria
    CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE ferreteria;

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


INSERT IGNORE INTO usuario (nombre, nombre_usuario, contrasena, rol)
VALUES ('Administrador', 'admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'ADMINISTRADOR');


INSERT IGNORE INTO `categoria` (`id_categoria`, `nombre`, `descripcion`, `activa`) VALUES (1,'Herramientas','Herramientas manuales',1),
(2,'Tornillería','Tornillos, clavos y tuercas',1),
(3,'Plomería','Tubos y accesorios',1),
(4,'Construcción','Cemento, block, hierro',1),
(5,'Electricidad','Cables, tomas, focos',1);
INSERT IGNORE INTO `marca` (`id_marca`, `nombre`, `descripcion`, `activa`) VALUES (1,'Truper',NULL,1),
(2,'Amanco',NULL,1),
(3,'Stanley',NULL,1),
(4,'Pretul','Herramientas económicas de la línea Truper',1);
INSERT IGNORE INTO `cliente` (`id_cliente`, `nombre`, `direccion`, `celular`, `limite_credito`, `dias_credito`, `activo`) VALUES (1,'Pedro Ramírez','San Pedro Sac.','3333-2222',1000.00,30,1),
(2,'Construcciones López','San Marcos','7760-1122',5000.00,30,1),
(3,'María Fuentes','Esquipulas Palo Gordo','5512-3434',500.00,15,1),
(4,'Carlos Bautista',NULL,'4123-9876',0.00,0,1),
(5,'Ana Gómez','San Pedro Sacatepéquez','5544-1122',800.00,15,1),
(6,'Luis Barrios','Aldea San Isidro, San Pedro Sac.','4789-3355',600.00,15,0);
INSERT IGNORE INTO `proveedor` (`id_proveedor`, `nombre`, `direccion`, `celular`, `nit`, `empresa`, `activo`) VALUES (1,'Juan Orozco','Zona 1, San Marcos','5555-4444','1234567-8','Distribuidora El Martillo',1),
(2,'Ana Pérez','Quetzaltenango','4444-3333','7654321-0','Ferremayoreo Xela',1);
INSERT IGNORE INTO `usuario` (`id_usuario`, `nombre`, `direccion`, `celular`, `nombre_usuario`, `contrasena`, `rol`, `activo`) VALUES (1,'Administrador',NULL,NULL,'admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','ADMINISTRADOR',1),
(2,'María López',NULL,'4455-6677','maria','626e3c805e77eeb472c42c6be607be2af7ac5c08fd7050f278e0330fe81abf57','VENDEDOR',1);
INSERT IGNORE INTO `producto` (`id_producto`, `codigo`, `nombre`, `unidad`, `presentacion`, `id_marca`, `id_categoria`, `precio_compra`, `precio_venta`, `cantidad_actual`, `existencia_minima`, `foto`, `es_kit`, `activo`) VALUES (1,'MAR-16','Martillo uña','unidad','16 oz',1,1,62.00,85.00,12.000,3.000,NULL,0,1),
(2,'CLV-2','Clavo','libra','2 pulgadas',NULL,2,7.90,12.00,26.000,5.000,NULL,0,1),
(3,'TOR-14','Tornillo goloso','unidad','1/4 x 1\"',NULL,2,0.45,1.00,300.000,50.000,NULL,0,1),
(4,'PVC-12','Tubo PVC','unidad','1/2\" x 3 m',2,3,18.75,28.00,36.000,12.000,NULL,0,1),
(5,'CEM-42','Cemento gris','saco','42.5 kg',NULL,4,77.50,92.00,30.000,10.000,NULL,0,1),
(6,'DES-PH2','Desarmador Phillips','unidad','#2',3,1,18.00,30.00,18.000,5.000,NULL,0,1),
(7,'CIN-5M','Cinta métrica','unidad','5 m',1,1,35.00,55.00,6.000,8.000,NULL,0,1),
(8,'CAB-12','Cable THHN','metro','calibre 12',NULL,5,4.20,6.50,275.000,50.000,NULL,0,1),
(9,'TEF-12','Cinta teflón','unidad','1/2\"',NULL,3,2.50,5.00,36.000,40.000,NULL,0,1),
(10,'LIJ-100','Lija de agua','unidad','grano 100',1,1,3.00,5.00,53.000,20.000,NULL,0,1),
(11,'KIT-FIJ','Kit de fijación','caja','100 piezas',1,2,60.00,95.00,4.000,5.000,NULL,1,1),
(30,'7501031311309','Brocha','unidad','2 pulgadas',4,NULL,12.50,20.00,15.000,3.000,NULL,0,1),
(31,'LLS-14','Llave stilson','unidad','14 pulgadas',1,1,95.00,150.00,8.000,2.000,NULL,0,0);
INSERT IGNORE INTO `componente_kit` (`id_componente_kit`, `id_kit`, `id_producto`, `cantidad_piezas`, `peso`) VALUES (1,11,3,80,3.0000),
(2,11,2,2,1.0000);
INSERT IGNORE INTO `venta` (`id_venta`, `fecha`, `id_cliente`, `id_usuario`, `es_credito`, `total`, `anulada`) VALUES (1,'2026-10-26 00:20:30',NULL,2,0,121.00,0),
(2,'2026-10-26 00:20:30',2,1,1,1518.00,0),
(3,'2026-09-16 00:20:30',1,2,1,150.00,0),
(4,'2026-10-26 00:20:30',NULL,2,0,182.50,0),
(5,'2026-10-26 00:23:57',NULL,2,0,48.00,0),
(6,'2026-10-26 00:25:06',NULL,2,0,48.00,0),
(7,'2026-11-01 19:30:34',NULL,1,0,177.00,0),
(8,'2026-11-01 19:30:39',5,1,1,115.00,0),
(9,'2026-11-01 22:35:37',NULL,1,0,80.00,0);
INSERT IGNORE INTO `detalle_venta` (`id_detalle_venta`, `id_venta`, `id_producto`, `cantidad`, `precio_final`) VALUES (1,1,1,1.000,85.00),
(2,1,2,3.000,12.00),
(3,2,5,15.000,90.00),
(4,2,4,6.000,28.00),
(5,3,7,2.000,55.00),
(6,3,3,100.000,0.40),
(7,4,8,25.000,6.50),
(8,4,9,4.000,5.00),
(9,5,2,4.000,12.00),
(10,6,2,4.000,12.00),
(11,7,1,1.000,85.00),
(12,7,2,3.000,12.00),
(13,7,4,2.000,28.00),
(14,8,6,2.000,30.00),
(15,8,7,1.000,55.00),
(16,9,6,2.000,30.00),
(17,9,10,4.000,5.00);
INSERT IGNORE INTO `credito` (`id_credito`, `id_venta`, `fecha_vencimiento`) VALUES (1,2,'2026-11-25'),
(2,3,'2026-10-16'),
(3,8,'2026-11-16');
INSERT IGNORE INTO `abono` (`id_abono`, `id_credito`, `fecha`, `monto`, `id_usuario`) VALUES (1,2,'2026-10-26 00:20:30',50.00,1),
(2,2,'2026-10-26 02:10:32',25.00,2),
(3,2,'2026-10-26 02:23:25',10.00,1),
(4,2,'2026-10-26 02:25:20',10.00,1),
(5,2,'2026-10-26 15:19:41',10.00,1),
(6,2,'2026-10-26 15:20:17',10.00,1),
(7,2,'2026-10-26 23:32:49',10.00,1),
(8,2,'2026-10-26 23:33:24',10.00,1),
(9,3,'2026-11-01 19:30:43',50.00,1),
(14,3,'2026-11-01 19:38:28',25.00,1);
INSERT IGNORE INTO `compra` (`id_compra`, `fecha`, `id_proveedor`, `id_usuario`, `es_credito`, `total`) VALUES (1,'2026-10-26 00:20:30',1,1,0,6124.00),
(2,'2026-10-26 00:20:30',2,1,1,79.00),
(3,'2026-11-01 22:35:45',1,1,0,2000.00);
INSERT IGNORE INTO `detalle_compra` (`id_detalle_compra`, `id_compra`, `id_producto`, `cantidad`, `precio_costo`) VALUES (1,1,1,14.000,62.00),
(2,1,2,30.000,8.50),
(3,1,3,400.000,0.45),
(4,1,4,20.000,19.00),
(5,1,5,25.000,78.00),
(6,1,6,22.000,18.00),
(7,1,7,9.000,35.00),
(8,1,8,300.000,4.20),
(9,1,9,40.000,2.50),
(10,1,10,60.000,3.00),
(11,1,11,4.000,60.00),
(12,2,2,10.000,7.90),
(13,3,5,20.000,77.50),
(14,3,4,24.000,18.75);
INSERT IGNORE INTO `baja_inventario` (`id_baja`, `id_producto`, `cantidad`, `motivo`, `id_usuario`, `fecha`) VALUES (1,10,3.000,'Producto dañado',1,'2026-10-26 00:20:30');
