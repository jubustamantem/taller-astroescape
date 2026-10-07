import mysql.connector

# Cada clase abre MySQL por su cuenta. Active Record.
# Estos datos tienen que coincidir con la conexión de Workbench.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


class Destino:
    def __init__(self, nombre: str) -> None:
        if nombre is None or nombre.strip() == "":
            raise ValueError("El destino debe tener nombre.")
        self._nombre = nombre
        # Aquí hay una relación
        self._viajes: list = []
        # Aquí hay una relación
        self._experiencias: list = []

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
                INSERT INTO destinos (nombre) VALUES (%s) AS nuevos
                ON DUPLICATE KEY UPDATE nombre = nuevos.nombre
                """,
                (self._nombre,),
            )
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, nombre: str) -> "Destino | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                "SELECT nombre FROM destinos WHERE nombre = %s",
                (nombre,),
            )
            fila = cursor.fetchone()
        finally:
            conexion.close()
        if fila is None:
            return None
        # El destino vuelve sin sus viajes ni sus experiencias.
        return cls(fila[0])

    @property
    def nombre(self) -> str:
        return self._nombre

    def agregar_viaje(self, viaje) -> None:
        self._viajes.append(viaje)

    def agregar_experiencia(self, experiencia) -> None:
        self._experiencias.append(experiencia)

    # Aquí, el destino modifica los cupos restantes de la experiencia. ¿Qué opinan al respecto?
    def reservar_experiencia(self, experiencia, personas: int) -> None:
        if experiencia is None or experiencia not in self._experiencias:
            raise ValueError("La experiencia no pertenece a este destino.")
        if personas <= 0:
            raise ValueError("Debe reservarse al menos un cupo.")
        if experiencia.cupos_restantes >= personas:
            experiencia.cupos_restantes = experiencia.cupos_restantes - personas
            # El destino abre MySQL y escribe en la tabla de otra clase.
            conexion = mysql.connector.connect(
                host=HOST,
                port=PUERTO,
                user=USUARIO,
                password=CONTRASENA,
                database=BASE_DE_DATOS,
            )
            try:
                cursor = conexion.cursor()
                cursor.execute(
                    """
                    UPDATE experiencias
                    SET cupos_restantes = cupos_restantes - %s
                    WHERE nombre = %s AND destino_nombre = %s
                    """,
                    (personas, experiencia.nombre, self._nombre),
                )
                conexion.commit()
            finally:
                conexion.close()

    @property
    def viajes(self) -> list:
        return list(self._viajes)

    @property
    def experiencias(self) -> list:
        return list(self._experiencias)
