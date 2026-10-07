class Cliente:
    def __init__(self, documento: str, nombre: str) -> None:
        if documento is None or documento.strip() == "" or nombre is None or nombre.strip() == "":
            raise ValueError("Documento y nombre son obligatorios.")
        self._documento = documento
        self._nombre = nombre
        # Aquí hay una relación
        self._alquileres: list = []

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
