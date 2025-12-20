-- ===============================================================
-- SCRIPT SQL PARA SISTEMA DE AUTOMATIZACIÓN DE COBROS - FNET
-- Ejecutar este script en tu base de datos MySQL/MariaDB
-- ===============================================================

-- 1. TABLA DE CONFIGURACIÓN DEL SISTEMA
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id_config INT PRIMARY KEY AUTO_INCREMENT,
    clave VARCHAR(50) UNIQUE NOT NULL,
    valor VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Valores por defecto
INSERT IGNORE INTO configuracion_sistema (clave, valor, descripcion) VALUES
('plazo_pago_dias', '21', 'Días de plazo tras vencimiento antes de corte (3 semanas)'),
('dias_recordatorio', '0', 'Días después del vencimiento para enviar recordatorio (0 = mismo día)'),
('whatsapp_habilitado', '0', 'Activar envío de WhatsApp (1=sí, 0=no)'),
('router_habilitado', '0', 'Activar corte automático en router (1=sí, 0=no)'),
('callmebot_apikey', '', 'API Key de CallMeBot para WhatsApp'),
('mikrotik_ip', '', 'IP del router MikroTik principal'),
('mikrotik_usuario', 'admin', 'Usuario de acceso al router MikroTik'),
('mikrotik_password', '', 'Contraseña del router MikroTik');


-- 2. TABLA DE NOTIFICACIONES PENDIENTES (COLA DE WHATSAPP)
CREATE TABLE IF NOT EXISTS notificacion_pendiente (
    id_notificacion INT PRIMARY KEY AUTO_INCREMENT,
    id_suscripcion INT NOT NULL,
    tipo ENUM('RECORDATORIO', 'ULTIMATUM', 'CORTE') NOT NULL,
    mensaje TEXT NOT NULL,
    telefono VARCHAR(20),
    fecha_programada DATE NOT NULL,
    fecha_enviado DATETIME NULL,
    estado ENUM('PENDIENTE', 'ENVIADO', 'ERROR', 'SIN_TELEFONO') DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_estado_fecha (estado, fecha_programada),
    INDEX idx_suscripcion (id_suscripcion),
    FOREIGN KEY (id_suscripcion) REFERENCES suscripcion(id_suscripcion) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 3. TABLA DE ALERTAS PARA EL GERENTE (BANDEJA DE ENTRADA)
CREATE TABLE IF NOT EXISTS alerta_gerente (
    id_alerta INT PRIMARY KEY AUTO_INCREMENT,
    tipo ENUM('SIN_TELEFONO', 'PAGO_MANUAL', 'CORTE_FALLIDO', 'RECONEXION_FALLO', 'OTRO') NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    mensaje TEXT NOT NULL,
    id_suscripcion INT NULL,
    leido TINYINT DEFAULT 0,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_leido (leido),
    INDEX idx_fecha (fecha_creacion),
    FOREIGN KEY (id_suscripcion) REFERENCES suscripcion(id_suscripcion) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 4. AGREGAR COLUMNA IP_CLIENTE A SUSCRIPCION (SI NO EXISTE)
-- Esta columna almacena la IP del cliente para poder cortar/reconectar
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'suscripcion' 
               AND COLUMN_NAME = 'ip_cliente');

SET @sqlStmt := IF(@exist = 0, 
    'ALTER TABLE suscripcion ADD COLUMN ip_cliente VARCHAR(45) NULL COMMENT "IP del cliente para corte/reconexión"',
    'SELECT "Columna ip_cliente ya existe"');
    
PREPARE stmt FROM @sqlStmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 5. AGREGAR COLUMNA FECHA_ULTIMATUM A SUSCRIPCION (SI NO EXISTE)
SET @exist2 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'suscripcion' 
               AND COLUMN_NAME = 'fecha_ultimatum');

SET @sqlStmt2 := IF(@exist2 = 0, 
    'ALTER TABLE suscripcion ADD COLUMN fecha_ultimatum DATE NULL COMMENT "Fecha límite antes del corte"',
    'SELECT "Columna fecha_ultimatum ya existe"');
    
PREPARE stmt2 FROM @sqlStmt2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;


-- 6. VERIFICACIÓN: Listar tablas creadas
SELECT 'TABLAS CREADAS:' as info;
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME IN ('configuracion_sistema', 'notificacion_pendiente', 'alerta_gerente');

-- 7. VERIFICACIÓN: Mostrar configuraciones
SELECT 'CONFIGURACIONES INICIALES:' as info;
SELECT clave, valor, descripcion FROM configuracion_sistema ORDER BY clave;

-- ===============================================================
-- FIN DEL SCRIPT
-- ===============================================================
