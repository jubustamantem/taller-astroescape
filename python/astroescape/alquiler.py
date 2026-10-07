import mysql.connector
from datetime import date

from .cliente import Cliente
from .traje_espacial import TrajeEspacial

# Cada clase abre MySQL por su cuenta. Active Record.
# Estos datos tienen que coincidir con la conexión de Workbench.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


def _fecha(valor: date | str) -> date:
    if isinstance(valor, date):
        return valor
    return date.fromisoformat(valor)


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
        self._id: int | None = None
        cliente.agregar_alquiler(self)
        traje.agregar_alquiler(self)

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
            if self._id is None:
                cursor.execute(
                    """
                    INSERT INTO alquileres (cliente_documento, traje_codigo, inicio, fin, estado)
                    VALUES (%s, %s, %s, %s, %s)
                    """,
                    (
                        self._cliente.documento,
                        self._traje.codigo,
                        self._inicio,
                        self._fin,
                        self._estado,
                    ),
                )
                self._id = cursor.lastrowid
            else:
                cursor.execute(
                    """
                    UPDATE alquileres
                    SET cliente_documento = %s, traje_codigo = %s, inicio = %s, fin = %s, estado = %s
                    WHERE id = %s
                    """,
                    (
                        self._cliente.documento,
                        self._traje.codigo,
                        self._inicio,
                        self._fin,
                        self._estado,
                        self._id,
                    ),
                )
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, identificador: int) -> "Alquiler | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                SELECT id, cliente_documento, traje_codigo, inicio, fin, estado
                FROM alquileres
                WHERE id = %s
                """,
                (identificador,),
            )
            fila = cursor.fetchone()
            if fila is None:
                return None
            # El alquiler consulta las tablas de Cliente y de TrajeEspacial.
            cursor.execute(
                "SELECT documento, nombre FROM clientes WHERE documento = %s",
                (fila[1],),
            )
            cliente_fila = cursor.fetchone()
            cursor.execute(
                "SELECT codigo, talla, unidades_disponibles FROM trajes WHERE codigo = %s",
                (fila[2],),
            )
            traje_fila = cursor.fetchone()
        finally:
            conexion.close()
        if cliente_fila is None or traje_fila is None:
            raise ValueError("El alquiler apunta a un cliente o un traje que no está guardado.")
        cliente = Cliente(cliente_fila[0], cliente_fila[1])
        traje = TrajeEspacial(traje_fila[0], traje_fila[1], traje_fila[2])
        alquiler = cls(cliente, traje, _fecha(fila[3]), _fecha(fila[4]))
        alquiler._id = fila[0]
        alquiler._estado = fila[5]
        return alquiler

    # Las unidades disponibles del traje se modifican aquí. ¿Qué opinan al respecto?
    def confirmar(self) -> None:
        self._traje.unidades_disponibles = self._traje.unidades_disponibles - 1
        self._estado = "CONFIRMADO"
        # El alquiler abre MySQL y actualiza la tabla del traje.
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
                UPDATE trajes
                SET unidades_disponibles = unidades_disponibles - 1
                WHERE codigo = %s
                """,
                (self._traje.codigo,),
            )
            if self._id is not None:
                cursor.execute(
                    "UPDATE alquileres SET estado = %s WHERE id = %s",
                    (self._estado, self._id),
                )
            conexion.commit()
        finally:
            conexion.close()

    @property
    def id(self) -> int | None:
        return self._id

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
