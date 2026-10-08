package astroescape;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Cliente cliente = new Cliente("123", "Ada");
        cliente.guardar();

        TrajeEspacial traje = new TrajeEspacial("T-1", "M", 2);
        traje.guardar();

        Alquiler alquiler = new Alquiler(cliente, traje, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));
        alquiler.guardar();
        alquiler.confirmar();

        TrajeEspacial trajeGuardado = TrajeEspacial.buscar("T-1");
        System.out.println(trajeGuardado.unidadesDisponibles);
        System.out.println(Alquiler.buscar(alquiler.getId()).getEstado());

        Destino luna = new Destino("Luna");
        luna.guardar();
        Experiencia paseo = new Experiencia("Paseo lunar", luna, 4);
        paseo.guardar();
        luna.reservarExperiencia(paseo, 2);
        System.out.println(Experiencia.buscar("Paseo lunar", luna).getCuposRestantes());

        ViajeIndividual viaje = new ViajeIndividual("V-1", luna, LocalDate.of(2026, 11, 1), cliente);
        viaje.agregarExperiencia(paseo);
        viaje.guardar();
        viaje.calcularPrecio();
        System.out.println(ViajeIndividual.buscar("V-1").getComprobante());

        Cliente ana = new Cliente("456", "Ana");
        ana.guardar();
        ViajeGrupal grupal = new ViajeGrupal("G-1", luna, LocalDate.of(2026, 12, 1), 3);
        grupal.agregarIntegrante(cliente);
        grupal.agregarIntegrante(ana);
        grupal.guardar();
        grupal.confirmarSalida();
        System.out.println(ViajeGrupal.buscar("G-1").getRegistroPago());
    }
}
