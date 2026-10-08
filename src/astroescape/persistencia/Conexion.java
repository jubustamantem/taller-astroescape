package astroescape.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class Conexion {
    // Paso 4. La conexión y el ORM viven aquí, fuera de las clases de dominio.
    // El usuario de MySQL tiene que poder crear la base. El resto lo hace el código.
    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "";
    public static final String BASE_DE_DATOS = "astroescape";

    private static EntityManagerFactory fabrica;

    private Conexion() {
    }

    public static EntityManager abrir() {
        if (fabrica == null) {
            asegurarBase();
            fabrica = Persistence.createEntityManagerFactory("astroescape", propiedades());
        }
        return fabrica.createEntityManager();
    }

    public static void cerrar() {
        if (fabrica != null && fabrica.isOpen()) {
            fabrica.close();
        }
        fabrica = null;
    }

    private static void asegurarBase() {
        if (!BASE_DE_DATOS.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("El nombre de la base no es válido.");
        }
        String urlServidor = "jdbc:mysql://" + HOST + ":" + PUERTO
            + "/?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
        try (Connection conexion = DriverManager.getConnection(urlServidor, USUARIO, CONTRASENA);
             Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS `" + BASE_DE_DATOS + "` "
                    + "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            );
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo crear la base de datos.", error);
        }
    }

    private static Map<String, Object> propiedades() {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(
            "jakarta.persistence.jdbc.url",
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
        );
        propiedades.put("jakarta.persistence.jdbc.user", USUARIO);
        propiedades.put("jakarta.persistence.jdbc.password", CONTRASENA);
        propiedades.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        propiedades.put("hibernate.hbm2ddl.auto", "update");
        propiedades.put("hibernate.show_sql", "false");
        return propiedades;
    }
}
