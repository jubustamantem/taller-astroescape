package astroescape;

import java.time.LocalDate;

public class Alquiler {
    // Aquí hay una relación
    private final Cliente cliente;
    private final TrajeEspacial traje;
    private final LocalDate inicio;
    private final LocalDate fin;
    private String estado;

    public Alquiler(Cliente cliente, TrajeEspacial traje, LocalDate inicio, LocalDate fin) {
        if (cliente == null || traje == null) {
            throw new IllegalArgumentException("El alquiler vincula un cliente y un traje.");
        }
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El intervalo del alquiler no es válido.");
        }
        this.cliente = cliente;
        this.traje = traje;
        this.inicio = inicio;
        this.fin = fin;
        this.estado = "SOLICITADO";
        cliente.agregarAlquiler(this);
        traje.agregarAlquiler(this);
    }

    // Las unidades disponibles del traje se modifican aquí. ¿Qué opinan al respecto?
    public void confirmar() {
        traje.unidadesDisponibles = traje.unidadesDisponibles - 1;
        this.estado = "CONFIRMADO";
    }

    public Cliente getCliente() {
        return cliente;
    }

    public TrajeEspacial getTraje() {
        return traje;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public String getEstado() {
        return estado;
    }
}
