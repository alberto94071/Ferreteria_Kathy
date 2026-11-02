# Sistema de Inventario y Ventas · Ferretería Kathy

Proyecto final del curso **Programación II**
Universidad Mariano Gálvez de Guatemala, sede San Pedro Sacatepéquez, San Marcos
Ingeniería en Sistemas de Información, sección "B"
Catedrático: Ing. Bilder Fuentes

Programa de escritorio en **Java (Swing)** con base de datos **MySQL** para controlar el inventario, las ventas al contado y al crédito, las compras, los abonos y los reportes de una ferretería real. Genera el comprobante de venta y el reporte de bajo stock en **PDF**, y además trae una **app Android** que se conecta al programa por el Wi-Fi de la tienda.

## Enlaces

| Recurso | Enlace |
| --- | --- |
| Video del recorrido del sistema | https://drive.google.com/file/d/1ZB6pAnYs-8sMwZHq7Co2MV7wXekgvnck/view?usp=sharing |
| Carpeta con la documentación | https://drive.google.com/drive/folders/1HhfAhqTyGByWMeqfvk2N7Lty98vYcawm?usp=sharing |
| PDF de ejemplo generados por el sistema | En la misma carpeta, dentro de **reportes pdf** (ComprobanteVenta.pdf y ReporteBajoStock.pdf) |
| Script de la base de datos | [`base_de_datos_ferreteria.sql`](base_de_datos_ferreteria.sql) en este repositorio |
| Descargar programa y app | [Release v2.0](https://github.com/alberto94071/Ferreteria_Kathy/releases/latest): `FerreteriaKathy_v1.1_Windows.zip` y `FerreteriaKathy.apk` |
| Última versión (release) | https://github.com/alberto94071/Ferreteria_Kathy/releases/latest |

## Integrantes

| Integrante | Carné |
| --- | --- |
| Rony Alberto Méndez Fuentes | 0903-25-29637 |
| Joaquín Daniel Maldonado | 0903-25-14491 |
| Ivana Marcela Malin de León | 0903-25-18673 |
| Carlos Vallardo Morales González | 0903-25-16887 |

## Qué hace el sistema

- Inicio de sesión con dos roles: **administrador** y **vendedor**.
- Productos, clientes, proveedores, categorías, marcas y usuarios (crear, editar y desactivar).
- Ventas al contado y al crédito, buscando por nombre o con lector de código de barras.
- No deja vender más de lo que hay en existencia y avisa si se vende por debajo del costo.
- No da crédito a un cliente que todavía debe, que no tiene crédito autorizado o que se pasa de su límite.
- Compras a proveedores que suben la existencia automáticamente.
- Créditos con fecha de vencimiento y abonos.
- Kits (productos armados con otros productos) y bajas de inventario.
- Reportes de ventas, compras y productos más vendidos por rango de fechas.
- Comprobante de venta y reporte de bajo stock en PDF.
- App Android para consultar precios escaneando, cobrar abonos y agregar productos.

## Instalación rápida (sin NetBeans)

Para usar el programa en una computadora con Windows sin abrir el código:

1. Instalar **MySQL Server 8** (https://dev.mysql.com/downloads/installer/). No hace falta instalar Java, ya viene incluido.
2. En [Releases](https://github.com/alberto94071/Ferreteria_Kathy/releases/latest) descargar **FerreteriaKathy_v1.1_Windows.zip** y descomprimirlo.
3. Abrir `config.properties` con el Bloc de notas y poner la contraseña de MySQL en `contrasena=`.
4. Doble clic en **Instalar base de datos.bat** (solo la primera vez).
5. Doble clic en **Iniciar Sistema.bat** y entrar con `admin` / `admin123`.

En la carpeta también vienen `Respaldar base de datos.bat` y la app para el celular en `app_celular/FerreteriaKathy.apk`.

## Requisitos para abrir el código

| Programa | Versión |
| --- | --- |
| Java (JDK) | 8 o superior |
| MySQL Server | 5.7 u 8.0 |
| MySQL Workbench | Cualquiera (para correr el script) |
| Apache NetBeans | Con soporte para Maven (lo trae por defecto) |
| Internet | Solo la primera vez, para que Maven descargue las librerías |

Las librerías se descargan solas con Maven: MySQL Connector/J 8.0.33, FlatLaf 3.7.2, Gson 2.11.0 y OpenPDF 1.3.43.

## Instalación en otra computadora

### 1. Descargar el proyecto

Con Git:

```
git clone https://github.com/alberto94071/Ferreteria_Kathy.git
```

O en esta página: botón verde **Code** → **Download ZIP**, y descomprimirlo.

### 2. Crear la base de datos

1. Abrir MySQL Workbench y conectarse al servidor local.
2. **File → Open SQL Script** y escoger `BaseDatos.sql`.
3. Ejecutarlo con el rayo (⚡).

Se crea la base de datos `ferreteria` con sus 14 tablas, el usuario administrador, el usuario vendedor y datos de ejemplo (productos, clientes, ventas, créditos y compras) para probar el sistema de una vez. El script se puede correr varias veces y no borra datos.

> `base_de_datos_ferreteria.sql` crea solo las tablas y el usuario administrador, sin datos de ejemplo.

### 3. Configurar la conexión

Abrir `config.properties` (está en la carpeta principal del proyecto) con el Bloc de notas y poner el usuario y la contraseña de MySQL de esa computadora:

```
servidor=localhost
puerto=3306
base_de_datos=ferreteria
usuario=root
contrasena=root
```

Ahí también se puede cambiar el nombre del negocio y el puerto de la app del celular (`api_puerto=8080`) sin volver a compilar.

### 4. Abrir y ejecutar en NetBeans

1. **File → Open Project** y escoger la carpeta del proyecto (la que tiene el `pom.xml`).
2. Clic derecho sobre el proyecto → **Clean and Build**. La primera vez tarda porque descarga las librerías.
3. **Run Project** (F6). La clase principal es `com.ferreteria.vista.App`.

### 5. Entrar al sistema

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| admin | admin123 | Administrador |
| maria | maria123 | Vendedor |

### 6. Ejecutable (opcional)

Al hacer **Clean and Build** se genera `target/SistemaFerreteria.jar`, que ya trae todas las librerías adentro. Se abre con doble clic o con:

```
java -jar SistemaFerreteria.jar
```

El archivo `config.properties` tiene que estar en la misma carpeta que el `.jar`.

## App del celular (opcional)

**Descargar la app:** https://github.com/alberto94071/Ferreteria_Kathy/releases/latest/download/FerreteriaKathy.apk

Se abre ese enlace desde el celular Android, se descarga el archivo y se instala. Android pide permitir la instalación de apps de origen desconocido para el navegador; se acepta solo para esta instalación.

1. El código de la app está en la carpeta `appmovil` y se abre con **Android Studio**.
2. La computadora y el celular tienen que estar en el mismo Wi-Fi.
3. Con el programa de escritorio abierto, abajo del menú aparece la dirección para el celular, por ejemplo `192.168.1.10:8080`.
4. En la app se escribe esa dirección y el mismo usuario y contraseña del programa.

Si Windows pregunta por el firewall al abrir el programa, hay que permitir el acceso en redes privadas para que el celular se pueda conectar.

## Estructura del proyecto

```
src/main/java/com/ferreteria/
├── modelo   Clases del negocio y sus reglas (Persona, Producto, Venta, Credito...)
├── dao      Acceso a MySQL con JDBC (un DAO por tabla principal)
├── vista    Pantallas en Swing
├── api      Servidor HTTP para la app del celular (puerto 8080)
└── util     Conexión, formato, sesión y generación de PDF
```

## Problemas comunes

| Problema | Solución |
| --- | --- |
| "No se pudo conectar a la base de datos" | Revisar que MySQL esté encendido y que el usuario y la contraseña en `config.properties` sean los correctos. |
| "Unknown database 'ferreteria'" | Falta correr el script SQL del paso 2. |
| NetBeans marca errores en los `import` | Hacer **Clean and Build** con internet para que Maven descargue las librerías. |
| El celular no se conecta | Revisar que estén en el mismo Wi-Fi, que la dirección sea la que dice el programa y que el firewall lo permita. |
