package astroescape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ViajeGrupalTest {

    @Test
    void rechazaCupoMenorQueDos() {
        Destino luna = new Destino("Luna");

        assertThrows(
            IllegalArgumentException.class,
            () -> new ViajeGrupal("VG-1", luna, LocalDate.of(2026, 6, 1), 1)
        );
    }

    @Test
    void noPermiteExcederCupo() {
        Destino luna = new Destino("Luna");
        ViajeGrupal viaje = new ViajeGrupal("VG-1", luna, LocalDate.of(2026, 6, 1), 2);
        viaje.agregarIntegrante(new Cliente("100", "Ada"));
        viaje.agregarIntegrante(new Cliente("200", "Ana"));

        assertThrows(
            IllegalStateException.class,
            () -> viaje.agregarIntegrante(new Cliente("300", "Ali"))
        );
    }

    @Test
    void confirmarSalidaExigeAlMenosDosIntegrantes() {
        Destino luna = new Destino("Luna");
        ViajeGrupal viaje = new ViajeGrupal("VG-1", luna, LocalDate.of(2026, 6, 1), 4);
        viaje.agregarIntegrante(new Cliente("100", "Ada"));

        assertThrows(IllegalStateException.class, viaje::confirmarSalida);
    }

    @Test
    void confirmarSalidaRegistraPago() {
        Destino luna = new Destino("Luna");
        ViajeGrupal viaje = new ViajeGrupal("VG-1", luna, LocalDate.of(2026, 6, 1), 4);
        viaje.agregarIntegrante(new Cliente("100", "Ada"));
        viaje.agregarIntegrante(new Cliente("200", "Ana"));

        viaje.confirmarSalida();

        assertEquals(
            "INSERT INTO pagos(documento, valor) VALUES ('100', 1000)",
            viaje.getRegistroPago()
        );
    }
}
