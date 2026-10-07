# Taller: del modelo de objetos a la persistencia

Este folder tiene el mismo modelo de Astroescape, ahora en Python. El taller recorre cómo ese modelo empieza a guardar datos. Este documento cubre solo el primer paso.

## Paso 1. La conexión dentro de las clases (Active Record)

Active Record es un objeto de dominio que también sabe persistirse: abre la base de datos, escribe SQL y aplica las reglas del negocio. La fila y el objeto viven en la misma clase.

Eso es lo que hace el paquete `astroescape` ahora. No hay una clase de conexión aparte ni un repositorio. Cada clase abre MySQL por su cuenta, con host, usuario, contraseña y base repetidos en el módulo.

El modelo de objetos no cambió: siguen `Destino`, `Experiencia`, `Viaje`, `ViajeIndividual`, `ViajeGrupal`, `Cliente`, `TrajeEspacial` y `Alquiler`, con las mismas reglas y las mismas relaciones. Se sumaron `guardar()` y `buscar()`.

## Qué hace cada clase con la base

| Clase | Tabla que usa | Qué más escribe |
| --- | --- | --- |
| `Cliente` | `clientes` | — |
| `TrajeEspacial` | `trajes` | — |
| `Destino` | `destinos` | `reservar_experiencia` actualiza `experiencias` |
| `Experiencia` | `experiencias` | — |
| `Alquiler` | `alquileres` | `confirmar` actualiza `trajes` y `alquileres` |
| `Viaje` | `viajes`, `viaje_experiencias`, `viaje_integrantes` | la usan las dos subclases |
| `ViajeIndividual` | `viajes` | `calcular_precio` actualiza `viajes.comprobante` |
| `ViajeGrupal` | `viajes`, `viaje_integrantes`, `pagos` | `confirmar_salida` arma un `INSERT` y lo ejecuta |

`Destino.buscar` recupera el nombre y deja vacías las listas de viajes y de experiencias. `Experiencia.buscar` no pasa por el constructor: si lo hiciera, registraría otra vez la experiencia en el destino.

## Antes de probarlo

1. Instalar el driver, desde la carpeta `python`:

```bash
pip install -r requirements.txt
```

2. En MySQL Workbench, abrir `python/astroescape.sql` y ejecutarlo. El script crea la base `astroescape` y las tablas. Para repetir una prueba desde cero, eliminar la base en Workbench y volver a ejecutar el script.

3. Ajustar la conexión en cada clase para que coincida con Workbench: `HOST`, `PUERTO`, `USUARIO`, `CONTRASENA` y `BASE_DE_DATOS`. Están al inicio de `cliente.py`, `traje_espacial.py`, `destino.py`, `experiencia.py`, `alquiler.py` y `viaje.py`. `ViajeIndividual` y `ViajeGrupal` reutilizan los de `viaje.py`. El valor inicial es `localhost`, puerto `3306`, usuario `root`, contraseña vacía y base `astroescape`. Hace falta MySQL 8.

## Cómo probarlo

Desde la carpeta `python`:

```python
from datetime import date

from astroescape import (
    Alquiler,
    Cliente,
    Destino,
    Experiencia,
    TrajeEspacial,
    ViajeGrupal,
    ViajeIndividual,
)

cliente = Cliente("123", "Ada")
cliente.guardar()

traje = TrajeEspacial("T-1", "M", 2)
traje.guardar()

alquiler = Alquiler(cliente, traje, date(2026, 10, 1), date(2026, 10, 3))
alquiler.guardar()
alquiler.confirmar()

traje_guardado = TrajeEspacial.buscar("T-1")
print(traje_guardado.unidades_disponibles)  # 1
print(Alquiler.buscar(alquiler.id).estado)  # CONFIRMADO

luna = Destino("Luna")
luna.guardar()
paseo = Experiencia("Paseo lunar", luna, 4)
paseo.guardar()
luna.reservar_experiencia(paseo, 2)
print(Experiencia.buscar("Paseo lunar", luna).cupos_restantes)  # 2

viaje = ViajeIndividual("V-1", luna, date(2026, 11, 1), cliente)
viaje.agregar_experiencia(paseo)
viaje.guardar()
viaje.calcular_precio()
print(ViajeIndividual.buscar("V-1").comprobante)

ana = Cliente("456", "Ana")
ana.guardar()
grupal = ViajeGrupal("G-1", luna, date(2026, 12, 1), 3)
grupal.agregar_integrante(cliente)
grupal.agregar_integrante(ana)
grupal.guardar()
grupal.confirmar_salida()
print(ViajeGrupal.buscar("G-1").registro_pago)
```

Hay que guardar el cliente, el traje, el destino y la experiencia antes de confirmar, reservar o calcular el precio. Si no, el `UPDATE` no encuentra la fila: el objeto cambia en memoria y la base no.

## Preguntas para mirar el código

1. ¿Dónde está abierta la conexión? Busquen `mysql.connector.connect` dentro de `confirmar`, `reservar_experiencia`, `calcular_precio` y `confirmar_salida`.
2. `Alquiler.confirmar` resta una unidad en el objeto y otra vez en `UPDATE trajes`. ¿Quién debería cuidar las unidades del traje?
3. `Destino.reservar_experiencia` modifica la tabla `experiencias`. ¿Qué sabe el destino de esa tabla?
4. `ViajeIndividual.calcular_precio` calcula 1200 y, además, guarda el comprobante. ¿Qué pasa si se llama antes de `guardar()`?
5. `ViajeGrupal.confirmar_salida` construye el `INSERT` concatenando el documento del titular y luego lo ejecuta. ¿Qué responsabilidad tiene ese método?
6. `Alquiler.buscar` y `ViajeIndividual.buscar` leen tablas de otras clases. ¿Qué cambia si una columna se renombra?
7. `Destino.buscar("Luna")` no devuelve los viajes que ya se guardaron con ese destino. ¿Dónde quedó la relación?
8. Dos llamadas a `Cliente.buscar("123")` devuelven dos objetos distintos. ¿Cómo se nota eso en los alquileres que cada uno tiene en memoria?
9. Host, usuario, contraseña y nombre de la base están repetidos en cada módulo. ¿Qué hay que tocar para apuntar a otra base de Workbench?

Anoten las respuestas. El contraste con un ORM parte de estas preguntas: la misma información, con el SQL fuera de las clases de dominio.
