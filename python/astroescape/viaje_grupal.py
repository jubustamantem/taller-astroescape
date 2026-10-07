from datetime import date

from .cliente import Cliente
from .destino import Destino
from .viaje import Viaje


# Aquí hay una relación
class ViajeGrupal(Viaje):
    def __init__(self, codigo: str, destino: Destino, salida: date, cupo_maximo: int) -> None:
        super().__init__(codigo, destino, salida)
        if cupo_maximo < 2:
            raise ValueError("Un viaje grupal admite al menos dos integrantes.")
        self._cupo_maximo = cupo_maximo
        # Aquí hay una relación
        self._integrantes: list[Cliente] = []
        self._registro_pago: str | None = None

    def agregar_integrante(self, cliente: Cliente) -> None:
        if cliente is None:
            raise ValueError("El integrante es obligatorio.")
        if len(self._integrantes) >= self._cupo_maximo:
            raise RuntimeError("El viaje grupal ya completó su cupo.")
        self._integrantes.append(cliente)

    # Este método registra el pago en la base de datos. ¿Qué opinan al respecto?
    def confirmar_salida(self) -> None:
        if len(self._integrantes) < 2:
            raise RuntimeError("El viaje grupal necesita al menos dos integrantes.")
        titular = self._integrantes[0]
        total = len(self._integrantes) * 500
        self._registro_pago = (
            "INSERT INTO pagos(documento, valor) VALUES ('" + titular.documento + "', " + str(total) + ")"
        )

    @property
    def cupo_maximo(self) -> int:
        return self._cupo_maximo

    @property
    def integrantes(self) -> list[Cliente]:
        return list(self._integrantes)

    @property
    def registro_pago(self) -> str | None:
        return self._registro_pago
