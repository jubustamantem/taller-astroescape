-- Ejecutar en MySQL Workbench antes de usar las clases.
-- Crea la base astroescape y las tablas que el Active Record espera.

CREATE DATABASE IF NOT EXISTS astroescape
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE astroescape;

CREATE TABLE IF NOT EXISTS clientes (
  documento VARCHAR(50) PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS trajes (
  codigo VARCHAR(50) PRIMARY KEY,
  talla VARCHAR(20) NOT NULL,
  unidades_disponibles INT NOT NULL
);

CREATE TABLE IF NOT EXISTS destinos (
  nombre VARCHAR(150) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS experiencias (
  nombre VARCHAR(150) NOT NULL,
  destino_nombre VARCHAR(150) NOT NULL,
  cupos_restantes INT NOT NULL,
  PRIMARY KEY (nombre, destino_nombre)
);

CREATE TABLE IF NOT EXISTS alquileres (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cliente_documento VARCHAR(50) NOT NULL,
  traje_codigo VARCHAR(50) NOT NULL,
  inicio DATE NOT NULL,
  fin DATE NOT NULL,
  estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS viajes (
  codigo VARCHAR(50) PRIMARY KEY,
  tipo VARCHAR(20) NOT NULL,
  destino_nombre VARCHAR(150) NOT NULL,
  salida DATE NOT NULL,
  cupo_maximo INT NULL,
  cliente_documento VARCHAR(50) NULL,
  comprobante VARCHAR(255) NULL,
  registro_pago TEXT NULL
);

CREATE TABLE IF NOT EXISTS viaje_experiencias (
  viaje_codigo VARCHAR(50) NOT NULL,
  experiencia_nombre VARCHAR(150) NOT NULL,
  PRIMARY KEY (viaje_codigo, experiencia_nombre)
);

CREATE TABLE IF NOT EXISTS viaje_integrantes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  viaje_codigo VARCHAR(50) NOT NULL,
  cliente_documento VARCHAR(50) NOT NULL,
  UNIQUE KEY uk_viaje_cliente (viaje_codigo, cliente_documento)
);

CREATE TABLE IF NOT EXISTS pagos (
  documento VARCHAR(50),
  valor INT
);
