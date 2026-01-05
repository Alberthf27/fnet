-- ===============================================================
-- SCRIPT SQL PARA SISTEMA DE PERMISOS - FNET
-- Ejecutar en la base de datos para habilitar gestión de permisos
-- ===============================================================

-- 1. Tabla de permisos disponibles en el sistema
CREATE TABLE IF NOT EXISTS permiso (
    id_permiso INT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(50) NOT NULL UNIQUE,      -- Ej: 'PANEL_PAGOS', 'BTN_ELIMINAR'
    descripcion VARCHAR(100) NOT NULL,       -- Ej: 'Acceso a panel de pagos'
    categoria VARCHAR(30) NOT NULL           -- Ej: 'PANEL', 'BOTON', 'FUNCION'
);

-- 2. Tabla de permisos por rol
CREATE TABLE IF NOT EXISTS rol_permiso (
    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE CASCADE,
    FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE CASCADE
);

-- 3. Insertar permisos base del sistema
INSERT INTO permiso (codigo, descripcion, categoria) VALUES
-- PANELES PRINCIPALES
('PANEL_CONTRATOS', 'Panel de Contratos/Suscripciones', 'PANEL'),
('PANEL_PAGOS', 'Panel de Pagos y Caja', 'PANEL'),
('PANEL_CLIENTES', 'Panel de Directorio de Clientes', 'PANEL'),
('PANEL_INSTALACIONES', 'Panel de Instalaciones', 'PANEL'),
('PANEL_FINANZAS', 'Panel de Finanzas', 'PANEL'),
('PANEL_INVENTARIO', 'Panel de Inventario', 'PANEL'),
('PANEL_YAPE', 'Pestaña de Procesar Yape', 'PANEL'),
('PANEL_BANDEJA', 'Bandeja de Alertas', 'PANEL'),
('PANEL_CONFIG', 'Configuración del Sistema', 'PANEL'),
('PANEL_USUARIOS', 'Gestión de Usuarios', 'PANEL'),

-- FUNCIONES
('FUNCION_EDITAR_CONTRATO', 'Editar contratos existentes', 'FUNCION'),
('FUNCION_ELIMINAR_CONTRATO', 'Dar de baja contratos', 'FUNCION'),
('FUNCION_NUEVO_CONTRATO', 'Crear nuevos contratos', 'FUNCION'),
('FUNCION_COBRAR', 'Registrar cobros/pagos', 'FUNCION'),
('FUNCION_REPORTES', 'Generar reportes', 'FUNCION')
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);

-- 4. Asignar TODOS los permisos al rol GERENTE (id_rol = 1)
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 1, id_permiso FROM permiso
ON DUPLICATE KEY UPDATE id_rol = id_rol;

-- 5. Asignar permisos básicos al rol EMPLEADO (id_rol = 2)
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 2, id_permiso FROM permiso 
WHERE codigo IN ('PANEL_CONTRATOS', 'PANEL_PAGOS', 'PANEL_CLIENTES', 'FUNCION_COBRAR')
ON DUPLICATE KEY UPDATE id_rol = id_rol;

-- 6. Ver permisos asignados
SELECT r.nombre AS rol, p.codigo, p.descripcion, p.categoria
FROM rol_permiso rp
JOIN rol r ON rp.id_rol = r.id_rol
JOIN permiso p ON rp.id_permiso = p.id_permiso
ORDER BY r.nombre, p.categoria, p.codigo;
