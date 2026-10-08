package astroescape.persistencia;

import astroescape.Alquiler;
import jakarta.persistence.EntityManager;

public class RepositorioAlquileres {
    private final EntityManager em;

    public RepositorioAlquileres(EntityManager em) {
        this.em = em;
    }

    public void guardar(Alquiler alquiler) {
        em.persist(alquiler);
    }

    public Alquiler buscarPorId(Long identificador) {
        return em.find(Alquiler.class, identificador);
    }
}
