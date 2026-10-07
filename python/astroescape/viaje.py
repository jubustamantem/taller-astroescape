from abc import ABC
from datetime import date

from .destino import Destino
from .experiencia import Experiencia


class Viaje(ABC):
    def __init__(self, codigo: str, destino: Destino, salida: date) -> None:
        if type(self) is Viaje:
            raise TypeError("No se puede instanciar la clase abstracta Viaje.")
        if codigo is None or codigo.strip() == "":
            raise ValueError("El viaje debe tener código.")
        if destino is None or salida is None:
            raise ValueError("Destino y fecha de salida son obligatorios.")
        self._codigo = codigo
        # Aquí hay una relación
        self._destino = destino
        self._salida = salida
        # Aquí hay una relación
        self._experiencias: list[Experiencia] = []
        destino.agregar_viaje(self)

    @property
    def codigo(self) -> str:
        return self._codigo

    @property
    def destino(self) -> Destino:
        return self._destino

    @property
    def salida(self) -> date:
        return self._salida

    def agregar_experiencia(self, experiencia: Experiencia) -> None:
        if experiencia is None:
            raise ValueError("La experiencia es obligatoria.")
        if experiencia.destino is not self._destino:
            raise ValueError("La experiencia no corresponde al destino del viaje.")
        self._experiencias.append(experiencia)

    @property
    def experiencias(self) -> list[Experiencia]:
        return list(self._experiencias)
