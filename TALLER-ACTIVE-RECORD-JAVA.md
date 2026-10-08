# Taller: Active Record en Java

Es el mismo paso que `python/TALLER-ACTIVE-RECORD.md`. Cada clase de `src/astroescape` abre MySQL y escribe su SQL. No hay repositorio ni mapeo aparte. Sirve para contrastarlo con la rama del Data Mapper, donde el SQL queda fuera de la entidad.

La base no la crea este programa. En MySQL Workbench hay que ejecutar `python/astroescape.sql`. En cada clase se ajustan `HOST`, `PUERTO`, `USUARIO`, `CONTRASENA` y `BASE_DE_DATOS` para que coincidan con Workbench. Están en `Cliente`, `TrajeEspacial`, `Destino`, `Experiencia`, `Alquiler` y `Viaje`. `ViajeIndividual` y `ViajeGrupal` reutilizan los de `Viaje`.

Hace falta JDK 17 o superior y Maven. Desde la raíz del proyecto:

```bash
mvn -q compile exec:java
```

Si se ejecuta otra vez con los mismos códigos, MySQL rechaza la clave duplicada. Para repetir la prueba, eliminar la base en Workbench y volver a ejecutar el script.

Hay que guardar el cliente, el traje, el destino y la experiencia antes de confirmar, reservar o calcular el precio. Si no, el `UPDATE` no encuentra la fila: el objeto cambia en memoria y la base no.

## Qué hace cada clase con la base

| Clase | Tabla que usa | Qué más escribe |
| --- | --- | --- |
| `Cliente` | `clientes` | — |
| `TrajeEspacial` | `trajes` | — |
| `Destino` | `destinos` | `reservarExperiencia` actualiza `experiencias` |
| `Experiencia` | `experiencias` | — |
| `Alquiler` | `alquileres` | `confirmar` actualiza `trajes` y `alquileres` |
| `Viaje` | `viajes`, `viaje_experiencias` | la usan las dos subclases |
| `ViajeIndividual` | `viajes` | `calcularPrecio` actualiza `viajes.comprobante` |
| `ViajeGrupal` | `viajes`, `viaje_integrantes`, `pagos` | `confirmarSalida` arma un `INSERT` y lo ejecuta |

`Destino.buscar` recupera el nombre y deja vacías las listas de viajes y de experiencias. `Experiencia.buscar` no usa el constructor público: si lo hiciera, registraría otra vez la experiencia en el destino.

## Preguntas para mirar el código

1. ¿Dónde está abierta la conexión? Busquen `DriverManager.getConnection` dentro de `confirmar`, `reservarExperiencia`, `calcularPrecio` y `confirmarSalida`.
2. `Alquiler.confirmar` resta una unidad en el objeto y otra vez en `UPDATE trajes`. ¿Quién debería cuidar las unidades del traje?
3. `Destino.reservarExperiencia` modifica la tabla `experiencias`. ¿Qué sabe el destino de esa tabla?
4. `ViajeIndividual.calcularPrecio` calcula 1200 y, además, guarda el comprobante. ¿Qué pasa si se llama antes de `guardar()`?
5. `ViajeGrupal.confirmarSalida` construye el `INSERT` concatenando el documento del titular y luego lo ejecuta. ¿Qué responsabilidad tiene ese método?
6. `Alquiler.buscar` y `ViajeIndividual.buscar` leen tablas de otras clases. ¿Qué cambia si una columna se renombra?
7. `Destino.buscar("Luna")` no devuelve los viajes que ya se guardaron con ese destino. ¿Dónde quedó la relación?
8. Dos llamadas a `Cliente.buscar("123")` devuelven dos objetos distintos. ¿Cómo se nota eso en los alquileres que cada uno tiene en memoria?
9. Host, usuario, contraseña y nombre de la base están repetidos en cada clase. ¿Qué hay que tocar para apuntar a otra base de Workbench?

En el Data Mapper esas respuestas cambian: la misma información, con el SQL fuera de las clases de dominio.
