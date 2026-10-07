import mysql.connector

from .destino import Destino

# Cada clase abre MySQL por su cuenta. Active Record.
# Estos datos tienen que coincidir con la conexión de Workbench.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


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

    @classmethod
    def _conectar(cls):
        # La clase abre la conexión.
        return mysql.connector.connect(
            host=HOST,
            port=PUERTO,
            user=USUARIO,
            password=CONTRASENA,
            database=BASE_DE_DATOS,
        )

    def guardar(self) -> None:
        conexion = self._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                INSERT INTO experiencias (nombre, destino_nombre, cupos_restantes)
                VALUES (%s, %s, %s) AS nuevos
                ON DUPLICATE KEY UPDATE cupos_restantes = nuevos.cupos_restantes
                """,
                (self._nombre, self._destino.nombre, self._cupos_restantes),
            )
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, nombre: str, destino: Destino) -> "Experiencia | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                SELECT nombre, cupos_restantes
                FROM experiencias
                WHERE nombre = %s AND destino_nombre = %s
                """,
                (nombre, destino.nombre),
            )
            fila = cursor.fetchone()
        finally:
            conexion.close()
        if fila is None:
            return None
        # No usamos el constructor: volvería a registrar la experiencia en el destino.
        experiencia = cls.__new__(cls)
        experiencia._nombre = fila[0]
        experiencia._destino = destino
        experiencia._cupos_restantes = fila[1]
        return experiencia

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
