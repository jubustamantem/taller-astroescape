package astroescape.persistencia;

import java.io.Serializable;
import java.util.Objects;

public class ExperienciaId implements Serializable {
    private String nombre;
    private String destino;

    public ExperienciaId() {
    }

    public ExperienciaId(String nombre, String destino) {
        this.nombre = nombre;
        this.destino = destino;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    @Override
    public boolean equals(Object otro) {
        if (!(otro instanceof ExperienciaId)) {
            return false;
        }
        ExperienciaId id = (ExperienciaId) otro;
        return Objects.equals(nombre, id.nombre) && Objects.equals(destino, id.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, destino);
    }
}
