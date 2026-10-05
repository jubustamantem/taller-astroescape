package astroescape;

public class Experiencia {
    private final String nombre;

    // Aquí hay una relación
    private final Destino destino;

    private int cuposRestantes;

    public Experiencia(String nombre, Destino destino, int cuposRestantes) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La experiencia debe tener nombre.");
        }
        if (destino == null) {
            throw new IllegalArgumentException("La experiencia pertenece a un destino.");
        }
        if (cuposRestantes < 0) {
            throw new IllegalArgumentException("Los cupos no pueden iniciar en negativo.");
        }
        this.nombre = nombre;
        this.destino = destino;
        this.cuposRestantes = cuposRestantes;
        destino.agregarExperiencia(this);
    }

    public String getNombre() {
        return nombre;
    }

    public Destino getDestino() {
        return destino;
    }

    public int getCuposRestantes() {
        return cuposRestantes;
    }

    public void setCuposRestantes(int cuposRestantes) {
        this.cuposRestantes = cuposRestantes;
    }
}
