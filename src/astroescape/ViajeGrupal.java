package astroescape;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Aquí hay una relación
public class ViajeGrupal extends Viaje {
    private final int cupoMaximo;

    // Aquí hay una relación
    private final List<Cliente> integrantes = new ArrayList<>();

    private String registroPago;

    public ViajeGrupal(String codigo, Destino destino, LocalDate salida, int cupoMaximo) {
        super(codigo, destino, salida);
        if (cupoMaximo < 2) {
            throw new IllegalArgumentException("Un viaje grupal admite al menos dos integrantes.");
        }
        this.cupoMaximo = cupoMaximo;
    }

    public void guardar() {
        try (Connection conexion = conectar()) {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                """
                INSERT INTO viajes (
                    codigo, tipo, destino_nombre, salida, cupo_maximo,
                    cliente_documento, comprobante, registro_pago
                ) VALUES (?, 'GRUPAL', ?, ?, ?, NULL, NULL, ?) AS nuevos
                ON DUPLICATE KEY UPDATE
                    tipo = nuevos.tipo,
                    destino_nombre = nuevos.destino_nombre,
                    salida = nuevos.salida,
                    cupo_maximo = nuevos.cupo_maximo,
                    cliente_documento = nuevos.cliente_documento,
                    comprobante = nuevos.comprobante,
                    registro_pago = nuevos.registro_pago
                """
            )) {
                sentencia.setString(1, getCodigo());
                sentencia.setString(2, getDestino().getNombre());
                sentencia.setDate(3, Date.valueOf(getSalida()));
                sentencia.setInt(4, cupoMaximo);
                sentencia.setString(5, registroPago);
                sentencia.executeUpdate();
            }
            guardarExperiencias(conexion);
            try (PreparedStatement borrar = conexion.prepareStatement(
                "DELETE FROM viaje_integrantes WHERE viaje_codigo = ?"
            )) {
                borrar.setString(1, getCodigo());
                borrar.executeUpdate();
            }
            try (PreparedStatement insertar = conexion.prepareStatement(
                """
                INSERT INTO viaje_integrantes (viaje_codigo, cliente_documento)
                VALUES (?, ?)
                """
            )) {
                for (Cliente integrante : integrantes) {
                    insertar.setString(1, getCodigo());
                    insertar.setString(2, integrante.getDocumento());
                    insertar.executeUpdate();
                }
            }
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el viaje grupal.", error);
        }
    }

    public static ViajeGrupal buscar(String codigo) {
        try (Connection conexion = conectar()) {
            String destinoNombre;
            LocalDate salida;
            int cupo;
            String registro;
            try (PreparedStatement sentencia = conexion.prepareStatement(
                """
                SELECT codigo, destino_nombre, salida, cupo_maximo, registro_pago
                FROM viajes
                WHERE codigo = ? AND tipo = 'GRUPAL'
                """
            )) {
                sentencia.setString(1, codigo);
                try (ResultSet filas = sentencia.executeQuery()) {
                    if (!filas.next()) {
                        return null;
                    }
                    destinoNombre = filas.getString("destino_nombre");
                    salida = filas.getDate("salida").toLocalDate();
                    cupo = filas.getInt("cupo_maximo");
                    registro = filas.getString("registro_pago");
                }
            }
            ViajeGrupal viaje = new ViajeGrupal(codigo, new Destino(destinoNombre), salida, cupo);
            viaje.registroPago = registro;
            try (PreparedStatement sentencia = conexion.prepareStatement(
                """
                SELECT c.documento, c.nombre
                FROM viaje_integrantes vi
                JOIN clientes c ON c.documento = vi.cliente_documento
                WHERE vi.viaje_codigo = ?
                ORDER BY vi.id
                """
            )) {
                sentencia.setString(1, codigo);
                try (ResultSet filas = sentencia.executeQuery()) {
                    while (filas.next()) {
                        viaje.agregarIntegrante(new Cliente(
                            filas.getString("documento"),
                            filas.getString("nombre")
                        ));
                    }
                }
            }
            try (PreparedStatement sentencia = conexion.prepareStatement(
                """
                SELECT e.nombre, e.cupos_restantes
                FROM viaje_experiencias ve
                JOIN experiencias e ON e.nombre = ve.experiencia_nombre
                WHERE ve.viaje_codigo = ? AND e.destino_nombre = ?
                """
            )) {
                sentencia.setString(1, codigo);
                sentencia.setString(2, destinoNombre);
                try (ResultSet filas = sentencia.executeQuery()) {
                    while (filas.next()) {
                        viaje.agregarExperiencia(new Experiencia(
                            filas.getString("nombre"),
                            viaje.getDestino(),
                            filas.getInt("cupos_restantes")
                        ));
                    }
                }
            }
            return viaje;
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el viaje grupal.", error);
        }
    }

    public void agregarIntegrante(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El integrante es obligatorio.");
        }
        if (integrantes.size() >= cupoMaximo) {
            throw new IllegalStateException("El viaje grupal ya completó su cupo.");
        }
        integrantes.add(cliente);
    }

    // Este método registra el pago en la base de datos. ¿Qué opinan al respecto?
    public void confirmarSalida() {
        if (integrantes.size() < 2) {
            throw new IllegalStateException("El viaje grupal necesita al menos dos integrantes.");
        }
        Cliente titular = integrantes.get(0);
        int total = integrantes.size() * 500;
        this.registroPago = "INSERT INTO pagos(documento, valor) VALUES ('"
            + titular.getDocumento() + "', " + total + ")";
        // La regla de negocio abre MySQL y ejecuta SQL armado en la clase.
        try (Connection conexion = DriverManager.getConnection(
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
            USUARIO,
            CONTRASENA
        )) {
            conexion.setAutoCommit(false);
            try (PreparedStatement pagos = conexion.prepareStatement(registroPago)) {
                pagos.executeUpdate();
            }
            try (PreparedStatement viajes = conexion.prepareStatement(
                "UPDATE viajes SET registro_pago = ? WHERE codigo = ?"
            )) {
                viajes.setString(1, registroPago);
                viajes.setString(2, getCodigo());
                viajes.executeUpdate();
            }
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo confirmar la salida.", error);
        }
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<Cliente> getIntegrantes() {
        return List.copyOf(integrantes);
    }

    public String getRegistroPago() {
        return registroPago;
    }
}
