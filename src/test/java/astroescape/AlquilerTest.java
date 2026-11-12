package astroescape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class AlquilerTest {

    @Test
    void rechazaIntervaloInvalido() {
        Cliente cliente = new Cliente("100", "Ada");
        TrajeEspacial traje = new TrajeEspacial("T-1", "M", 3);
        LocalDate inicio = LocalDate.of(2026, 5, 10);
        LocalDate fin = LocalDate.of(2026, 5, 1);

        assertThrows(
            IllegalArgumentException.class,
            () -> new Alquiler(cliente, traje, inicio, fin)
        );
    }

    @Test
    void confirmarCambiaEstadoYReduceUnidades() {
        Cliente cliente = new Cliente("100", "Ada");
        TrajeEspacial traje = new TrajeEspacial("T-1", "M", 3);
        Alquiler alquiler = new Alquiler(
            cliente,
            traje,
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 10)
        );

        alquiler.confirmar();

        assertEquals("CONFIRMADO", alquiler.getEstado());
        assertEquals(2, traje.unidadesDisponibles);
    }
}
