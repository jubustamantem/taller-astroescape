import mysql.connector

# Cada clase abre MySQL por su cuenta. Active Record.
# Estos datos tienen que coincidir con la conexión de Workbench.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


class TrajeEspacial:
    def __init__(self, codigo: str, talla: str, unidades_disponibles: int) -> None:
        if codigo is None or codigo.strip() == "" or talla is None or talla.strip() == "":
            raise ValueError("Código y talla son obligatorios.")
        if unidades_disponibles < 0:
            raise ValueError("Las unidades no pueden iniciar en negativo.")
        self._codigo = codigo
        self._talla = talla
        self.unidades_disponibles = unidades_disponibles
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
                INSERT INTO trajes (codigo, talla, unidades_disponibles)
                VALUES (%s, %s, %s) AS nuevos
                ON DUPLICATE KEY UPDATE
                    talla = nuevos.talla,
                    unidades_disponibles = nuevos.unidades_disponibles
                """,
                (self._codigo, self._talla, self.unidades_disponibles),
            )
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, codigo: str) -> "TrajeEspacial | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                "SELECT codigo, talla, unidades_disponibles FROM trajes WHERE codigo = %s",
                (codigo,),
            )
            fila = cursor.fetchone()
        finally:
            conexion.close()
        if fila is None:
            return None
        return cls(fila[0], fila[1], fila[2])

    @property
    def codigo(self) -> str:
        return self._codigo

    @property
    def talla(self) -> str:
        return self._talla

    def agregar_alquiler(self, alquiler) -> None:
        self._alquileres.append(alquiler)

    @property
    def alquileres(self) -> list:
        return list(self._alquileres)
