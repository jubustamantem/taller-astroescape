package astroescape;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// Aquí hay una relación
public class ViajeIndividual extends Viaje {
    // Aquí hay una relación
    private final Cliente cliente;

    private String comprobante;

    public ViajeIndividual(String codigo, Destino destino, LocalDate salida, Cliente cliente) {
        super(codigo, destino, salida);
        if (cliente == null) {
            throw new IllegalArgumentException("El viaje individual exige un cliente.");
        }
        // Aquí hay una relación
        this.cliente = cliente;
    }

    public void guardar() {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                 """
                 INSERT INTO viajes (
                     codigo, tipo, destino_nombre, salida, cupo_maximo,
                     cliente_documento, comprobante, registro_pago
                 ) VALUES (?, 'INDIVIDUAL', ?, ?, NULL, ?, ?, NULL) AS nuevos
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
            sentencia.setString(4, cliente.getDocumento());
            sentencia.setString(5, comprobante);
            sentencia.executeUpdate();
            guardarExperiencias(conexion);
            conexion.commit();
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el viaje individual.", error);
        }
    }

    public static ViajeIndividual buscar(String codigo) {
        try (Connection conexion = conectar()) {
            String destinoNombre;
            LocalDate salida;
            String documentoCliente;
            String comprobanteGuardado;
            try (PreparedStatement sentencia = conexion.prepareStatement(
                """
                SELECT codigo, destino_nombre, salida, cliente_documento, comprobante
                FROM viajes
                WHERE codigo = ? AND tipo = 'INDIVIDUAL'
                """
            )) {
                sentencia.setString(1, codigo);
                try (ResultSet filas = sentencia.executeQuery()) {
                    if (!filas.next()) {
                        return null;
                    }
                    destinoNombre = filas.getString("destino_nombre");
                    salida = filas.getDate("salida").toLocalDate();
                    documentoCliente = filas.getString("cliente_documento");
                    comprobanteGuardado = filas.getString("comprobante");
                }
            }
            Cliente cliente = buscarCliente(conexion, documentoCliente);
            ViajeIndividual viaje = new ViajeIndividual(codigo, new Destino(destinoNombre), salida, cliente);
            viaje.comprobante = comprobanteGuardado;
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
            throw new IllegalStateException("No se pudo consultar el viaje individual.", error);
        }
    }

    private static Cliente buscarCliente(Connection conexion, String documento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
            "SELECT documento, nombre FROM clientes WHERE documento = ?"
        )) {
            sentencia.setString(1, documento);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    throw new IllegalArgumentException(
                        "El viaje individual apunta a un cliente que no está guardado."
                    );
                }
                return new Cliente(filas.getString("documento"), filas.getString("nombre"));
            }
        }
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getComprobante() {
        return comprobante;
    }

    // Este método calcula el precio y prepara el comprobante. ¿Qué opinan al respecto?
    public int calcularPrecio() {
        int precio = 1200;
        this.comprobante = "VIAJE " + getCodigo()
            + " CLIENTE " + cliente.getNombre()
            + " TOTAL " + precio;
        // Calcular el precio también escribe en MySQL.
        try (Connection conexion = DriverManager.getConnection(
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
            USUARIO,
            CONTRASENA
        )) {
            conexion.setAutoCommit(false);
            try (PreparedStatement sentencia = conexion.prepareStatement(
                "UPDATE viajes SET comprobante = ? WHERE codigo = ?"
            )) {
                sentencia.setString(1, comprobante);
                sentencia.setString(2, getCodigo());
                sentencia.executeUpdate();
                conexion.commit();
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo guardar el comprobante.", error);
        }
        return precio;
    }
}
