from astroescape.alquiler import Alquiler
from astroescape.cliente import Cliente
from astroescape.destino import Destino
from astroescape.experiencia import Experiencia
from astroescape.traje_espacial import TrajeEspacial
from astroescape.viaje import Viaje
from astroescape.viaje_grupal import ViajeGrupal
from astroescape.viaje_individual import ViajeIndividual
from persistencia.conexion import Sesion
from persistencia.mapeo import configurar_mapeo
from persistencia.repositorios import (
    RepositorioAlquileres,
    RepositorioClientes,
    RepositorioDestinos,
    RepositorioExperiencias,
    RepositorioTrajes,
    RepositorioViajes,
)


class ServicioAstroescape:
    """Paso 8. El servicio usa repositorios. No ejecuta SQL ni lo pide a la entidad."""

    def __init__(self) -> None:
        configurar_mapeo()
        self._sesion = Sesion()
        self._clientes = RepositorioClientes(self._sesion)
        self._trajes = RepositorioTrajes(self._sesion)
        self._destinos = RepositorioDestinos(self._sesion)
        self._experiencias = RepositorioExperiencias(self._sesion)
        self._alquileres = RepositorioAlquileres(self._sesion)
        self._viajes = RepositorioViajes(self._sesion)

    def registrar_cliente(self, cliente: Cliente) -> None:
        self._clientes.guardar(cliente)

    def consultar_cliente(self, documento: str) -> Cliente | None:
        return self._clientes.buscar_por_documento(documento)

    def registrar_traje(self, traje: TrajeEspacial) -> None:
        self._trajes.guardar(traje)

    def consultar_traje(self, codigo: str) -> TrajeEspacial | None:
        return self._trajes.buscar_por_codigo(codigo)

    def registrar_destino(self, destino: Destino) -> None:
        self._destinos.guardar(destino)

    def consultar_destino(self, nombre: str) -> Destino | None:
        return self._destinos.buscar_por_nombre(nombre)

    def registrar_experiencia(self, experiencia: Experiencia) -> None:
        self._experiencias.guardar(experiencia)

    def consultar_experiencia(self, nombre: str, destino_nombre: str) -> Experiencia | None:
        return self._experiencias.buscar(nombre, destino_nombre)

    def registrar_alquiler(self, alquiler: Alquiler) -> None:
        self._alquileres.guardar(alquiler)

    def consultar_alquiler(self, identificador: int) -> Alquiler | None:
        return self._alquileres.buscar_por_id(identificador)

    def confirmar_alquiler(self, alquiler: Alquiler) -> None:
        alquiler.confirmar()
        self._trajes.guardar(alquiler.traje)
        self._alquileres.guardar(alquiler)

    def registrar_viaje(self, viaje: Viaje) -> None:
        self._viajes.guardar(viaje)

    def consultar_viaje(self, codigo: str) -> Viaje | None:
        return self._viajes.buscar_por_codigo(codigo)

    def calcular_precio(self, viaje: ViajeIndividual) -> int:
        precio = viaje.calcular_precio()
        self._viajes.guardar(viaje)
        return precio

    def reservar_experiencia(self, destino: Destino, experiencia: Experiencia, personas: int) -> None:
        destino.reservar_experiencia(experiencia, personas)
        self._experiencias.guardar(experiencia)

    def confirmar_salida(self, viaje: ViajeGrupal) -> None:
        viaje.confirmar_salida()
        self._viajes.guardar(viaje)

    def guardar_cambios(self) -> None:
        self._sesion.commit()

    def cerrar(self) -> None:
        self._sesion.close()

    def __enter__(self) -> "ServicioAstroescape":
        return self

    def __exit__(self, exc_type, exc, tb) -> None:
        if exc_type is not None:
            self._sesion.rollback()
        self._sesion.close()
