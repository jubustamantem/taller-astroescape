from sqlalchemy.orm import Session

from astroescape.alquiler import Alquiler
from astroescape.cliente import Cliente
from astroescape.destino import Destino
from astroescape.experiencia import Experiencia
from astroescape.traje_espacial import TrajeEspacial
from astroescape.viaje import Viaje

# Paso 6. El repositorio guarda y consulta. La entidad no ve la sesión ni el SQL.


class Repositorio:
    def __init__(self, sesion: Session) -> None:
        self._sesion = sesion


class RepositorioClientes(Repositorio):
    def guardar(self, cliente: Cliente) -> None:
        self._sesion.add(cliente)

    def buscar_por_documento(self, documento: str) -> Cliente | None:
        return self._sesion.get(Cliente, documento)


class RepositorioTrajes(Repositorio):
    def guardar(self, traje: TrajeEspacial) -> None:
        self._sesion.add(traje)

    def buscar_por_codigo(self, codigo: str) -> TrajeEspacial | None:
        return self._sesion.get(TrajeEspacial, codigo)


class RepositorioDestinos(Repositorio):
    def guardar(self, destino: Destino) -> None:
        self._sesion.add(destino)

    def buscar_por_nombre(self, nombre: str) -> Destino | None:
        return self._sesion.get(Destino, nombre)


class RepositorioExperiencias(Repositorio):
    def guardar(self, experiencia: Experiencia) -> None:
        self._sesion.add(experiencia)

    def buscar(self, nombre: str, destino_nombre: str) -> Experiencia | None:
        return self._sesion.get(Experiencia, (nombre, destino_nombre))


class RepositorioAlquileres(Repositorio):
    def guardar(self, alquiler: Alquiler) -> None:
        self._sesion.add(alquiler)

    def buscar_por_id(self, identificador: int) -> Alquiler | None:
        return self._sesion.get(Alquiler, identificador)


class RepositorioViajes(Repositorio):
    def guardar(self, viaje: Viaje) -> None:
        self._sesion.add(viaje)

    def buscar_por_codigo(self, codigo: str) -> Viaje | None:
        return self._sesion.get(Viaje, codigo)
