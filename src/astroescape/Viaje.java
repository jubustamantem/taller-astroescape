package astroescape;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Viaje {
    private String codigo;

    // Aquí hay una relación
    private Destino destino;

    private LocalDate salida;

    // Aquí hay una relación
    private List<Experiencia> experiencias = new ArrayList<>();

    protected Viaje() {
    }

    protected Viaje(String codigo, Destino destino, LocalDate salida) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El viaje debe tener código.");
        }
        if (destino == null || salida == null) {
            throw new IllegalArgumentException("Destino y fecha de salida son obligatorios.");
        }
        this.codigo = codigo;
        this.destino = destino;
        this.salida = salida;
        destino.agregarViaje(this);
    }

    public String getCodigo() {
        return codigo;
    }

    public Destino getDestino() {
        return destino;
    }

    public LocalDate getSalida() {
        return salida;
    }

    public void agregarExperiencia(Experiencia experiencia) {
        if (experiencia == null) {
            throw new IllegalArgumentException("La experiencia es obligatoria.");
        }
        if (experiencia.getDestino() != destino) {
            throw new IllegalArgumentException("La experiencia no corresponde al destino del viaje.");
        }
        experiencias.add(experiencia);
    }

    public List<Experiencia> getExperiencias() {
        return List.copyOf(experiencias);
    }
}
