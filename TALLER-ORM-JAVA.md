# Taller: persistencia con ORM en Java

Es el mismo recorrido que `python/TALLER-ORM.md`, con Hibernate. Las clases de `src/astroescape` no escriben SQL. El mapeo está en `src/main/resources/META-INF/orm.xml` y los repositorios en `src/astroescape/persistencia`.

Hace falta JDK 17 o superior y Maven. En `src/astroescape/persistencia/Conexion.java` se ajustan `HOST`, `PUERTO`, `USUARIO`, `CONTRASENA` y `BASE_DE_DATOS`. El usuario de MySQL tiene que poder crear bases. Desde la raíz del proyecto:

```bash
mvn -q compile exec:java
```

Al arrancar, `Conexion` crea la base `astroescape` y Hibernate crea las tablas que falten (`hibernate.hbm2ddl.auto=update`). No hace falta un script. Si la base ya venía del programa en Python, conviene eliminarla antes: las tablas de integrantes no coinciden y Hibernate no las reescribe.

Si se ejecuta otra vez con los mismos códigos, MySQL rechaza la clave duplicada. Eliminar la base `astroescape` y volver a ejecutar deja el esquema vacío.

Hibernate necesita un constructor sin argumentos para armar el objeto al leer una fila, y escribe los campos por reflexión. Por eso los campos persistentes ya no son `final` y cada clase tiene un constructor privado vacío. Las reglas siguen en el constructor público. `Alquiler` tiene `id`, que genera MySQL.

## 1. Partir de la clase de dominio

`Cliente`, `TrajeEspacial`, `Destino`, `Experiencia`, `Alquiler`, `Viaje`, `ViajeIndividual` y `ViajeGrupal` conservan sus reglas. `confirmar`, `reservarExperiencia`, `calcularPrecio` y `confirmarSalida` modifican objetos en memoria.

`confirmarSalida` sigue armando el texto del `INSERT`. No lo ejecuta. El repositorio guarda ese texto en `viajes.registro_pago`.

## 2. Atributos que requieren persistencia

| Clase | Se guardan | Quedan como relación |
| --- | --- | --- |
| `Cliente` | `documento`, `nombre` | alquileres |
| `TrajeEspacial` | `codigo`, `talla`, `unidadesDisponibles` | alquileres |
| `Destino` | `nombre` | viajes, experiencias |
| `Experiencia` | `nombre`, destino, `cuposRestantes` | — |
| `Alquiler` | cliente, traje, `inicio`, `fin`, `estado` | — |
| `Viaje` | `codigo`, destino, `salida`, experiencias | — |
| `ViajeIndividual` | cliente, `comprobante` | — |
| `ViajeGrupal` | `cupoMaximo`, integrantes, `registroPago` | — |

`tipo` no es un dato del dominio. Sirve para distinguir, en la misma tabla, un viaje individual de uno grupal.

## 3. Tabla y clave primaria

| Clase | Tabla | Clave primaria |
| --- | --- | --- |
| `Cliente` | `clientes` | `documento` |
| `TrajeEspacial` | `trajes` | `codigo` |
| `Destino` | `destinos` | `nombre` |
| `Experiencia` | `experiencias` | `nombre` + `destino_nombre` |
| `Alquiler` | `alquileres` | `id` (la genera MySQL) |
| `Viaje` y sus subclases | `viajes` | `codigo` |
| experiencias del viaje | `viaje_experiencias` | viaje + experiencia |
| integrantes | `viaje_integrantes` | viaje + cliente, con columna `orden` |

## 4. Configurar la conexión y el ORM

`Conexion` abre JDBC para crear la base y después la fábrica de `EntityManager`. Para ver el SQL, en `propiedades()` se puede poner `hibernate.show_sql` en `true`.

## 5. Declarar el mapeo entidad-tabla

`orm.xml` declara tablas, columnas y relaciones. Las clases de dominio no llevan anotaciones del ORM.

## 6. Crear el repositorio

Cada clase de `persistencia` hace `persist` para guardar y `find` para consultar. No escriben SQL.

## 7. Guardar y consultar

`Main` crea los objetos, pide al servicio que los registre y llama a `guardarCambios()`. Después abre otro servicio y consulta.

## 8. Main usa el servicio, no SQL en la entidad

`ServicioAstroescape` es el único que conoce los repositorios. `confirmarAlquiler` llama a `alquiler.confirmar()` y después a los repositorios. El `UPDATE` lo emite Hibernate al confirmar la transacción.
