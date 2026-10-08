package astroescape;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Viaje {
    // Cada clase abre MySQL por su cuenta. Active Record.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private final String codigo;

    // Aquí hay una relación
    private final Destino destino;

    private final LocalDate salida;

    // Aquí hay una relación
    private final List<Experiencia> experiencias = new ArrayList<>();

    protected Viaje(String codigo, Destino destino, LocalDate salida) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El viaje debe tener código.");
        }
        if (destino == null || salida == null) {
            throw new IllegalArgumentException("Destino y fecha de salida son obligatorios.");
        }
        this.codigo = codigo;
        // Aquí hay una relación
        this.destino = destino;
        this.salida = salida;
        // Aquí hay una relación
        destino.agregarViaje(this);
    }

    protected static Connection conectar() {
        // La jerarquía abre la conexión.
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

    protected void guardarExperiencias(Connection conexion) throws SQLException {
        try (PreparedStatement borrar = conexion.prepareStatement(
            "DELETE FROM viaje_experiencias WHERE viaje_codigo = ?"
        )) {
            borrar.setString(1, codigo);
            borrar.executeUpdate();
        }
        try (PreparedStatement insertar = conexion.prepareStatement(
            """
            INSERT INTO viaje_experiencias (viaje_codigo, experiencia_nombre)
            VALUES (?, ?)
            """
        )) {
            for (Experiencia experiencia : experiencias) {
                insertar.setString(1, codigo);
                insertar.setString(2, experiencia.getNombre());
                insertar.executeUpdate();
            }
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public Destino getDestino() {
        return destino;
    }

    public LocalDate getSalida() {
        return salida;
    }

    public void agregarExperiencia(Experiencia experiencia) {
        if (experiencia == null) {
            throw new IllegalArgumentException("La experiencia es obligatoria.");
        }
        if (experiencia.getDestino() != destino) {
            throw new IllegalArgumentException("La experiencia no corresponde al destino del viaje.");
        }
        experiencias.add(experiencia);
    }

    public List<Experiencia> getExperiencias() {
        return List.copyOf(experiencias);
    }
}
