package astroescape;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String documento;
    private String nombre;

    // Aquí hay una relación
    private List<Alquiler> alquileres = new ArrayList<>();

    private Cliente() {
    }

    public Cliente(String documento, String nombre) {
        if (documento == null || documento.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Documento y nombre son obligatorios.");
        }
        this.documento = documento;
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    void agregarAlquiler(Alquiler alquiler) {
        alquileres.add(alquiler);
    }

    public List<Alquiler> getAlquileres() {
        return List.copyOf(alquileres);
    }
}
