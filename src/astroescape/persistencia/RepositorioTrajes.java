package astroescape.persistencia;

import astroescape.TrajeEspacial;
import jakarta.persistence.EntityManager;

public class RepositorioTrajes {
    private final EntityManager em;

    public RepositorioTrajes(EntityManager em) {
        this.em = em;
    }

    public void guardar(TrajeEspacial traje) {
        em.persist(traje);
    }

    public TrajeEspacial buscarPorCodigo(String codigo) {
        return em.find(TrajeEspacial.class, codigo);
    }
}
