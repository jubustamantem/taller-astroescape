package astroescape;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class Alquiler {
    // Cada clase abre MySQL por su cuenta. Active Record.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private Long id;

    // Aquí hay una relación
    private final Cliente cliente;
    private final TrajeEspacial traje;
    private final LocalDate inicio;
    private final LocalDate fin;
    private String estado;

    public Alquiler(Cliente cliente, TrajeEspacial traje, LocalDate inicio, LocalDate fin) {
        if (cliente == null || traje == null) {
            throw new IllegalArgumentException("El alquiler vincula un cliente y un traje.");
        }
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El intervalo del alquiler no es válido.");
        }
        // Aquí hay una relación
        this.cliente = cliente;
        this.traje = traje;
        this.inicio = inicio;
        this.fin = fin;
        this.estado = "SOLICITADO";
        cliente.agregarAlquiler(this);
        traje.agregarAlquiler(this);
    }

    private static Connection conectar() {
        // La clase abre la conexión.
        try {
            Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS
                    + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                USUARIO,
                CONTRASENA
            );
            conexion.setAutoCommit(false);
            return conexion;
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo abrir MySQL.", error);
        }
    }

    public void guardar() {
        try (Connection conexion = conectar()) {
            if (id == null) {
                try (PreparedStatement sentencia = conexion.prepareStatement(
                    """
                    INSERT INTO alquileres (cliente_documento, traje_codigo, inicio, fin, estado)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
                )) {
                    sentencia.setString(1, cliente.getDocumento());
                    sentencia.setString(2, traje.getCodigo());
                    sentencia.setDate(3, Date.valueOf(inicio));
                    sentencia.setDate(4, Date.valueOf(fin));
                    sentencia.setString(5, estado);
                    sentencia.executeUpdate();
                    try (ResultSet claves = sentencia.getGeneratedKeys()) {
                        if (claves.next()) {
                            id = claves.getLong(1);
                        }
                    }
                }
            } else {
                try (PreparedStatement sentencia = conexion.prepareStatement(
                    """
                    UPDATE alquileres
                    SET cliente_documento = ?, traje_codigo = ?, inicio = ?, fin = ?, estado = ?
                    WHERE id = ?
                    """
                )) {
                    sentencia.setString(1, cliente.getDocumento());
                    sentencia.setString(2, traje.getCodigo());
                    sentencia.setDate(3, Date.valueOf(inicio));
                    sentencia.setDate(4, Date.valueOf(fin));
                    sentencia.setString(5, estado);
                    sentencia.setLong(6, id);
                    sentencia.executeUpdate();
                }
            }
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el alquiler.", error);
        }
    }

    public static Alquiler buscar(long identificador) {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 """
                 SELECT id, cliente_documento, traje_codigo, inicio, fin, estado
                 FROM alquileres
                 WHERE id = ?
                 """
             )) {
            sentencia.setLong(1, identificador);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                // El alquiler consulta las tablas de Cliente y de TrajeEspacial.
                Cliente cliente = buscarCliente(conexion, filas.getString("cliente_documento"));
                TrajeEspacial traje = buscarTraje(conexion, filas.getString("traje_codigo"));
                Alquiler alquiler = new Alquiler(
                    cliente,
                    traje,
                    filas.getDate("inicio").toLocalDate(),
                    filas.getDate("fin").toLocalDate()
                );
                alquiler.id = filas.getLong("id");
                alquiler.estado = filas.getString("estado");
                return alquiler;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el alquiler.", error);
        }
    }

    private static Cliente buscarCliente(Connection conexion, String documento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
            "SELECT documento, nombre FROM clientes WHERE documento = ?"
        )) {
            sentencia.setString(1, documento);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    throw new IllegalArgumentException("El alquiler apunta a un cliente que no está guardado.");
                }
                return new Cliente(filas.getString("documento"), filas.getString("nombre"));
            }
        }
    }

    private static TrajeEspacial buscarTraje(Connection conexion, String codigo) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
            "SELECT codigo, talla, unidades_disponibles FROM trajes WHERE codigo = ?"
        )) {
            sentencia.setString(1, codigo);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    throw new IllegalArgumentException("El alquiler apunta a un traje que no está guardado.");
                }
                return new TrajeEspacial(
                    filas.getString("codigo"),
                    filas.getString("talla"),
                    filas.getInt("unidades_disponibles")
                );
            }
        }
    }

    // Las unidades disponibles del traje se modifican aquí. ¿Qué opinan al respecto?
    public void confirmar() {
        traje.unidadesDisponibles = traje.unidadesDisponibles - 1;
        this.estado = "CONFIRMADO";
        // El alquiler abre MySQL y actualiza la tabla del traje.
        try (Connection conexion = DriverManager.getConnection(
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
            USUARIO,
            CONTRASENA
        )) {
            conexion.setAutoCommit(false);
            try (PreparedStatement trajes = conexion.prepareStatement(
                """
                UPDATE trajes
                SET unidades_disponibles = unidades_disponibles - 1
                WHERE codigo = ?
                """
            )) {
                trajes.setString(1, traje.getCodigo());
                trajes.executeUpdate();
            }
            if (id != null) {
                try (PreparedStatement alquileres = conexion.prepareStatement(
                    "UPDATE alquileres SET estado = ? WHERE id = ?"
                )) {
                    alquileres.setString(1, estado);
                    alquileres.setLong(2, id);
                    alquileres.executeUpdate();
                }
            }
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo confirmar el alquiler.", error);
        }
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public TrajeEspacial getTraje() {
        return traje;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public String getEstado() {
        return estado;
    }
}
