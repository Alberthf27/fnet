-- ===============================================================
-- SCRIPT SQL PARA ACTUALIZAR CODIGOS DE FACTURA - FNET
-- Actualiza el codigo_factura existente sin eliminar datos
-- ===============================================================

-- 1. Actualizar TODOS los codigo_factura existentes a formato secuencial
-- Usa el id_factura para generar códigos como 0001, 0002, 0003, etc.
UPDATE factura 
SET codigo_factura = LPAD(id_factura, 4, '0')
WHERE id_factura > 0;

-- 2. Ver los resultados
SELECT id_factura, codigo_factura, periodo_mes, monto_total 
FROM factura 
ORDER BY id_factura ASC 
LIMIT 20;

-- ===============================================================
-- NOTA: Este script actualiza los códigos SIN eliminar datos.
-- Las nuevas facturas generarán códigos secuenciales automáticamente.
-- ===============================================================
