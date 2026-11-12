package astroescape.persistencia;

import astroescape.Cliente;
import jakarta.persistence.EntityManager;

public class RepositorioClientes {
    private final EntityManager em;

    public RepositorioClientes(EntityManager em) {
        this.em = em;
    }

    public void guardar(Cliente cliente) {
        em.persist(cliente);
    }

    public Cliente buscarPorDocumento(String documento) {
        return em.find(Cliente.class, documento);
    }
}
