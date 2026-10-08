package astroescape;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Destino {
    // Cada clase abre MySQL por su cuenta. Active Record.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private final String nombre;

    // Aquí hay una relación
    private final List<Viaje> viajes = new ArrayList<>();

    // Aquí hay una relación
    private final List<Experiencia> experiencias = new ArrayList<>();

    public Destino(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El destino debe tener nombre.");
        }
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
                 INSERT INTO destinos (nombre) VALUES (?) AS nuevos
                 ON DUPLICATE KEY UPDATE nombre = nuevos.nombre
                 """
             )) {
            sentencia.setString(1, nombre);
            sentencia.executeUpdate();
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el destino.", error);
        }
    }

    public static Destino buscar(String nombre) {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT nombre FROM destinos WHERE nombre = ?"
             )) {
            sentencia.setString(1, nombre);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                // El destino vuelve sin sus viajes ni sus experiencias.
                return new Destino(filas.getString("nombre"));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el destino.", error);
        }
    }

    public String getNombre() {
        return nombre;
    }

    void agregarViaje(Viaje viaje) {
        viajes.add(viaje);
    }

    void agregarExperiencia(Experiencia experiencia) {
        experiencias.add(experiencia);
    }

    // Aquí, el destino modifica los cupos restantes de la experiencia. ¿Qué opinan al respecto?
    public void reservarExperiencia(Experiencia experiencia, int personas) {
        if (experiencia == null || !experiencias.contains(experiencia)) {
            throw new IllegalArgumentException("La experiencia no pertenece a este destino.");
        }
        if (personas <= 0) {
            throw new IllegalArgumentException("Debe reservarse al menos un cupo.");
        }
        if (experiencia.getCuposRestantes() >= personas) {
            experiencia.setCuposRestantes(experiencia.getCuposRestantes() - personas);
            // El destino abre MySQL y escribe en la tabla de otra clase.
            try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS
                    + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                USUARIO,
                CONTRASENA
            )) {
                conexion.setAutoCommit(false);
                try (PreparedStatement sentencia = conexion.prepareStatement(
                    """
                    UPDATE experiencias
                    SET cupos_restantes = cupos_restantes - ?
                    WHERE nombre = ? AND destino_nombre = ?
                    """
                )) {
                    sentencia.setInt(1, personas);
                    sentencia.setString(2, experiencia.getNombre());
                    sentencia.setString(3, nombre);
                    sentencia.executeUpdate();
                    conexion.commit();
                }
            } catch (SQLException error) {
                throw new IllegalStateException("No se pudo reservar la experiencia.", error);
            }
        }
    }

    public List<Viaje> getViajes() {
        return List.copyOf(viajes);
    }

    public List<Experiencia> getExperiencias() {
        return List.copyOf(experiencias);
    }
}
