import mysql.connector

# Cada clase abre MySQL por su cuenta. Active Record.
# Estos datos tienen que coincidir con la conexión de Workbench.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


class Cliente:
    def __init__(self, documento: str, nombre: str) -> None:
        if documento is None or documento.strip() == "" or nombre is None or nombre.strip() == "":
            raise ValueError("Documento y nombre son obligatorios.")
        self._documento = documento
        self._nombre = nombre
        # Aquí hay una relación
        self._alquileres: list = []

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
                INSERT INTO clientes (documento, nombre) VALUES (%s, %s) AS nuevos
                ON DUPLICATE KEY UPDATE nombre = nuevos.nombre
                """,
                (self._documento, self._nombre),
            )
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, documento: str) -> "Cliente | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                "SELECT documento, nombre FROM clientes WHERE documento = %s",
                (documento,),
            )
            fila = cursor.fetchone()
        finally:
            conexion.close()
        if fila is None:
            return None
        return cls(fila[0], fila[1])

    @property
    def documento(self) -> str:
        return self._documento

    @property
    def nombre(self) -> str:
        return self._nombre

    def agregar_alquiler(self, alquiler) -> None:
        self._alquileres.append(alquiler)

    @property
    def alquileres(self) -> list:
        return list(self._alquileres)
