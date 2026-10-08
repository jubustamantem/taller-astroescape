package astroescape;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Experiencia {
    // Cada clase abre MySQL por su cuenta. Active Record.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private final String nombre;

    // Aquí hay una relación
    private final Destino destino;

    private int cuposRestantes;

    public Experiencia(String nombre, Destino destino, int cuposRestantes) {
        this(nombre, destino, cuposRestantes, true);
    }

    private Experiencia(String nombre, Destino destino, int cuposRestantes, boolean registrar) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La experiencia debe tener nombre.");
        }
        if (destino == null) {
            throw new IllegalArgumentException("La experiencia pertenece a un destino.");
        }
        if (cuposRestantes < 0) {
            throw new IllegalArgumentException("Los cupos no pueden iniciar en negativo.");
        }
        this.nombre = nombre;
        // Aquí hay una relación
        this.destino = destino;
        this.cuposRestantes = cuposRestantes;
        if (registrar) {
            destino.agregarExperiencia(this);
        }
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
                 INSERT INTO experiencias (nombre, destino_nombre, cupos_restantes)
                 VALUES (?, ?, ?) AS nuevos
                 ON DUPLICATE KEY UPDATE cupos_restantes = nuevos.cupos_restantes
                 """
             )) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, destino.getNombre());
            sentencia.setInt(3, cuposRestantes);
            sentencia.executeUpdate();
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar la experiencia.", error);
        }
    }

    public static Experiencia buscar(String nombre, Destino destino) {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 """
                 SELECT nombre, cupos_restantes
                 FROM experiencias
                 WHERE nombre = ? AND destino_nombre = ?
                 """
             )) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, destino.getNombre());
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                // No usamos el constructor público: volvería a registrar la experiencia en el destino.
                return new Experiencia(filas.getString("nombre"), destino, filas.getInt("cupos_restantes"), false);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar la experiencia.", error);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public Destino getDestino() {
        return destino;
    }

    public int getCuposRestantes() {
        return cuposRestantes;
    }

    public void setCuposRestantes(int cuposRestantes) {
        this.cuposRestantes = cuposRestantes;
    }
}
