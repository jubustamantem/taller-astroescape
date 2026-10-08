package astroescape;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrajeEspacial {
    // Cada clase abre MySQL por su cuenta. Active Record.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private final String codigo;
    private final String talla;
    public int unidadesDisponibles;

    // Aquí hay una relación
    private final List<Alquiler> alquileres = new ArrayList<>();

    public TrajeEspacial(String codigo, String talla, int unidadesDisponibles) {
        if (codigo == null || codigo.isBlank() || talla == null || talla.isBlank()) {
            throw new IllegalArgumentException("Código y talla son obligatorios.");
        }
        if (unidadesDisponibles < 0) {
            throw new IllegalArgumentException("Las unidades no pueden iniciar en negativo.");
        }
        this.codigo = codigo;
        this.talla = talla;
        this.unidadesDisponibles = unidadesDisponibles;
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
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 """
                 INSERT INTO trajes (codigo, talla, unidades_disponibles)
                 VALUES (?, ?, ?) AS nuevos
                 ON DUPLICATE KEY UPDATE
                     talla = nuevos.talla,
                     unidades_disponibles = nuevos.unidades_disponibles
                 """
             )) {
            sentencia.setString(1, codigo);
            sentencia.setString(2, talla);
            sentencia.setInt(3, unidadesDisponibles);
            sentencia.executeUpdate();
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el traje.", error);
        }
    }

    public static TrajeEspacial buscar(String codigo) {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT codigo, talla, unidades_disponibles FROM trajes WHERE codigo = ?"
             )) {
            sentencia.setString(1, codigo);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                return new TrajeEspacial(
                    filas.getString("codigo"),
                    filas.getString("talla"),
                    filas.getInt("unidades_disponibles")
                );
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el traje.", error);
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTalla() {
        return talla;
    }

    void agregarAlquiler(Alquiler alquiler) {
        alquileres.add(alquiler);
    }

    public List<Alquiler> getAlquileres() {
        return List.copyOf(alquileres);
    }
}
