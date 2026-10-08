package astroescape;

import java.time.LocalDate;

public class Alquiler {
    private Long id;

    // Aquí hay una relación
    private Cliente cliente;
    private TrajeEspacial traje;
    private LocalDate inicio;
    private LocalDate fin;
    private String estado;

    private Alquiler() {
    }

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

    public Long getId() {
        return id;
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
