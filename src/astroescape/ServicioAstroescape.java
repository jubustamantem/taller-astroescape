package astroescape;

import astroescape.persistencia.Conexion;
import astroescape.persistencia.RepositorioAlquileres;
import astroescape.persistencia.RepositorioClientes;
import astroescape.persistencia.RepositorioDestinos;
import astroescape.persistencia.RepositorioExperiencias;
import astroescape.persistencia.RepositorioTrajes;
import astroescape.persistencia.RepositorioViajes;
import jakarta.persistence.EntityManager;

public class ServicioAstroescape implements AutoCloseable {
    private final EntityManager em;
    private final RepositorioClientes clientes;
    private final RepositorioTrajes trajes;
    private final RepositorioDestinos destinos;
    private final RepositorioExperiencias experiencias;
    private final RepositorioAlquileres alquileres;
    private final RepositorioViajes viajes;

    public ServicioAstroescape() {
        this.em = Conexion.abrir();
        this.em.getTransaction().begin();
        this.clientes = new RepositorioClientes(em);
        this.trajes = new RepositorioTrajes(em);
        this.destinos = new RepositorioDestinos(em);
        this.experiencias = new RepositorioExperiencias(em);
        this.alquileres = new RepositorioAlquileres(em);
        this.viajes = new RepositorioViajes(em);
    }

    public void registrarCliente(Cliente cliente) {
        clientes.guardar(cliente);
    }

    public Cliente consultarCliente(String documento) {
        return clientes.buscarPorDocumento(documento);
    }

    public void registrarTraje(TrajeEspacial traje) {
        trajes.guardar(traje);
    }

    public TrajeEspacial consultarTraje(String codigo) {
        return trajes.buscarPorCodigo(codigo);
    }

    public void registrarDestino(Destino destino) {
        destinos.guardar(destino);
    }

    public Destino consultarDestino(String nombre) {
        return destinos.buscarPorNombre(nombre);
    }

    public void registrarExperiencia(Experiencia experiencia) {
        experiencias.guardar(experiencia);
    }

    public Experiencia consultarExperiencia(String nombre, String destinoNombre) {
        return experiencias.buscar(nombre, destinoNombre);
    }

    public void registrarAlquiler(Alquiler alquiler) {
        alquileres.guardar(alquiler);
    }

    public Alquiler consultarAlquiler(Long identificador) {
        return alquileres.buscarPorId(identificador);
    }

    public void confirmarAlquiler(Alquiler alquiler) {
        alquiler.confirmar();
        trajes.guardar(alquiler.getTraje());
        alquileres.guardar(alquiler);
    }

    public void registrarViaje(Viaje viaje) {
        viajes.guardar(viaje);
    }

    public Viaje consultarViaje(String codigo) {
        return viajes.buscarPorCodigo(codigo);
    }

    public int calcularPrecio(ViajeIndividual viaje) {
        int precio = viaje.calcularPrecio();
        viajes.guardar(viaje);
        return precio;
    }

    public void reservarExperiencia(Destino destino, Experiencia experiencia, int personas) {
        destino.reservarExperiencia(experiencia, personas);
        experiencias.guardar(experiencia);
    }

    public void confirmarSalida(ViajeGrupal viaje) {
        viaje.confirmarSalida();
        viajes.guardar(viaje);
    }

    public void guardarCambios() {
        em.getTransaction().commit();
    }

    @Override
    public void close() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }
}
