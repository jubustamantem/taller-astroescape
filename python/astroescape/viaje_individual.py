import mysql.connector
from datetime import date

from .cliente import Cliente
from .destino import Destino
from .experiencia import Experiencia
from .viaje import BASE_DE_DATOS, CONTRASENA, HOST, PUERTO, USUARIO, Viaje


def _fecha(valor: date | str) -> date:
    if isinstance(valor, date):
        return valor
    return date.fromisoformat(valor)


# Aquí hay una relación
class ViajeIndividual(Viaje):
    def __init__(self, codigo: str, destino: Destino, salida: date, cliente: Cliente) -> None:
        super().__init__(codigo, destino, salida)
        if cliente is None:
            raise ValueError("El viaje individual exige un cliente.")
        # Aquí hay una relación
        self._cliente = cliente
        self._comprobante: str | None = None

    def guardar(self) -> None:
        conexion = self._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                INSERT INTO viajes (
                    codigo, tipo, destino_nombre, salida, cupo_maximo,
                    cliente_documento, comprobante, registro_pago
                ) VALUES (%s, 'INDIVIDUAL', %s, %s, NULL, %s, %s, NULL) AS nuevos
                ON DUPLICATE KEY UPDATE
                    tipo = nuevos.tipo,
                    destino_nombre = nuevos.destino_nombre,
                    salida = nuevos.salida,
                    cupo_maximo = nuevos.cupo_maximo,
                    cliente_documento = nuevos.cliente_documento,
                    comprobante = nuevos.comprobante,
                    registro_pago = nuevos.registro_pago
                """,
                (
                    self._codigo,
                    self._destino.nombre,
                    self._salida,
                    self._cliente.documento,
                    self._comprobante,
                ),
            )
            self._guardar_experiencias(cursor)
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, codigo: str) -> "ViajeIndividual | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                SELECT codigo, destino_nombre, salida, cliente_documento, comprobante
                FROM viajes
                WHERE codigo = %s AND tipo = 'INDIVIDUAL'
                """,
                (codigo,),
            )
            fila = cursor.fetchone()
            if fila is None:
                return None
            cursor.execute(
                "SELECT documento, nombre FROM clientes WHERE documento = %s",
                (fila[3],),
            )
            cliente_fila = cursor.fetchone()
            cursor.execute(
                """
                SELECT e.nombre, e.cupos_restantes
                FROM viaje_experiencias ve
                JOIN experiencias e ON e.nombre = ve.experiencia_nombre
                WHERE ve.viaje_codigo = %s AND e.destino_nombre = %s
                """,
                (codigo, fila[1]),
            )
            experiencias_filas = cursor.fetchall()
        finally:
            conexion.close()
        if cliente_fila is None:
            raise ValueError("El viaje individual apunta a un cliente que no está guardado.")
        destino = Destino(fila[1])
        cliente = Cliente(cliente_fila[0], cliente_fila[1])
        viaje = cls(fila[0], destino, _fecha(fila[2]), cliente)
        viaje._comprobante = fila[4]
        for nombre, cupos in experiencias_filas:
            viaje.agregar_experiencia(Experiencia(nombre, destino, cupos))
        return viaje

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
        # Calcular el precio también escribe en MySQL.
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
                "UPDATE viajes SET comprobante = %s WHERE codigo = %s",
                (self._comprobante, self._codigo),
            )
            conexion.commit()
        finally:
            conexion.close()
        return precio
