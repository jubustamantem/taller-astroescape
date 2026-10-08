package astroescape.persistencia;

import astroescape.Destino;
import jakarta.persistence.EntityManager;

public class RepositorioDestinos {
    private final EntityManager em;

    public RepositorioDestinos(EntityManager em) {
        this.em = em;
    }

    public void guardar(Destino destino) {
        em.persist(destino);
    }

    public Destino buscarPorNombre(String nombre) {
        return em.find(Destino.class, nombre);
    }
}
