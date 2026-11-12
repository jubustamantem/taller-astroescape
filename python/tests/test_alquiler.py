from datetime import date

import pytest

from astroescape.alquiler import Alquiler
from astroescape.cliente import Cliente
from astroescape.traje_espacial import TrajeEspacial


def test_rechaza_intervalo_invalido():
    cliente = Cliente("100", "Ada")
    traje = TrajeEspacial("T-1", "M", 3)
    inicio = date(2026, 5, 10)
    fin = date(2026, 5, 1)

    with pytest.raises(ValueError):
        Alquiler(cliente, traje, inicio, fin)


def test_confirmar_cambia_estado_y_reduce_unidades():
    cliente = Cliente("100", "Ada")
    traje = TrajeEspacial("T-1", "M", 3)
    alquiler = Alquiler(cliente, traje, date(2026, 5, 1), date(2026, 5, 10))

    alquiler.confirmar()

    assert alquiler.estado == "CONFIRMADO"
    assert traje.unidades_disponibles == 2
