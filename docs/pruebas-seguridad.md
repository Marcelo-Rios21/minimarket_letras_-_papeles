# Pruebas de seguridad - LETRAS & PAPELES

## 1. Objetivo de las pruebas

El objetivo de estas pruebas es validar que la configuración de Spring Security implementada en el backend de "LETRAS & PAPELES" cumple con los requerimientos de autenticación, autorización por roles y protección de endpoints sensibles.

Las pruebas fueron realizadas utilizando autenticación HTTP Basic con tres usuarios iniciales:

| Usuario | Contraseña | Rol |
|---|---|---|
| cliente | cliente123 | ROLE_CLIENTE |
| empleado | empleado123 | ROLE_EMPLEADO |
| gerente | gerente123 | ROLE_GERENTE |

---

## 2. Pruebas de autenticación

### 2.1 Acceso a endpoint público sin autenticación

Endpoint probado:

    GET /public/hola

Resultado esperado:

    200 OK

Resultado obtenido:

    200 OK

Conclusión:

El endpoint público funciona correctamente sin requerir autenticación.

---

### 2.2 Acceso a endpoint privado sin autenticación

Endpoint probado:

    GET /api/usuarios

Resultado esperado:

    401 Unauthorized

Resultado obtenido:

    401 Unauthorized

Conclusión:

El backend bloquea correctamente el acceso a recursos privados cuando no se envían credenciales.

---

## 3. Pruebas de autorización por roles

### 3.1 Cliente consultando productos

Usuario utilizado:

    cliente / cliente123

Endpoint probado:

    GET /api/productos

Resultado esperado:

    200 OK

Resultado obtenido:

    200 OK

Conclusión:

El cliente puede acceder correctamente a la consulta de productos, ya que la lectura de productos está permitida para usuarios autenticados.

---

### 3.2 Cliente intentando acceder a usuarios

Usuario utilizado:

    cliente / cliente123

Endpoint probado:

    GET /api/usuarios

Resultado esperado:

    403 Forbidden

Resultado obtenido:

    403 Forbidden

Conclusión:

El cliente se autentica correctamente, pero no tiene permisos para acceder a la administración de usuarios. Esto confirma que la autorización por roles está funcionando.

---

### 3.3 Empleado intentando acceder a usuarios

Usuario utilizado:

    empleado / empleado123

Endpoint probado:

    GET /api/usuarios

Resultado esperado:

    403 Forbidden

Resultado obtenido:

    403 Forbidden

Conclusión:

El empleado no puede acceder a la administración de usuarios, ya que esta operación está reservada para el gerente.

---

### 3.4 Gerente accediendo a usuarios

Usuario utilizado:

    gerente / gerente123

Endpoint probado:

    GET /api/usuarios

Resultado esperado:

    200 OK

Resultado obtenido:

    200 OK

Conclusión:

El gerente puede acceder correctamente a la administración de usuarios, ya que posee el rol con mayor nivel de permisos.

---

## 4. Pruebas de protección de datos sensibles

### 4.1 Verificación de ocultamiento de contraseñas

Usuario utilizado:

    gerente / gerente123

Endpoint probado:

    GET /api/usuarios

Resultado esperado:

La respuesta JSON debe mostrar usuarios y roles, pero no debe mostrar el campo `password`.

Resultado obtenido:

La respuesta contiene `id`, `username` y `roles`, pero no expone el campo `password`.

Conclusión:

La contraseña se oculta correctamente en las respuestas JSON mediante la configuración aplicada en la entidad `Usuario`.

---

## 5. Pruebas de monitoreo básico

### 5.1 Registro de acceso sin autenticación

Endpoint probado:

    GET /api/usuarios

Usuario utilizado:

    Sin credenciales

Resultado esperado:

    401 Unauthorized

Resultado obtenido:

    401 Unauthorized

Además, en consola se registra un evento indicando intento de acceso no autenticado o credenciales inválidas.

Conclusión:

El sistema registra eventos de autenticación fallida mediante `CustomAuthenticationEntryPoint`.

---

### 5.2 Registro de acceso denegado por permisos insuficientes

Usuario utilizado:

    cliente / cliente123

Endpoint probado:

    GET /api/usuarios

Resultado esperado:

    403 Forbidden

Resultado obtenido:

    403 Forbidden

Además, en consola se registra un evento indicando acceso denegado por permisos insuficientes.

Conclusión:

El sistema registra eventos de autorización fallida mediante `CustomAccessDeniedHandler`.

---

## 6. Resumen general de resultados

| Prueba | Usuario | Resultado esperado | Resultado obtenido | Estado |
|---|---|---:|---:|---:|
| Endpoint público `/public/hola` | Sin usuario | 200 | 200 | Correcto |
| Endpoint privado `/api/usuarios` sin login | Sin usuario | 401 | 401 | Correcto |
| Cliente consulta productos | cliente | 200 | 200 | Correcto |
| Cliente accede a usuarios | cliente | 403 | 403 | Correcto |
| Empleado accede a usuarios | empleado | 403 | 403 | Correcto |
| Gerente accede a usuarios | gerente | 200 | 200 | Correcto |
| Contraseña oculta en JSON | gerente | Sin password visible | Sin password visible | Correcto |
| Log de acceso no autenticado | Sin usuario | Log generado | Log generado | Correcto |
| Log de acceso denegado | cliente | Log generado | Log generado | Correcto |

---

## 7. Conclusión de pruebas

Las pruebas realizadas confirman que la configuración de seguridad implementada cumple con los requerimientos principales de la actividad. El backend exige autenticación para recursos privados, diferencia permisos según roles, protege información sensible y registra eventos relevantes de seguridad.

La autenticación con nombre de usuario y contraseña funciona correctamente mediante HTTP Basic. Además, la autorización por roles impide que usuarios con permisos limitados accedan a funciones administrativas, como la gestión de usuarios.

También se confirmó que el campo `password` no se expone en las respuestas JSON, lo que contribuye a proteger datos sensibles del sistema.
