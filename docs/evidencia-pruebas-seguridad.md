# Evidencia de pruebas de seguridad y cobertura

## 1. Objetivo

Este documento resume la evidencia técnica obtenida durante la ejecución de pruebas unitarias y pruebas de autorización del backend MiniMarket Plus.

El foco principal fue validar que las operaciones críticas del sistema respeten las reglas de autenticación y autorización por rol.

## 2. Roles evaluados

| Perfil funcional | Rol técnico |
|---|---|
| Cliente | ROLE_CLIENTE |
| Cajero | ROLE_EMPLEADO |
| Administrador | ROLE_GERENTE |

## 3. Pruebas de autorización agregadas

Se agregaron pruebas automáticas con MockMvc para validar el comportamiento de endpoints protegidos.

Clases agregadas:

- AuthProductoAuthorizationTest
- InventarioAuthorizationTest
- VentaAuthorizationTest

## 4. Casos validados

### Autenticación

| Caso | Resultado esperado |
|---|---|
| Login válido | 200 OK |
| Login inválido | 401 Unauthorized |

### Producto

| Caso | Resultado esperado |
|---|---|
| Modificar producto sin token | 401 Unauthorized |
| Cliente modifica producto | 403 Forbidden |
| Cajero modifica producto | 403 Forbidden |
| Administrador modifica producto | 200 OK |

### Inventario

| Caso | Resultado esperado |
|---|---|
| Registrar inventario sin token | 401 Unauthorized |
| Cliente registra inventario | 403 Forbidden |
| Cajero registra inventario | 200 OK |
| Administrador registra inventario | 200 OK |

### Venta

| Caso | Resultado esperado |
|---|---|
| Generar venta sin token | 401 Unauthorized |
| Cliente genera venta | 403 Forbidden |
| Administrador genera venta | 403 Forbidden |
| Cajero genera venta | 201 Created |

## 5. Resultado de ejecución

Comando utilizado:

mvn clean test

Resultado obtenido:

- 73 pruebas ejecutadas correctamente.
- Build finalizado correctamente.
- No se registraron errores ni fallos en la suite completa.

## 6. Reportes generados

Los reportes XML/TXT de pruebas se generan en:

target/surefire-reports

El reporte HTML de cobertura se genera en:

target/site/jacoco/index.html

## 7. Resultado JaCoCo observado

| Métrica | Resultado |
|---|---:|
| Cobertura de instrucciones | 69% |
| Cobertura de ramas | 52% |
| security.config | 100% |
| security.handler | 100% |
| security.jwt | 93% |
| entity | 91% |

## 8. Análisis breve

Los resultados permiten confirmar que las reglas de acceso se aplican correctamente sobre operaciones críticas del backend.

Se validó que los endpoints protegidos no permiten acceso sin token JWT, respondiendo 401 Unauthorized. También se comprobó que un usuario autenticado con rol insuficiente recibe 403 Forbidden. Finalmente, se verificó que los roles autorizados sí pueden ejecutar las operaciones permitidas.

Esto demuestra que el backend diferencia correctamente entre cliente, cajero y administrador, cumpliendo el control de acceso solicitado para productos, inventario y ventas.

## 9. Mejoras identificadas

A partir de las pruebas y del análisis de cobertura se identifican las siguientes mejoras futuras:

- Aumentar cobertura de controladores, ya que el paquete controller presenta menor cobertura relativa.
- Agregar pruebas MockMvc para categorías, carrito, detalle de ventas y usuarios.
- Incorporar DTOs para reducir exposición directa de entidades.
- Agregar control de propiedad de recursos para carritos y ventas.
- Centralizar el manejo de errores mediante un RestControllerAdvice.
- Incorporar alertas de stock mínimo en inventario.