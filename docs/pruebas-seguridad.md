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
    La respuesta JSON debe mostrar usuarios y roles, pero no debe mostrar el campo password.
    ```

Resultado obtenido:

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

La respuesta contiene `id`, `username` y `roles`, pero no expone el campo `password`. Esto confirma que las credenciales no se devuelven en las respuestas JSON del endpoint de usuarios.

---

## 5. Pruebas de manejo básico de errores de seguridad

### 5.1 Acceso sin autenticación

Endpoint probado:

    ```text
    GET /api/productos
    ```

Usuario utilizado:

    ```text
    Sin token JWT
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

El backend responde correctamente con `401 Unauthorized` cuando se intenta acceder a un recurso protegido sin enviar token JWT. Esto permite diferenciar un problema de autenticación, es decir, ausencia o invalidez de credenciales.

---

### 5.2 Acceso denegado por permisos insuficientes

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

El backend responde correctamente con `403 Forbidden` cuando un usuario autenticado intenta acceder a un recurso para el cual no tiene permisos suficientes. Esto permite diferenciar un problema de autorización.

---

## 6. Pruebas contra amenazas comunes

### 6.1 Prueba contra SQL Injection en login

Amenaza evaluada:

    ```text
    SQL Injection
    ```

Endpoint probado:

    ```text
    POST /api/auth/login
    ```

Payload utilizado:

    ```json
    {
    "username": "' OR '1'='1",
    "password": "cualquier"
    }
    ```

Comando utilizado:

    ```powershell
    @'
    {
    "username": "' OR '1'='1",
    "password": "cualquier"
    }
    '@ | Set-Content -Encoding UTF8 .\login_sqli.json

    curl.exe -i -X POST http://localhost:8080/api/auth/login `
    -H "Content-Type: application/json" `
    --data-binary "@login_sqli.json"
    ```

Resultado esperado:

    ```text
    HTTP/1.1 401
    ```

Resultado obtenido:

    ```text
    HTTP/1.1 401
    {"status":401,"error":"Unauthorized","message":"Autenticacion requerida","path":"/api/auth/login"}
    ```

Conclusión:

El payload de SQL Injection no logró autenticarse ni saltarse el mecanismo de login. Esto confirma que la autenticación no acepta entradas maliciosas como credenciales válidas. Además, el uso de Spring Data JPA y repositorios reduce el riesgo de inyección SQL al evitar consultas construidas mediante concatenación manual de parámetros.

---

### 6.2 Prueba con token JWT inválido

Amenaza evaluada:

    ```text
    Uso de token inválido o manipulado
    ```

Endpoint probado:

    ```text
    GET /api/productos
    ```

