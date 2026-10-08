package astroescape;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    // Cada clase abre MySQL por su cuenta. Active Record.
    // Estos datos tienen que coincidir con la conexión de Workbench.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private final String documento;
    private final String nombre;

    // Aquí hay una relación
    private final List<Alquiler> alquileres = new ArrayList<>();

    public Cliente(String documento, String nombre) {
        if (documento == null || documento.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Documento y nombre son obligatorios.");
        }
        this.documento = documento;
        this.nombre = nombre;
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
                 INSERT INTO clientes (documento, nombre) VALUES (?, ?) AS nuevos
                 ON DUPLICATE KEY UPDATE nombre = nuevos.nombre
                 """
             )) {
            sentencia.setString(1, documento);
            sentencia.setString(2, nombre);
            sentencia.executeUpdate();
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el cliente.", error);
        }
    }

    public static Cliente buscar(String documento) {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT documento, nombre FROM clientes WHERE documento = ?"
             )) {
            sentencia.setString(1, documento);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                return new Cliente(filas.getString("documento"), filas.getString("nombre"));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el cliente.", error);
        }
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    void agregarAlquiler(Alquiler alquiler) {
        alquileres.add(alquiler);
    }

    public List<Alquiler> getAlquileres() {
        return List.copyOf(alquileres);
    }
}
