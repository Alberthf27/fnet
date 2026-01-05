# ===============================================================
# COMANDOS POWERSHELL PARA SINCRONIZAR FNET-AUTOMATION
# Ejecutar en PowerShell
# ===============================================================

# 1. Ir a la carpeta de fnet-automation (AJUSTAR RUTA SI ES DIFERENTE)
cd "E:\ALBERTH ALM\DOCUMENTOS\NetBeansProjects\fnet-automation"

# 2. Verificar estado de git
git status

# 3. Hacer pull de los últimos cambios
git pull origin main

# 4. Copiar los archivos actualizados desde fnet
# YapePagoProcessor.java (versión mejorada con distribución de meses)
Copy-Item "E:\ALBERTH ALM\DOCUMENTOS\NetBeansProjects\fnet\src\servicio\YapePagoProcessor.java" -Destination ".\src\main\java\servicio\YapePagoProcessor.java" -Force

# PagoDAO.java (con nuevas mejoras)
Copy-Item "E:\ALBERTH ALM\DOCUMENTOS\NetBeansProjects\fnet\src\DAO\PagoDAO.java" -Destination ".\src\main\java\DAO\PagoDAO.java" -Force

# 5. Ver los cambios
git diff

# 6. Agregar los archivos modificados
git add .

# 7. Hacer commit
git commit -m "feat: Sincronizar YapePagoProcessor y PagoDAO con fnet local

- YapePagoProcessor: Distribucion de pagos por meses
- PagoDAO: Codigo factura secuencial 0001, 0002...
- Mejoras en obtenerMovimientosDelDia"

# 8. Push a GitHub
git push origin main

# ===============================================================
# NOTA: Si hay conflictos, revisa los archivos manualmente antes de hacer push
# ===============================================================
