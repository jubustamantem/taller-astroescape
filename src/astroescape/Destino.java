package astroescape;

import java.util.ArrayList;
import java.util.List;

public class Destino {
    private String nombre;

    // Aquí hay una relación
    private List<Viaje> viajes = new ArrayList<>();

    // Aquí hay una relación
    private List<Experiencia> experiencias = new ArrayList<>();

    private Destino() {
    }

    public Destino(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El destino debe tener nombre.");
        }
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    void agregarViaje(Viaje viaje) {
        viajes.add(viaje);
    }

    void agregarExperiencia(Experiencia experiencia) {
        experiencias.add(experiencia);
    }

    // Aquí, el destino modifica los cupos restantes de la experiencia. ¿Qué opinan al respecto?
    public void reservarExperiencia(Experiencia experiencia, int personas) {
        if (experiencia == null || !experiencias.contains(experiencia)) {
            throw new IllegalArgumentException("La experiencia no pertenece a este destino.");
        }
        if (personas <= 0) {
            throw new IllegalArgumentException("Debe reservarse al menos un cupo.");
        }
        if (experiencia.getCuposRestantes() >= personas) {
            experiencia.setCuposRestantes(experiencia.getCuposRestantes() - personas);
        }
    }

    public List<Viaje> getViajes() {
        return List.copyOf(viajes);
    }

    public List<Experiencia> getExperiencias() {
        return List.copyOf(experiencias);
    }
}
