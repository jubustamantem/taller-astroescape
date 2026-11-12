from datetime import date

from .cliente import Cliente
from .destino import Destino
from .viaje import Viaje


# Aquí hay una relación
class ViajeIndividual(Viaje):
    def __init__(self, codigo: str, destino: Destino, salida: date, cliente: Cliente) -> None:
        super().__init__(codigo, destino, salida)
        if cliente is None:
            raise ValueError("El viaje individual exige un cliente.")
        # Aquí hay una relación
        self._cliente = cliente
        self._comprobante: str | None = None

    @property
    def cliente(self) -> Cliente:
        return self._cliente

    @property
    def comprobante(self) -> str | None:
        return self._comprobante

    # Este método calcula el precio y prepara el comprobante. ¿Qué opinan al respecto?
    def calcular_precio(self) -> int:
        precio = 1200
        self._comprobante = (
            "VIAJE " + self.codigo + " CLIENTE " + self._cliente.nombre + " TOTAL " + str(precio)
        )
        return precio
