from sqlalchemy import (
    Column,
    Date,
    ForeignKey,
    ForeignKeyConstraint,
    Integer,
    String,
    Table,
    Text,
    UniqueConstraint,
    and_,
)
from sqlalchemy.orm import registry, relationship

from astroescape.alquiler import Alquiler
from astroescape.cliente import Cliente
from astroescape.destino import Destino
from astroescape.experiencia import Experiencia
from astroescape.traje_espacial import TrajeEspacial
from astroescape.viaje import Viaje
from astroescape.viaje_grupal import ViajeGrupal
from astroescape.viaje_individual import ViajeIndividual
from persistencia.conexion import asegurar_base, motor

# Paso 5. Mapeo imperativo: la clase de dominio no hereda del ORM.
registro = registry()
metadata = registro.metadata
_configurado = False

clientes = Table(
    "clientes",
    metadata,
    Column("documento", String(50), primary_key=True),
    Column("nombre", String(150), nullable=False),
)

trajes = Table(
    "trajes",
    metadata,
    Column("codigo", String(50), primary_key=True),
    Column("talla", String(20), nullable=False),
    Column("unidades_disponibles", Integer, nullable=False),
)

destinos = Table(
    "destinos",
    metadata,
    Column("nombre", String(150), primary_key=True),
)

experiencias = Table(
    "experiencias",
    metadata,
    Column("nombre", String(150), primary_key=True),
    Column("destino_nombre", String(150), ForeignKey("destinos.nombre"), primary_key=True),
    Column("cupos_restantes", Integer, nullable=False),
)

alquileres = Table(
    "alquileres",
    metadata,
    Column("id", Integer, primary_key=True, autoincrement=True),
    Column("cliente_documento", String(50), ForeignKey("clientes.documento"), nullable=False),
    Column("traje_codigo", String(50), ForeignKey("trajes.codigo"), nullable=False),
    Column("inicio", Date, nullable=False),
    Column("fin", Date, nullable=False),
    Column("estado", String(20), nullable=False),
)

viajes = Table(
    "viajes",
    metadata,
    Column("codigo", String(50), primary_key=True),
    Column("tipo", String(20), nullable=False),
    Column("destino_nombre", String(150), ForeignKey("destinos.nombre"), nullable=False),
    Column("salida", Date, nullable=False),
    Column("cupo_maximo", Integer),
    Column("cliente_documento", String(50), ForeignKey("clientes.documento")),
    Column("comprobante", String(255)),
    Column("registro_pago", Text),
)

viaje_experiencias = Table(
    "viaje_experiencias",
    metadata,
    Column("viaje_codigo", String(50), ForeignKey("viajes.codigo"), primary_key=True),
    Column("experiencia_nombre", String(150), primary_key=True),
    Column("destino_nombre", String(150), primary_key=True),
    ForeignKeyConstraint(
        ["experiencia_nombre", "destino_nombre"],
        ["experiencias.nombre", "experiencias.destino_nombre"],
    ),
)

viaje_integrantes = Table(
    "viaje_integrantes",
    metadata,
    Column("id", Integer, primary_key=True, autoincrement=True),
    Column("viaje_codigo", String(50), ForeignKey("viajes.codigo"), nullable=False),
    Column("cliente_documento", String(50), ForeignKey("clientes.documento"), nullable=False),
    UniqueConstraint("viaje_codigo", "cliente_documento", name="uk_viaje_cliente"),
)


