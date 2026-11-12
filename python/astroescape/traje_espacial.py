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
