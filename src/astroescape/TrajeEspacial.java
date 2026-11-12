package astroescape;

import java.util.ArrayList;
import java.util.List;

public class TrajeEspacial {
    private String codigo;
    private String talla;
    public int unidadesDisponibles;

    // Aquí hay una relación
    private List<Alquiler> alquileres = new ArrayList<>();

    private TrajeEspacial() {
    }

    public TrajeEspacial(String codigo, String talla, int unidadesDisponibles) {
        if (codigo == null || codigo.isBlank() || talla == null || talla.isBlank()) {
            throw new IllegalArgumentException("Código y talla son obligatorios.");
        }
        if (unidadesDisponibles < 0) {
            throw new IllegalArgumentException("Las unidades no pueden iniciar en negativo.");
        }
        this.codigo = codigo;
        this.talla = talla;
        this.unidadesDisponibles = unidadesDisponibles;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTalla() {
        return talla;
    }

    void agregarAlquiler(Alquiler alquiler) {
        alquileres.add(alquiler);
    }

    public List<Alquiler> getAlquileres() {
        return List.copyOf(alquileres);
    }
}
