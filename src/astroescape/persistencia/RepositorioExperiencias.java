package astroescape.persistencia;

import astroescape.Experiencia;
import jakarta.persistence.EntityManager;

public class RepositorioExperiencias {
    private final EntityManager em;

    public RepositorioExperiencias(EntityManager em) {
        this.em = em;
    }

    public void guardar(Experiencia experiencia) {
        em.persist(experiencia);
    }

    public Experiencia buscar(String nombre, String destinoNombre) {
        return em.find(Experiencia.class, new ExperienciaId(nombre, destinoNombre));
    }
}
