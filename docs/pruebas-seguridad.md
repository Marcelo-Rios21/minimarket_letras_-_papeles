# Pruebas de seguridad - MiniMarket Plus
## 1. Objetivo de las pruebas

El objetivo de estas pruebas es validar que la configuración de Spring Security implementada en el backend de "MiniMarket Plus" cumple con los requerimientos de autenticación mediante JWT, autorización por roles y protección de endpoints sensibles.

Las pruebas fueron realizadas utilizando autenticación mediante JWT. Para cada usuario, primero se ejecuta el endpoint de login y luego se utiliza el token recibido en el encabezado `Authorization` con el formato `Bearer`.

Usuarios iniciales utilizados:

| Usuario | Contraseña | Rol |
|---|---|---|
| cliente | cliente123 | ROLE_CLIENTE |
| empleado | empleado123 | ROLE_EMPLEADO |
| gerente | gerente123 | ROLE_GERENTE |

Ejemplo de autenticación utilizado en PowerShell:

```powershell
$login = @{
  username = "cliente"
  password = "cliente123"
} | ConvertTo-Json -Compress

$auth = Invoke-RestMethod `
  -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $login

$token = $auth.token
```

El token obtenido se utiliza posteriormente en las solicitudes protegidas:

```powershell
-H "Authorization: Bearer $token"
```

---

## 2. Pruebas de autenticación

### 2.1 Acceso a endpoint público sin autenticación

Endpoint probado:

    ```text
    GET /public/hola
    ```

Comando utilizado:

    ```powershell
    curl.exe -i http://localhost:8080/public/hola
    ```

Resultado esperado:

    ```text
    HTTP/1.1 200
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 200
    ¡Hola Mundo!
    ```

Conclusión:

El endpoint público funciona correctamente sin requerir autenticación. Esto confirma que las rutas públicas definidas en la configuración de seguridad son accesibles sin token JWT.

---

### 2.2 Acceso a endpoint privado sin token JWT

Endpoint probado:

    ```text
    GET /api/productos
    ```

Comando utilizado:

    ```powershell
    curl.exe -i http://localhost:8080/api/productos
    ```

Resultado esperado:

    ```text
    HTTP/1.1 401
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 401
    {"status":401,"error":"Unauthorized","message":"Autenticacion requerida","path":"/api/productos"}
    ```

Conclusión:

El backend bloquea correctamente el acceso a recursos privados cuando no se envía un token JWT válido. Esto confirma que los endpoints protegidos requieren autenticación.

### 2.3 Inicio de sesión y generación de token JWT

Endpoint probado:

    ```text
    POST /api/auth/login
    ```

Comando utilizado en PowerShell:

    ```powershell
    $body = @{
    username = "cliente"
    password = "cliente123"
    } | ConvertTo-Json -Compress

    Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
    ```

Resultado esperado:

    ```text
    Respuesta con token JWT, tipo de token, usuario y roles.
    ```

Resultado obtenido:

    ```text
    Se obtiene un token JWT para el usuario cliente con rol ROLE_CLIENTE.
    ```

Conclusión:

El endpoint de login autentica correctamente las credenciales válidas y genera un token JWT que puede ser utilizado en solicitudes posteriores.

### 2.4 Acceso a endpoint privado con token JWT válido

Endpoint probado:

    ```text
    GET /api/productos
    ```

Comando utilizado en PowerShell:

    ```powershell
    $login = @{
    username = "cliente"
    password = "cliente123"
    } | ConvertTo-Json -Compress

    $auth = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $login

    $token = $auth.token

    Invoke-WebRequest `
    -Uri "http://localhost:8080/api/productos" `
    -Method GET `
    -Headers @{ Authorization = "Bearer $token" } `
    -UseBasicParsing
    ```

Resultado esperado:

    ```text
    HTTP 200
    ```

Resultado obtenido:

    ```text
    StatusCode: 200
    Content: []
    ```

Conclusión:

El backend permite acceder correctamente a un endpoint protegido cuando la solicitud incluye un token JWT válido en el encabezado `Authorization`.

---

## 3. Pruebas de autorización por roles

El objetivo de estas pruebas es verificar que los permisos configurados en `SecurityConfig` se apliquen correctamente según el rol del usuario autenticado.

Para cada prueba se realizó primero un inicio de sesión mediante `/api/auth/login`, se obtuvo el token JWT correspondiente y luego se envió dicho token en el encabezado `Authorization`.

---

### 3.1 Cliente accede a productos

Usuario utilizado:

    ```text
    cliente / ROLE_CLIENTE
    ```

Endpoint probado:

    ```text
    GET /api/productos
    ```

Resultado esperado:

    ```text
    HTTP 200
    ```

Resultado obtenido:

    ```text
    StatusCode: 200
    Content: []
    ```

Conclusión:

El cliente puede consultar productos correctamente. Esto es coherente con la matriz de permisos, ya que la consulta de productos está permitida para clientes, empleados y gerentes.

---

### 3.2 Cliente intenta acceder a administración de usuarios

Usuario utilizado:

    ```text
    cliente / ROLE_CLIENTE
    ```

Endpoint probado:

    ```text
    GET /api/usuarios
    ```

Resultado esperado:

    ```text
    HTTP/1.1 403
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 403
    ```

Conclusión:

El backend bloquea correctamente el acceso del cliente al endpoint de administración de usuarios. Esto confirma que un usuario autenticado no obtiene acceso automático a todos los recursos, sino que sus permisos dependen del rol asignado.

---

### 3.3 Empleado accede a inventario

Usuario utilizado:

    ```text
    empleado / ROLE_EMPLEADO
    ```

Endpoint probado:

    ```text
    GET /api/inventario
    ```

Comando utilizado:

    ```powershell
    $login = @{
    username = "empleado"
    password = "empleado123"
    } | ConvertTo-Json -Compress

    $auth = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $login

    $token = $auth.token

    curl.exe -i http://localhost:8080/api/inventario `
    -H "Authorization: Bearer $token"
    ```

Resultado esperado:

    ```text
    HTTP/1.1 200
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 200
    []
    ```

Conclusión:

El empleado puede acceder correctamente al inventario. Esto confirma que el rol `ROLE_EMPLEADO` tiene permisos operativos sobre recursos internos del minimarket.

---

### 3.4 Cliente intenta acceder a inventario

Usuario utilizado:

    ```text
    cliente / ROLE_CLIENTE
    ```

Endpoint probado:

    ```text
    GET /api/inventario
    ```

Comando utilizado:

    ```powershell
    $login = @{
    username = "cliente"
    password = "cliente123"
    } | ConvertTo-Json -Compress

    $auth = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $login

    $token = $auth.token

    curl.exe -i http://localhost:8080/api/inventario `
    -H "Authorization: Bearer $token"
    ```

Resultado esperado:

    ```text
    HTTP/1.1 403
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 403
    {"status":403,"error":"Forbidden","message":"Acceso denegado","path":"/api/inventario"}
    ```

Conclusión:

El backend bloquea correctamente el acceso del cliente al inventario. Esto confirma que las operaciones internas quedan restringidas a roles operativos o administrativos.

---

### 3.5 Gerente accede a administración de usuarios

Usuario utilizado:

    ```text
    gerente / ROLE_GERENTE
    ```

Endpoint probado:

    ```text
    GET /api/usuarios
    ```

Comando utilizado:

    ```powershell
    $login = @{
    username = "gerente"
    password = "gerente123"
    } | ConvertTo-Json -Compress

    $auth = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $login

    $token = $auth.token

    curl.exe -i http://localhost:8080/api/usuarios `
    -H "Authorization: Bearer $token"
    ```

Resultado esperado:

    ```text
    HTTP/1.1 200
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 200
    ```

Respuesta obtenida:

    ```json
    [
    {
        "id": 1,
        "username": "cliente",
        "roles": [
        {
            "id": 1,
            "nombre": "ROLE_CLIENTE"
        }
        ]
    },
    {
        "id": 2,
        "username": "empleado",
        "roles": [
        {
            "id": 2,
            "nombre": "ROLE_EMPLEADO"
        }
        ]
    },
    {
        "id": 3,
        "username": "gerente",
        "roles": [
        {
            "id": 3,
            "nombre": "ROLE_GERENTE"
        }
        ]
    }
    ]
    ```

Conclusión:

El gerente puede acceder correctamente al endpoint de administración de usuarios. Además, la respuesta no expone el campo `password`, lo que confirma que las credenciales no se devuelven en la respuesta JSON.

---

### 3.6 Resultado general de autorización

Las pruebas realizadas confirman que la autorización basada en roles funciona correctamente:

| Prueba | Usuario | Rol | Endpoint | Resultado |
|---|---|---|---|---|
| Consulta de productos | cliente | ROLE_CLIENTE | `GET /api/productos` | 200 |
| Administración de usuarios | cliente | ROLE_CLIENTE | `GET /api/usuarios` | 403 |
| Consulta de inventario | empleado | ROLE_EMPLEADO | `GET /api/inventario` | 200 |
| Consulta de inventario | cliente | ROLE_CLIENTE | `GET /api/inventario` | 403 |
| Administración de usuarios | gerente | ROLE_GERENTE | `GET /api/usuarios` | 200 |

Conclusión general:

La configuración de Spring Security aplica correctamente las restricciones por rol. Los clientes pueden acceder a recursos permitidos, los empleados pueden operar recursos internos como inventario y los gerentes tienen acceso administrativo.

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
