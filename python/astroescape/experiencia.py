from .destino import Destino


class Experiencia:
    def __init__(self, nombre: str, destino: Destino, cupos_restantes: int) -> None:
        if nombre is None or nombre.strip() == "":
            raise ValueError("La experiencia debe tener nombre.")
        if destino is None:
            raise ValueError("La experiencia pertenece a un destino.")
        if cupos_restantes < 0:
            raise ValueError("Los cupos no pueden iniciar en negativo.")
        self._nombre = nombre
        # Aquí hay una relación
        self._destino = destino
        self._cupos_restantes = cupos_restantes
        destino.agregar_experiencia(self)

    @property
    def nombre(self) -> str:
        return self._nombre

    @property
    def destino(self) -> Destino:
        return self._destino

    @property
    def cupos_restantes(self) -> int:
        return self._cupos_restantes

    @cupos_restantes.setter
    def cupos_restantes(self, cupos_restantes: int) -> None:
        self._cupos_restantes = cupos_restantes
