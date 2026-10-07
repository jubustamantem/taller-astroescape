from datetime import date

from astroescape import (
    Alquiler,
    Cliente,
    Destino,
    Experiencia,
    TrajeEspacial,
    ViajeGrupal,
    ViajeIndividual,
)
from persistencia.mapeo import configurar_mapeo
from servicio import ServicioAstroescape


def main() -> None:
    configurar_mapeo()
    ada = Cliente("123", "Ada")
    ana = Cliente("456", "Ana")
    traje = TrajeEspacial("T-1", "M", 2)
    alquiler = Alquiler(ada, traje, date(2026, 10, 1), date(2026, 10, 3))
    luna = Destino("Luna")
    paseo = Experiencia("Paseo lunar", luna, 4)
    viaje = ViajeIndividual("V-1", luna, date(2026, 11, 1), ada)
    viaje.agregar_experiencia(paseo)
    grupal = ViajeGrupal("G-1", luna, date(2026, 12, 1), 3)
    grupal.agregar_integrante(ada)
    grupal.agregar_integrante(ana)

    # Paso 7. Guardar: Main arma objetos de dominio y el servicio llama a los repositorios.
    with ServicioAstroescape() as servicio:
        servicio.registrar_cliente(ada)
        servicio.registrar_cliente(ana)
        servicio.registrar_traje(traje)
        servicio.registrar_alquiler(alquiler)
        servicio.registrar_destino(luna)
        servicio.registrar_experiencia(paseo)
        servicio.registrar_viaje(viaje)
        servicio.registrar_viaje(grupal)
        servicio.guardar_cambios()
        alquiler_id = alquiler.id

    # Paso 7. Consultar en otra sesión, para leer lo que quedó en MySQL.
    with ServicioAstroescape() as servicio:
        print(servicio.consultar_cliente("123").nombre)
        print(servicio.consultar_traje("T-1").unidades_disponibles)
        print(servicio.consultar_alquiler(alquiler_id).estado)

        luna_db = servicio.consultar_destino("Luna")
        paseo_db = next(item for item in luna_db.experiencias if item.nombre == "Paseo lunar")
        servicio.reservar_experiencia(luna_db, paseo_db, 2)

        alquiler_db = servicio.consultar_alquiler(alquiler_id)
        servicio.confirmar_alquiler(alquiler_db)

        viaje_db = servicio.consultar_viaje("V-1")
        print(servicio.calcular_precio(viaje_db))
        print(viaje_db.comprobante)

        grupal_db = servicio.consultar_viaje("G-1")
        servicio.confirmar_salida(grupal_db)
        print(grupal_db.registro_pago)
        servicio.guardar_cambios()

    with ServicioAstroescape() as servicio:
        print(servicio.consultar_traje("T-1").unidades_disponibles)
        print(servicio.consultar_alquiler(alquiler_id).estado)
        print(servicio.consultar_experiencia("Paseo lunar", "Luna").cupos_restantes)
        print([cliente.nombre for cliente in servicio.consultar_viaje("G-1").integrantes])


if __name__ == "__main__":
    main()
