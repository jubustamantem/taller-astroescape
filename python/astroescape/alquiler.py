from datetime import date

from .cliente import Cliente
from .traje_espacial import TrajeEspacial


class Alquiler:
    def __init__(self, cliente: Cliente, traje: TrajeEspacial, inicio: date, fin: date) -> None:
        if cliente is None or traje is None:
            raise ValueError("El alquiler vincula un cliente y un traje.")
        if inicio is None or fin is None or fin < inicio:
            raise ValueError("El intervalo del alquiler no es válido.")
        # Aquí hay una relación
        self._cliente = cliente
        self._traje = traje
        self._inicio = inicio
        self._fin = fin
        self._estado = "SOLICITADO"
        cliente.agregar_alquiler(self)
        traje.agregar_alquiler(self)

    # Las unidades disponibles del traje se modifican aquí. ¿Qué opinan al respecto?
    def confirmar(self) -> None:
        self._traje.unidades_disponibles = self._traje.unidades_disponibles - 1
        self._estado = "CONFIRMADO"

    @property
    def cliente(self) -> Cliente:
        return self._cliente

    @property
    def traje(self) -> TrajeEspacial:
        return self._traje

    @property
    def inicio(self) -> date:
        return self._inicio

    @property
    def fin(self) -> date:
        return self._fin

    @property
    def estado(self) -> str:
        return self._estado
