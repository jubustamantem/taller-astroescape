# Taller: persistencia con ORM

El modelo de dominio sigue en `astroescape`. Esta vez las clases no abren MySQL ni escriben SQL. El mapeo y los repositorios viven aparte. Es un Data Mapper: la entidad y la fila se relacionan en otro objeto.

No hay un script para crear la base. Al arrancar, el código crea la base `astroescape` y sus tablas. En `persistencia/conexion.py` solo se ajustan `HOST`, `PUERTO`, `USUARIO`, `CONTRASENA` y `BASE_DE_DATOS`, con un usuario de MySQL que pueda crear bases. Desde la carpeta `python`:

```bash
pip install -r requirements.txt
python main.py
```

`main.py` recorre los pasos 7 y 8. Si se ejecuta otra vez con los mismos códigos, MySQL rechaza la clave duplicada. Para repetir la prueba, eliminar la base `astroescape`: la siguiente ejecución la vuelve a crear vacía.

## 1. Partir de la clase de dominio

`Cliente`, `TrajeEspacial`, `Destino`, `Experiencia`, `Alquiler`, `Viaje`, `ViajeIndividual` y `ViajeGrupal` conservan sus reglas. `confirmar`, `reservar_experiencia`, `calcular_precio` y `confirmar_salida` modifican objetos en memoria.

`confirmar_salida` sigue armando el texto del `INSERT`. No lo ejecuta. El repositorio guarda ese texto en `viajes.registro_pago`.

## 2. Atributos que requieren persistencia

| Clase | Se guardan | Quedan como relación |
| --- | --- | --- |
| `Cliente` | `documento`, `nombre` | alquileres |
| `TrajeEspacial` | `codigo`, `talla`, `unidades_disponibles` | alquileres |
| `Destino` | `nombre` | viajes, experiencias |
| `Experiencia` | `nombre`, destino, `cupos_restantes` | — |
| `Alquiler` | cliente, traje, `inicio`, `fin`, `estado` | — |
| `Viaje` | `codigo`, destino, `salida`, experiencias | — |
| `ViajeIndividual` | cliente, `comprobante` | — |
| `ViajeGrupal` | `cupo_maximo`, integrantes, `registro_pago` | — |

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
| experiencias del viaje | `viaje_experiencias` | `viaje_codigo` + `experiencia_nombre` + `destino_nombre` |
| integrantes | `viaje_integrantes` | `id` |

## 4. Configurar la conexión y el ORM

`persistencia/conexion.py` crea el motor de SQLAlchemy con el driver de MySQL y la fábrica `Sesion`. `asegurar_base()` crea la base si todavía no existe. Cambiar de servidor o de nombre es tocar ese archivo.

Para ver el SQL que genera el ORM, en ese mismo archivo se puede pasar `echo=True` a `create_engine`.

## 5. Declarar el mapeo entidad-tabla

`persistencia/mapeo.py` declara las tablas y las une a las clases con `map_imperatively`. Las clases de `astroescape` no heredan de una clase del ORM.

`configurar_mapeo()` se llama al crear el servicio. Después del mapeo ejecuta `metadata.create_all`: crea las tablas que falten y no borra las que ya existen. La segunda vez no vuelve a mapear.

## 6. Crear el repositorio

`persistencia/repositorios.py` tiene un repositorio por entidad. `guardar` hace `sesion.add`. `buscar` hace `sesion.get` con la clave primaria. No hay `mysql.connector` ni sentencias SQL en esas clases.

## 7. Guardar y consultar

`main.py` crea los objetos, pide al servicio que los registre y llama a `guardar_cambios()`. Después abre otro servicio y consulta cliente, traje y alquiler.

## 8. Main usa el servicio, no SQL en la entidad

`ServicioAstroescape` es el único que conoce los repositorios. `confirmar_alquiler` llama a `alquiler.confirmar()` y después al repositorio del traje y del alquiler. El `UPDATE` lo emite SQLAlchemy al confirmar la sesión, porque el traje y el alquiler quedaron modificados.

Lo mismo pasa con `calcular_precio`, `reservar_experiencia` y `confirmar_salida`: la regla queda en la entidad y la escritura queda en el repositorio.