def configurar_mapeo() -> None:
    global _configurado
    if _configurado:
        return

    # Cliente: persisten documento (PK) y nombre.
    registro.map_imperatively(
        Cliente,
        clientes,
        properties={
            "_documento": clientes.c.documento,
            "_nombre": clientes.c.nombre,
            "_alquileres": relationship(
                Alquiler,
                primaryjoin=alquileres.c.cliente_documento == clientes.c.documento,
                foreign_keys=[alquileres.c.cliente_documento],
                viewonly=True,
                lazy="selectin",
                overlaps="_cliente",
            ),
        },
    )

    # TrajeEspacial: persisten codigo (PK), talla y unidades_disponibles.
    registro.map_imperatively(
        TrajeEspacial,
        trajes,
        properties={
            "_codigo": trajes.c.codigo,
            "_talla": trajes.c.talla,
            "unidades_disponibles": trajes.c.unidades_disponibles,
            "_alquileres": relationship(
                Alquiler,
                primaryjoin=alquileres.c.traje_codigo == trajes.c.codigo,
                foreign_keys=[alquileres.c.traje_codigo],
                viewonly=True,
                lazy="selectin",
                overlaps="_traje",
            ),
        },
    )

    # Destino: persiste nombre (PK). Viajes y experiencias se leen por la relación.
    registro.map_imperatively(
        Destino,
        destinos,
        properties={
            "_nombre": destinos.c.nombre,
            "_viajes": relationship(
                Viaje,
                primaryjoin=viajes.c.destino_nombre == destinos.c.nombre,
                foreign_keys=[viajes.c.destino_nombre],
                viewonly=True,
                lazy="selectin",
                overlaps="_destino",
            ),
            "_experiencias": relationship(
                Experiencia,
                primaryjoin=experiencias.c.destino_nombre == destinos.c.nombre,
                foreign_keys=[experiencias.c.destino_nombre],
                viewonly=True,
                lazy="selectin",
                overlaps="_destino",
            ),
        },
    )

    # Experiencia: PK compuesta nombre + destino. Persiste cupos_restantes.
    registro.map_imperatively(
        Experiencia,
        experiencias,
        properties={
            "_nombre": experiencias.c.nombre,
            "_destino_nombre": experiencias.c.destino_nombre,
            "_cupos_restantes": experiencias.c.cupos_restantes,
            "_destino": relationship(
                Destino,
                foreign_keys=[experiencias.c.destino_nombre],
                lazy="selectin",
                overlaps="_experiencias",
            ),
        },
    )

    # Alquiler: PK id generada por MySQL. Persisten cliente, traje, fechas y estado.
    registro.map_imperatively(
        Alquiler,
        alquileres,
        properties={
            "_id": alquileres.c.id,
            "_cliente_documento": alquileres.c.cliente_documento,
            "_traje_codigo": alquileres.c.traje_codigo,
            "_inicio": alquileres.c.inicio,
            "_fin": alquileres.c.fin,
            "_estado": alquileres.c.estado,
            "_cliente": relationship(
                Cliente,
                foreign_keys=[alquileres.c.cliente_documento],
                lazy="selectin",
                overlaps="_alquileres",
            ),
            "_traje": relationship(
                TrajeEspacial,
                foreign_keys=[alquileres.c.traje_codigo],
                lazy="selectin",
                overlaps="_alquileres",
            ),
        },
    )

    # Viaje: PK codigo. tipo distingue individual y grupal en la misma tabla.
    registro.map_imperatively(
        Viaje,
        viajes,
        polymorphic_on=viajes.c.tipo,
        polymorphic_abstract=True,
        exclude_properties=[
            viajes.c.cupo_maximo,
            viajes.c.cliente_documento,
            viajes.c.comprobante,
            viajes.c.registro_pago,
        ],
        properties={
            "_codigo": viajes.c.codigo,
            "_tipo": viajes.c.tipo,
            "_destino_nombre": viajes.c.destino_nombre,
            "_salida": viajes.c.salida,
            "_destino": relationship(
                Destino,
                foreign_keys=[viajes.c.destino_nombre],
                lazy="selectin",
                overlaps="_viajes",
            ),
            "_experiencias": relationship(
                Experiencia,
                secondary=viaje_experiencias,
                primaryjoin=viajes.c.codigo == viaje_experiencias.c.viaje_codigo,
                secondaryjoin=and_(
                    viaje_experiencias.c.experiencia_nombre == experiencias.c.nombre,
                    viaje_experiencias.c.destino_nombre == experiencias.c.destino_nombre,
                ),
                lazy="selectin",
                cascade="save-update",
            ),
        },
    )

    # ViajeIndividual: además persisten el cliente y el comprobante.
    registro.map_imperatively(
        ViajeIndividual,
        inherits=Viaje,
        polymorphic_identity="INDIVIDUAL",
        properties={
            "_cliente_documento": viajes.c.cliente_documento,
            "_comprobante": viajes.c.comprobante,
            "_cliente": relationship(
                Cliente,
                foreign_keys=[viajes.c.cliente_documento],
                lazy="selectin",
            ),
        },
    )

    # ViajeGrupal: además persisten cupo, integrantes y el texto de registro_pago.
    registro.map_imperatively(
        ViajeGrupal,
        inherits=Viaje,
        polymorphic_identity="GRUPAL",
        properties={
            "_cupo_maximo": viajes.c.cupo_maximo,
            "_registro_pago": viajes.c.registro_pago,
            "_integrantes": relationship(
                Cliente,
                secondary=viaje_integrantes,
                primaryjoin=viajes.c.codigo == viaje_integrantes.c.viaje_codigo,
                secondaryjoin=viaje_integrantes.c.cliente_documento == clientes.c.documento,
                order_by=viaje_integrantes.c.id,
                lazy="selectin",
                cascade="save-update",
            ),
        },
    )

    asegurar_base()
    metadata.create_all(motor)
    _configurado = True
