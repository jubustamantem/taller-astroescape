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
class ViajeGrupal(Viaje):
    def __init__(self, codigo: str, destino: Destino, salida: date, cupo_maximo: int) -> None:
        super().__init__(codigo, destino, salida)
        if cupo_maximo < 2:
            raise ValueError("Un viaje grupal admite al menos dos integrantes.")
        self._cupo_maximo = cupo_maximo
        # Aquí hay una relación
        self._integrantes: list[Cliente] = []
        self._registro_pago: str | None = None

    def guardar(self) -> None:
        conexion = self._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                INSERT INTO viajes (
                    codigo, tipo, destino_nombre, salida, cupo_maximo,
                    cliente_documento, comprobante, registro_pago
                ) VALUES (%s, 'GRUPAL', %s, %s, %s, NULL, NULL, %s) AS nuevos
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
                    self._cupo_maximo,
                    self._registro_pago,
                ),
            )
            self._guardar_experiencias(cursor)
            cursor.execute(
                "DELETE FROM viaje_integrantes WHERE viaje_codigo = %s",
                (self._codigo,),
            )
            for cliente in self._integrantes:
                cursor.execute(
                    """
                    INSERT INTO viaje_integrantes (viaje_codigo, cliente_documento)
                    VALUES (%s, %s)
                    """,
                    (self._codigo, cliente.documento),
                )
            conexion.commit()
        finally:
            conexion.close()

    @classmethod
    def buscar(cls, codigo: str) -> "ViajeGrupal | None":
        conexion = cls._conectar()
        try:
            cursor = conexion.cursor()
            cursor.execute(
                """
                SELECT codigo, destino_nombre, salida, cupo_maximo, registro_pago
                FROM viajes
                WHERE codigo = %s AND tipo = 'GRUPAL'
                """,
                (codigo,),
            )
            fila = cursor.fetchone()
            if fila is None:
                return None
            cursor.execute(
                """
                SELECT c.documento, c.nombre
                FROM viaje_integrantes vi
                JOIN clientes c ON c.documento = vi.cliente_documento
                WHERE vi.viaje_codigo = %s
                ORDER BY vi.id
                """,
                (codigo,),
            )
            integrantes_filas = cursor.fetchall()
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
        destino = Destino(fila[1])
        viaje = cls(fila[0], destino, _fecha(fila[2]), fila[3])
        viaje._registro_pago = fila[4]
        for documento, nombre in integrantes_filas:
            viaje.agregar_integrante(Cliente(documento, nombre))
        for nombre_experiencia, cupos in experiencias_filas:
            viaje.agregar_experiencia(Experiencia(nombre_experiencia, destino, cupos))
        return viaje

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
            "INSERT INTO pagos(documento, valor) VALUES ('"
            + titular.documento
            + "', "
            + str(total)
            + ")"
        )
        # La regla de negocio abre MySQL y ejecuta SQL armado en la clase.
        conexion = mysql.connector.connect(
            host=HOST,
            port=PUERTO,
            user=USUARIO,
            password=CONTRASENA,
            database=BASE_DE_DATOS,
        )
        try:
            cursor = conexion.cursor()
            cursor.execute(self._registro_pago)
            cursor.execute(
                "UPDATE viajes SET registro_pago = %s WHERE codigo = %s",
                (self._registro_pago, self._codigo),
            )
            conexion.commit()
        finally:
            conexion.close()

    @property
    def cupo_maximo(self) -> int:
        return self._cupo_maximo

    @property
    def integrantes(self) -> list[Cliente]:
        return list(self._integrantes)

    @property
    def registro_pago(self) -> str | None:
        return self._registro_pago
