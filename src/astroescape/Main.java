package astroescape;

import astroescape.persistencia.Conexion;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Cliente ada = new Cliente("123", "Ada");
        Cliente ana = new Cliente("456", "Ana");
        TrajeEspacial traje = new TrajeEspacial("T-1", "M", 2);
        Alquiler alquiler = new Alquiler(ada, traje, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));
        Destino luna = new Destino("Luna");
        Experiencia paseo = new Experiencia("Paseo lunar", luna, 4);
        ViajeIndividual viaje = new ViajeIndividual("V-1", luna, LocalDate.of(2026, 11, 1), ada);
        viaje.agregarExperiencia(paseo);
        ViajeGrupal grupal = new ViajeGrupal("G-1", luna, LocalDate.of(2026, 12, 1), 3);
        grupal.agregarIntegrante(ada);
        grupal.agregarIntegrante(ana);

        Long alquilerId;
        try (ServicioAstroescape servicio = new ServicioAstroescape()) {
            servicio.registrarCliente(ada);
            servicio.registrarCliente(ana);
            servicio.registrarTraje(traje);
            servicio.registrarAlquiler(alquiler);
            servicio.registrarDestino(luna);
            servicio.registrarExperiencia(paseo);
            servicio.registrarViaje(viaje);
            servicio.registrarViaje(grupal);
            servicio.guardarCambios();
            alquilerId = alquiler.getId();
        }

        try (ServicioAstroescape servicio = new ServicioAstroescape()) {
            System.out.println(servicio.consultarCliente("123").getNombre());
            System.out.println(servicio.consultarTraje("T-1").unidadesDisponibles);
            System.out.println(servicio.consultarAlquiler(alquilerId).getEstado());

            Destino lunaDb = servicio.consultarDestino("Luna");
            Experiencia paseoDb = lunaDb.getExperiencias().stream()
                .filter(item -> item.getNombre().equals("Paseo lunar"))
                .findFirst()
                .orElseThrow();
            servicio.reservarExperiencia(lunaDb, paseoDb, 2);

            Alquiler alquilerDb = servicio.consultarAlquiler(alquilerId);
            servicio.confirmarAlquiler(alquilerDb);

            ViajeIndividual viajeDb = (ViajeIndividual) servicio.consultarViaje("V-1");
            System.out.println(servicio.calcularPrecio(viajeDb));
            System.out.println(viajeDb.getComprobante());

            ViajeGrupal grupalDb = (ViajeGrupal) servicio.consultarViaje("G-1");
            servicio.confirmarSalida(grupalDb);
            System.out.println(grupalDb.getRegistroPago());
            servicio.guardarCambios();
        }

        try (ServicioAstroescape servicio = new ServicioAstroescape()) {
            System.out.println(servicio.consultarTraje("T-1").unidadesDisponibles);
            System.out.println(servicio.consultarAlquiler(alquilerId).getEstado());
            System.out.println(servicio.consultarExperiencia("Paseo lunar", "Luna").getCuposRestantes());
            ViajeGrupal grupalDb = (ViajeGrupal) servicio.consultarViaje("G-1");
            System.out.println(grupalDb.getIntegrantes().stream()
                .map(Cliente::getNombre)
                .collect(Collectors.joining(", ")));
        } finally {
            Conexion.cerrar();
        }
    }
}
