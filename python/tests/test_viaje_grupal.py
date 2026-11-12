from datetime import date

import pytest

from astroescape.cliente import Cliente
from astroescape.destino import Destino
from astroescape.viaje_grupal import ViajeGrupal


def test_rechaza_cupo_menor_que_dos():
    luna = Destino("Luna")

    with pytest.raises(ValueError):
        ViajeGrupal("VG-1", luna, date(2026, 6, 1), 1)


def test_no_permite_exceder_cupo():
    luna = Destino("Luna")
    viaje = ViajeGrupal("VG-1", luna, date(2026, 6, 1), 2)
    viaje.agregar_integrante(Cliente("100", "Ada"))
    viaje.agregar_integrante(Cliente("200", "Ana"))

    with pytest.raises(RuntimeError):
        viaje.agregar_integrante(Cliente("300", "Ali"))


def test_confirmar_salida_exige_al_menos_dos_integrantes():
    luna = Destino("Luna")
    viaje = ViajeGrupal("VG-1", luna, date(2026, 6, 1), 4)
    viaje.agregar_integrante(Cliente("100", "Ada"))

    with pytest.raises(RuntimeError):
        viaje.confirmar_salida()


def test_confirmar_salida_registra_pago():
    luna = Destino("Luna")
    viaje = ViajeGrupal("VG-1", luna, date(2026, 6, 1), 4)
    viaje.agregar_integrante(Cliente("100", "Ada"))
    viaje.agregar_integrante(Cliente("200", "Ana"))

    viaje.confirmar_salida()

    assert viaje.registro_pago == "INSERT INTO pagos(documento, valor) VALUES ('100', 1000)"
