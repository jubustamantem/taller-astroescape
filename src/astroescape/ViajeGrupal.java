package astroescape;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Aquí hay una relación
public class ViajeGrupal extends Viaje {
    private final int cupoMaximo;

    // Aquí hay una relación
    private final List<Cliente> integrantes = new ArrayList<>();

    private String registroPago;

    public ViajeGrupal(String codigo, Destino destino, LocalDate salida, int cupoMaximo) {
        super(codigo, destino, salida);
        if (cupoMaximo < 2) {
            throw new IllegalArgumentException("Un viaje grupal admite al menos dos integrantes.");
        }
        this.cupoMaximo = cupoMaximo;
    }

    public void agregarIntegrante(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El integrante es obligatorio.");
        }
        if (integrantes.size() >= cupoMaximo) {
            throw new IllegalStateException("El viaje grupal ya completó su cupo.");
        }
        integrantes.add(cliente);
    }

    // Este método registra el pago en la base de datos. ¿Qué opinan al respecto?
    public void confirmarSalida() {
        if (integrantes.size() < 2) {
            throw new IllegalStateException("El viaje grupal necesita al menos dos integrantes.");
        }
        Cliente titular = integrantes.get(0);
        int total = integrantes.size() * 500;
        this.registroPago = "INSERT INTO pagos(documento, valor) VALUES ('"
            + titular.getDocumento() + "', " + total + ")";
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<Cliente> getIntegrantes() {
        return List.copyOf(integrantes);
    }

    public String getRegistroPago() {
        return registroPago;
    }
}
