package astroescape;

import java.time.LocalDate;

// Aquí hay una relación
public class ViajeIndividual extends Viaje {
    // Aquí hay una relación
    private Cliente cliente;

    private String comprobante;

    private ViajeIndividual() {
    }

    public ViajeIndividual(String codigo, Destino destino, LocalDate salida, Cliente cliente) {
        super(codigo, destino, salida);
        if (cliente == null) {
            throw new IllegalArgumentException("El viaje individual exige un cliente.");
        }
        this.cliente = cliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getComprobante() {
        return comprobante;
    }

    // Este método calcula el precio y prepara el comprobante. ¿Qué opinan al respecto?
    public int calcularPrecio() {
        int precio = 1200;
        this.comprobante = "VIAJE " + getCodigo()
            + " CLIENTE " + cliente.getNombre()
            + " TOTAL " + precio;
        return precio;
    }
}
