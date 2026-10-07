from urllib.parse import quote_plus

from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# Paso 4. La conexión y el ORM viven aquí, fuera de las clases de dominio.
# El usuario de MySQL tiene que poder crear la base. El resto lo hace el código.
HOST = "localhost"
PUERTO = 3306
USUARIO = "root"
CONTRASENA = ""
BASE_DE_DATOS = "astroescape"


def _url(base: str | None = None) -> str:
    ruta = f"/{base}" if base else ""
    return (
        f"mysql+mysqlconnector://{USUARIO}:{quote_plus(CONTRASENA)}"
        f"@{HOST}:{PUERTO}{ruta}"
    )


def asegurar_base() -> None:
    if not BASE_DE_DATOS.isidentifier():
        raise ValueError("El nombre de la base no es válido.")
    servidor = create_engine(_url(), pool_pre_ping=True)
    with servidor.begin() as conexion:
        conexion.execute(
            text(
                f"CREATE DATABASE IF NOT EXISTS `{BASE_DE_DATOS}` "
                "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            )
        )
    servidor.dispose()


url = _url(BASE_DE_DATOS)
motor = create_engine(url, echo=False, pool_pre_ping=True)
Sesion = sessionmaker(bind=motor, expire_on_commit=False)
