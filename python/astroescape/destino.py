class Destino:
    def __init__(self, nombre: str) -> None:
        if nombre is None or nombre.strip() == "":
            raise ValueError("El destino debe tener nombre.")
        self._nombre = nombre
        # Aquí hay una relación
        self._viajes: list = []
        # Aquí hay una relación
        self._experiencias: list = []

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

    @property
    def viajes(self) -> list:
        return list(self._viajes)

    @property
    def experiencias(self) -> list:
        return list(self._experiencias)