Comando utilizado:

    ```powershell
    curl.exe -i http://localhost:8080/api/productos -H "Authorization: Bearer token_invalido"
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

El backend rechaza correctamente solicitudes con un token inválido. Esto confirma que no basta con enviar cualquier valor en el encabezado `Authorization`; el token debe ser válido, estar firmado correctamente y superar la validación del filtro JWT.

---

### 6.3 Prueba CSRF en API stateless

Amenaza evaluada:

    ```text
    CSRF
    ```

Endpoint probado:

    ```text
    POST /api/productos
    ```

Comando utilizado:

    ```powershell
    curl.exe -i -X POST http://localhost:8080/api/productos `
    -H "Content-Type: application/json" `
    --data-binary "{}"
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

El backend bloquea correctamente una solicitud de modificación enviada sin token JWT. Esto respalda la configuración stateless del sistema: las operaciones protegidas no dependen de sesiones tradicionales del navegador, sino de un token enviado explícitamente en el encabezado `Authorization`.

---

### 6.4 Prueba XSS en creación de categoría

Amenaza evaluada:

    ```text
    XSS
    ```

Endpoint probado:

    ```text
    POST /api/categorias
    ```

Payload utilizado:

    ```json
    {
    "nombre": "<script>alert('xss')</script>"
    }
    ```

Resultado inicial observado:

    ```text
    HTTP/1.1 200
    {"id":1,"nombre":"<script>alert('xss')</script>","productos":null}
    ```

Este resultado evidenció que el backend aceptaba contenido potencialmente peligroso en campos de texto. Para corregirlo, se incorporó una validación básica mediante la clase `InputValidator`, aplicada sobre el campo `nombre` en los controladores de categorías y productos.

Archivos modificados:

    ```text
    src/main/java/com/minimarket/security/util/InputValidator.java
    src/main/java/com/minimarket/controller/CategoriaController.java
    src/main/java/com/minimarket/controller/ProductoController.java
    ```

Comando utilizado después de la corrección:

    ```powershell
    @'
    {
    "nombre": "<script>alert('xss')</script>"
    }
    '@ | Set-Content -Encoding UTF8 .\categoria_xss.json

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

    curl.exe -i -X POST http://localhost:8080/api/categorias `
    -H "Content-Type: application/json" `
    -H "Authorization: Bearer $token" `
    --data-binary "@categoria_xss.json"
    ```

Resultado esperado después de la corrección:

    ```text
    HTTP/1.1 400
    ```

Resultado obtenido después de la corrección:

    ```text
    HTTP/1.1 400
    {"status":400,"error":"Bad Request","message":"El campo nombre contiene caracteres no permitidos","path":"/api/categorias"}
    ```

Conclusión:

El backend ahora rechaza entradas con contenido potencialmente peligroso, como etiquetas `<script>`. Esta validación reduce el riesgo de almacenar datos que posteriormente puedan generar XSS si son consumidos por un frontend.

---

## 7. Resumen general de resultados

| Prueba | Usuario | Endpoint | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---:|---:|---|
| Endpoint público | Sin usuario | `GET /public/hola` | 200 | 200 | Correcto |
| Endpoint privado sin token | Sin usuario | `GET /api/productos` | 401 | 401 | Correcto |
| Login JWT | cliente | `POST /api/auth/login` | Token JWT | Token JWT generado | Correcto |
| Endpoint privado con token | cliente | `GET /api/productos` | 200 | 200 | Correcto |
| Cliente accede a usuarios | cliente | `GET /api/usuarios` | 403 | 403 | Correcto |
| Empleado accede a inventario | empleado | `GET /api/inventario` | 200 | 200 | Correcto |
| Cliente accede a inventario | cliente | `GET /api/inventario` | 403 | 403 | Correcto |
| Gerente accede a usuarios | gerente | `GET /api/usuarios` | 200 | 200 | Correcto |
| Contraseña oculta en JSON | gerente | `GET /api/usuarios` | Sin password visible | Sin password visible | Correcto |
| Acceso sin autenticación | Sin usuario | `GET /api/productos` | 401 | 401 | Correcto |
| Acceso con rol insuficiente | cliente | `GET /api/inventario` | 403 | 403 | Correcto |
| SQL Injection en login | Sin usuario | `POST /api/auth/login` | 401 | 401 | Correcto |
| Token inválido | Token inválido | `GET /api/productos` | 401 | 401 | Correcto |
| CSRF / POST sin token | Sin usuario | `POST /api/productos` | 401 | 401 | Correcto |
| XSS en categoría | empleado | `POST /api/categorias` | 400 | 400 | Correcto |

---

## 8. Conclusión de pruebas

Las pruebas realizadas confirman que la configuración de seguridad implementada cumple con los requerimientos principales de autenticación y autorización del backend.

El sistema permite acceder a rutas públicas sin autenticación, bloquea correctamente los recursos privados cuando no se envía token JWT, genera tokens mediante el endpoint de login y permite acceder a endpoints protegidos cuando se envía un token válido en el encabezado `Authorization`.

Además, la autorización basada en roles funciona correctamente. El cliente puede consultar productos, pero no puede acceder a la administración de usuarios ni al inventario. El empleado puede acceder a recursos operativos como inventario. El gerente, que representa el perfil administrador, puede acceder a la administración de usuarios.

También se verificó que el campo `password` no se expone en las respuestas JSON, lo que contribuye a proteger información sensible. Finalmente, las respuestas `401 Unauthorized` y `403 Forbidden` permiten diferenciar entre falta de autenticación y falta de permisos.

Finalmente se realizaron pruebas específicas contra amenazas comunes. El backend rechazó intentos de SQL Injection en el login, tokens JWT inválidos, solicitudes de modificación sin token y entradas con contenido potencialmente peligroso para XSS. En el caso de XSS, la prueba inicial permitió detectar que el backend aceptaba etiquetas `<script>`, por lo que se agregó una validación básica de entrada en categorías y productos. Luego de la corrección, el backend respondió correctamente con `400 Bad Request`.
