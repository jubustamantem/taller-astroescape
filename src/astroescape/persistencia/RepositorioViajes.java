package astroescape.persistencia;

import astroescape.Viaje;
import jakarta.persistence.EntityManager;

public class RepositorioViajes {
    private final EntityManager em;

    public RepositorioViajes(EntityManager em) {
        this.em = em;
    }

    public void guardar(Viaje viaje) {
        em.persist(viaje);
    }

    public Viaje buscarPorCodigo(String codigo) {
        return em.find(Viaje.class, codigo);
    }
}
