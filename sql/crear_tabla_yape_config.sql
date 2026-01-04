-- Script para crear tabla de configuración de Yape
-- Guarda la última fecha procesada para evitar duplicados

CREATE TABLE IF NOT EXISTS yape_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    clave VARCHAR(50) UNIQUE NOT NULL,
    valor TEXT,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    descripcion VARCHAR(255)
);

-- Insertar configuración inicial
INSERT INTO yape_config (clave, valor, descripcion) 
VALUES ('ultima_fecha_procesada', NULL, 'Última fecha de transacción Yape procesada')
ON DUPLICATE KEY UPDATE valor = valor;

-- Verificar
SELECT * FROM yape_config;
