import mysql.connector
from abc import ABC
from datetime import date

from .destino import Destino
from .experiencia import Experiencia

# Cada clase abre MySQL por su cuenta. Active Record.
# Estos datos tienen que coincidir con la conexión de Workbench.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


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

    @classmethod
    def _conectar(cls):
        # La jerarquía abre la conexión.
        return mysql.connector.connect(
            host=HOST,
            port=PUERTO,
            user=USUARIO,
            password=CONTRASENA,
            database=BASE_DE_DATOS,
        )

    def _guardar_experiencias(self, cursor) -> None:
        cursor.execute(
            "DELETE FROM viaje_experiencias WHERE viaje_codigo = %s",
            (self._codigo,),
        )
        for experiencia in self._experiencias:
            cursor.execute(
                """
                INSERT INTO viaje_experiencias (viaje_codigo, experiencia_nombre)
                VALUES (%s, %s)
                """,
                (self._codigo, experiencia.nombre),
            )

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
