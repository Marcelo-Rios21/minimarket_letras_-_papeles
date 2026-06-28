# Análisis de seguridad del backend - MiniMarket Plus

## 1. Contexto del backend analizado

El backend corresponde a una aplicacion Spring Boot para el sistema "MiniMarket Plus". La aplicacion mantiene una arquitectura por capas, separando controladores, servicios, repositorios, entidades y configuracion de seguridad.

El sistema incorpora autenticacion mediante JWT y autorizacion por roles con Spring Security. Para efectos funcionales, se consideran tres perfiles principales: cliente, cajero y administrador. En el codigo estos perfiles se representan mediante los roles `ROLE_CLIENTE`, `ROLE_EMPLEADO` y `ROLE_GERENTE`.

La seguridad del backend se orienta a proteger operaciones criticas asociadas a productos, inventario, ventas, carrito y usuarios. Las reglas de acceso permiten diferenciar que acciones puede realizar cada perfil, evitando que usuarios sin permisos ejecuten funciones administrativas u operativas sensibles.

## 2. Componentes relevantes de seguridad existentes

En el backend se identifican los siguientes componentes relacionados con seguridad:

- Entidad `Usuario`, utilizada para representar las cuentas del sistema.
- Entidad `Rol`, utilizada para representar los niveles de acceso.
- Repositorio `UsuarioRepository`, utilizado para buscar usuarios registrados.
- Repositorio `RolRepository`, utilizado para gestionar roles.
- Clase `CustomUserDetails`, encargada de adaptar la entidad `Usuario` al modelo de autenticacion de Spring Security.
- Clase `CustomUserDetailsService`, encargada de cargar usuarios desde la base de datos.
- Clase `SecurityConfig`, donde se define la configuracion principal de Spring Security y la matriz de permisos por endpoint.
- Clase `JwtUtil`, encargada de generar, firmar, validar y leer tokens JWT.
- Clase `JwtAuthenticationFilter`, encargada de interceptar solicitudes protegidas y validar el token recibido en el encabezado `Authorization`.
- DTOs `LoginRequest`, `RegisterRequest` y `AuthResponse`, utilizados para el flujo de autenticacion y registro.
- Controlador `AuthController`, encargado de exponer los endpoints de login y registro.
- Bean `BCryptPasswordEncoder`, utilizado para proteger contrasenas mediante cifrado unidireccional.

Estos componentes permiten implementar un esquema de seguridad stateless, donde cada solicitud protegida debe incluir un token JWT valido y donde el acceso final depende del rol asociado al usuario autenticado.

## 3. Puntos criticos identificados

A partir de la revision del backend se identifican los siguientes puntos criticos:

1. La aplicacion utiliza Spring Security con configuracion stateless y autenticacion mediante JWT.
2. Existen entidades `Usuario` y `Rol`, junto con reglas de autorizacion por tipo de usuario.
3. El sistema requiere proteger operaciones sensibles como gestion de productos, ventas, inventario, carrito y usuarios.
4. La modificacion de productos debe quedar restringida al administrador.
5. La generacion de ventas debe quedar restringida al cajero.
6. Los movimientos de inventario deben quedar restringidos a cajero y administrador.
7. Los endpoints protegidos deben responder `401 Unauthorized` cuando no se envia token JWT.
8. Los endpoints protegidos deben responder `403 Forbidden` cuando el usuario autenticado no posee el rol requerido.
9. El backend puede exponer informacion sensible si devuelve entidades completas sin control, especialmente en el caso de usuarios.
10. Es necesario mantener las contrasenas protegidas mediante `BCryptPasswordEncoder`.
11. JWT se encuentra implementado mediante `JwtUtil` y `JwtAuthenticationFilter`, por lo que corresponde mantener esta estrategia y validarla mediante pruebas.
12. Se recomienda fortalecer progresivamente la trazabilidad de eventos sensibles, como intentos fallidos de autenticacion, accesos denegados y operaciones criticas.

## 4. Necesidades de seguridad del cliente

Para el sistema "MiniMarket Plus", el backend debe proteger los recursos asociados a usuarios, roles, productos, categorias, carrito, ventas, detalle de ventas e inventario. La aplicacion administra informacion operativa y datos de usuarios, por lo que requiere controles claros de autenticacion, autorizacion y proteccion de datos.

Las principales necesidades de seguridad son:

- Permitir que solo usuarios autenticados accedan a recursos privados.
- Diferenciar permisos entre cliente, cajero y administrador.
- Proteger credenciales mediante almacenamiento seguro de contrasenas.
- Evitar exposicion de datos sensibles en respuestas de la API.
- Restringir la modificacion de productos al administrador.
- Restringir la generacion de ventas al cajero.
- Restringir los movimientos de inventario a cajero y administrador.
- Mantener una configuracion stateless mediante JWT.
- Registrar eventos relevantes de seguridad, como accesos denegados e intentos fallidos.

## 5. Amenazas potenciales identificadas

A partir del analisis del backend y de los requerimientos de la actividad, se identifican las siguientes amenazas potenciales para el sistema "MiniMarket Plus":

### 5.1 Accesos no autorizados

El backend expone endpoints asociados a productos, categorias, carrito, ventas, detalle de ventas, inventario y usuarios. Si estos recursos no se protegen correctamente, un usuario podria consultar informacion o ejecutar operaciones que no le corresponden.

Esta amenaza se mitiga mediante Spring Security, autenticacion JWT y reglas de autorizacion por rol. Los endpoints protegidos deben responder `401 Unauthorized` cuando no existe token y `403 Forbidden` cuando el rol autenticado no posee permisos suficientes.

### 5.2 Escalamiento de privilegios

Un usuario autenticado podria intentar ejecutar operaciones superiores a su perfil. Por ejemplo, un cliente podria intentar modificar productos o registrar inventario, o un administrador podria intentar ejecutar una operacion reservada al flujo operativo del cajero.

Esta amenaza se mitiga separando responsabilidades: el cliente queda limitado al flujo de consulta y carrito, el cajero registra ventas y movimientos de inventario, y el administrador gestiona productos y usuarios.

### 5.3 Manipulacion o uso indebido de tokens JWT

Dado que el backend utiliza JWT, existe el riesgo de recibir tokens invalidos, expirados, mal formados o manipulados. Si el token no se valida correctamente, un usuario podria intentar acceder a recursos protegidos sin autenticacion legitima.

Esta amenaza se mitiga mediante firma del token, validacion de expiracion, extraccion segura del usuario autenticado y verificacion del token en cada solicitud protegida mediante `JwtAuthenticationFilter`.

### 5.4 Exposicion de datos sensibles

La entidad `Usuario` contiene informacion sensible, especialmente el campo `password`. Si el backend devuelve entidades completas sin control, existe riesgo de exponer datos internos o credenciales.

Esta amenaza se reduce evitando la serializacion del campo de contrasena y manteniendo la recomendacion de utilizar DTOs para respuestas de API.

### 5.5 Almacenamiento inseguro de contrasenas

Si las contrasenas se almacenaran en texto plano, un acceso indebido a la base de datos podria comprometer directamente las cuentas de los usuarios.

Esta amenaza se mitiga mediante `BCryptPasswordEncoder`, que permite almacenar contrasenas de forma no reversible.

### 5.6 Acceso indebido a recursos ajenos

Ademas del control por roles, el sistema debe considerar que un usuario podria intentar modificar identificadores de recursos, como carrito, venta o usuario, para acceder a informacion que no le corresponde.

Esta amenaza se reconoce como una mejora futura, recomendandose incorporar validaciones de propiedad del recurso en servicios o controladores.

## 6. Roles y responsabilidades

Para implementar una estrategia de autorizacion coherente, el backend define tres perfiles funcionales principales: cliente, cajero y administrador. En el codigo estos perfiles se representan mediante los roles `ROLE_CLIENTE`, `ROLE_EMPLEADO` y `ROLE_GERENTE`.

### 6.1 Cliente

El cliente representa al usuario final del sistema. Su acceso se orienta al flujo de compra y consulta.

Responsabilidades permitidas:

- Consultar productos y categorias disponibles.
- Gestionar su propio carrito.
- Acceder solo a operaciones asociadas a su perfil.

Restricciones principales:

- No puede modificar productos.
- No puede registrar movimientos de inventario.
- No puede generar ventas desde el endpoint protegido de ventas.
- No puede administrar usuarios ni roles.

### 6.2 Cajero

El cajero corresponde al perfil operativo del sistema. En el codigo se representa mediante `ROLE_EMPLEADO`.

Responsabilidades permitidas:

- Consultar productos y categorias.
- Registrar ventas.
- Consultar ventas cuando corresponda a la operacion.
- Registrar movimientos de inventario.
- Registrar o actualizar categorias segun las reglas operativas del backend.
- Revisar informacion operativa necesaria para la atencion.

Restricciones principales:

- No puede modificar productos.
- No puede eliminar productos.
- No puede administrar usuarios ni roles.
- No puede ejecutar funciones administrativas reservadas al gerente.

### 6.3 Administrador

El administrador corresponde al perfil con mayor nivel de control dentro del backend. En el codigo se representa mediante `ROLE_GERENTE`.

Responsabilidades permitidas:

- Administrar usuarios.
- Gestionar roles.
- Crear, editar y eliminar productos.
- Registrar y consultar movimientos de inventario.
- Consultar informacion operativa del sistema.
- Eliminar recursos permitidos segun la matriz de seguridad.

Restricciones principales:

- No genera ventas en la pauta estricta, ya que la creacion de ventas queda reservada al cajero.
- Debe operar bajo autenticacion JWT y autorizacion por rol.

La separacion de responsabilidades permite reducir riesgos de acceso indebido y evita que un unico perfil concentre todas las operaciones criticas del sistema.

## 7. Comparacion de estrategias de autenticacion

Para definir la estrategia de autenticacion del backend se consideran distintas alternativas aplicables a un sistema REST como "MiniMarket Plus".

| Estrategia | Descripcion | Ventajas | Desventajas | Aplicabilidad al proyecto |
|---|---|---|---|---|
| Autenticacion en memoria | Los usuarios se definen directamente en la configuracion de seguridad. | Facil de implementar y util para pruebas rapidas. | No es adecuada para produccion ni para administrar usuarios reales. | Baja. Puede servir para pruebas iniciales, pero no responde al alcance del sistema. |
| Autenticacion con base de datos usando JPA | Los usuarios y roles se almacenan en la base de datos y se cargan mediante `UserDetailsService`. | Se integra con la arquitectura actual y permite administrar usuarios reales. | Requiere configurar correctamente cifrado de contrasenas y reglas de autorizacion. | Alta. Es coherente con las entidades `Usuario` y `Rol` del proyecto. |
| Autenticacion JDBC | Spring Security consulta usuarios y roles directamente desde tablas mediante JDBC. | Es una estrategia valida y soportada por Spring Security. | El proyecto ya utiliza Spring Data JPA, por lo que seria menos coherente. | Media. Es posible, pero no es la opcion mas alineada con el backend. |
| LDAP / LDAPS | La autenticacion se realiza contra un directorio corporativo externo. | Centraliza usuarios corporativos y LDAPS protege la comunicacion. | Requiere infraestructura externa, certificados y administracion adicional. | Media. Es relevante para una evolucion empresarial, pero no se implementa en esta entrega. |
| JWT | La autenticacion se realiza mediante tokens enviados por el cliente en cada solicitud protegida. | Es adecuada para APIs REST stateless y permite separar frontend y backend. | Requiere proteger la clave secreta y validar correctamente firma y expiracion. | Alta. Es la estrategia utilizada por el backend. |
| IDaaS / OAuth2 / SSO | La autenticacion se delega a un proveedor externo de identidad. | Permite inicio de sesion unico, MFA y administracion centralizada. | Requiere integracion con servicios externos y configuracion adicional. | Media. Es una mejora futura posible. |

La estrategia seleccionada es autenticacion con usuarios almacenados en base de datos, roles gestionados mediante JPA y tokens JWT para proteger las solicitudes HTTP. Esta combinacion mantiene una arquitectura REST stateless y permite aplicar autorizacion por rol sobre endpoints criticos.

## 8. Estrategia seleccionada y matriz de permisos

La estrategia de seguridad del backend se basa en Spring Security, JWT, `UserDetailsService`, `BCryptPasswordEncoder` y reglas de autorizacion declaradas en `SecurityConfig`.

Los perfiles funcionales definidos son:

| Rol tecnico | Perfil funcional | Descripcion |
|---|---|---|
| `ROLE_CLIENTE` | Cliente | Usuario final que consulta productos y utiliza el flujo de carrito. |
| `ROLE_EMPLEADO` | Cajero | Usuario operativo que registra ventas y movimientos de inventario. |
| `ROLE_GERENTE` | Administrador | Usuario administrativo con permisos para gestionar usuarios y productos. |

La matriz de permisos queda definida de la siguiente forma:

| Recurso / Endpoint | Cliente | Cajero | Administrador |
|---|---:|---:|---:|
| `/api/auth/**` | Publico | Publico | Publico |
| `/public/**` | Publico | Publico | Publico |
| `GET /api/productos/**` | Permitido | Permitido | Permitido |
| `POST /api/productos/**` | Denegado | Denegado | Permitido |
| `PUT /api/productos/**` | Denegado | Denegado | Permitido |
| `DELETE /api/productos/**` | Denegado | Denegado | Permitido |
| `GET /api/categorias/**` | Permitido | Permitido | Permitido |
| `POST /api/categorias/**` | Denegado | Permitido | Permitido |
| `PUT /api/categorias/**` | Denegado | Permitido | Permitido |
| `DELETE /api/categorias/**` | Denegado | Denegado | Permitido |
| `/api/carrito/**` | Permitido | Permitido | Permitido |
| `GET /api/ventas/**` | Denegado | Permitido | Permitido |
| `POST /api/ventas/**` | Denegado | Permitido | Denegado |
| `/api/detalle-ventas/**` | Denegado | Permitido | Permitido |
| `/api/inventario/**` | Denegado | Permitido | Permitido |
| `/api/usuarios/**` | Denegado | Denegado | Permitido |

Esta matriz separa las responsabilidades principales del sistema. El cliente mantiene acceso al flujo de consulta y carrito. El cajero concentra las operaciones de venta e inventario. El administrador gestiona productos y usuarios, sin intervenir en la generacion directa de ventas, ya que esa operacion corresponde al flujo operativo del cajero.

## 9. Configuracion de autorizacion implementada

La configuracion de autorizacion se define en la clase `SecurityConfig`, donde se establece una politica stateless y se deshabilitan mecanismos de sesion tradicionales, HTTP Basic y formulario de login. La autenticacion se realiza mediante JWT, validado en cada solicitud protegida por `JwtAuthenticationFilter`.

La configuracion permite acceso publico a:

- `/api/auth/**`
- `/public/**`
- `/error`
- Consola H2 para el entorno de pruebas

El resto de endpoints se protege mediante reglas por metodo HTTP y rol. Las principales restricciones son:

- La consulta de productos esta disponible para cliente, cajero y administrador.
- La creacion, edicion y eliminacion de productos queda reservada al administrador.
- Los movimientos de inventario quedan permitidos para cajero y administrador.
- La generacion de ventas queda reservada al cajero.
- La administracion de usuarios queda reservada al administrador.
- Los accesos sin token JWT reciben `401 Unauthorized`.
- Los accesos con rol insuficiente reciben `403 Forbidden`.

Esta configuracion permite validar la identidad del usuario mediante token JWT y, posteriormente, verificar si el rol asociado tiene permisos para ejecutar la operacion solicitada.

## 10. Mitigacion de amenazas mediante la configuracion implementada

La configuracion de seguridad del backend permite mitigar las amenazas principales identificadas para "MiniMarket Plus".

### 10.1 Accesos no autorizados

Los endpoints privados requieren autenticacion mediante token JWT. Si una solicitud no incluye token, el sistema responde con `401 Unauthorized`.

Esta medida protege recursos como productos, inventario, ventas, carrito, detalle de ventas y usuarios, evitando que usuarios no autenticados ejecuten operaciones internas del backend.

### 10.2 Accesos con rol insuficiente

La autorizacion se aplica mediante reglas por rol en `SecurityConfig`. Cuando un usuario autenticado intenta ejecutar una operacion no permitida para su perfil, el sistema responde con `403 Forbidden`.

Ejemplos validados:

- Un cliente no puede modificar productos.
- Un cajero no puede modificar productos.
- Un cliente no puede registrar movimientos de inventario.
- Un cliente no puede generar ventas.
- Un administrador no genera ventas en la pauta estricta, ya que esa operacion queda reservada al cajero.

### 10.3 Proteccion de productos

La modificacion de productos se restringe al administrador. Esto protege la integridad del catalogo, evitando que perfiles operativos o clientes alteren precios, stock o informacion del producto.

Permisos principales:

- Cliente: consulta productos.
- Cajero: consulta productos.
- Administrador: crea, edita y elimina productos.

### 10.4 Proteccion de inventario

Los movimientos de inventario quedan restringidos a cajero y administrador. Esto permite controlar entradas y salidas de productos, evitando que clientes manipulen registros internos de stock.

Ademas, la capa de servicio valida que cada movimiento este asociado a un producto, tenga cantidad valida y utilice un tipo de movimiento permitido.

### 10.5 Proteccion de ventas

La generacion de ventas queda reservada al cajero. Esta separacion permite diferenciar al cliente asociado a la compra del usuario operativo que registra la venta en el sistema.

La capa de servicio valida que la venta tenga usuario, detalles, productos existentes, cantidades validas y stock suficiente. Tambien calcula el total y descuenta stock durante el registro de la venta.

### 10.6 Proteccion de contrasenas

Las contrasenas se protegen mediante `BCryptPasswordEncoder`, evitando almacenarlas en texto plano. Esto reduce el impacto de una eventual exposicion de la base de datos.

### 10.7 Proteccion de tokens JWT

El token JWT se genera durante el login y se valida en cada solicitud protegida. El filtro `JwtAuthenticationFilter` verifica el encabezado `Authorization`, extrae el usuario, valida el token y registra la autenticacion en el contexto de seguridad.

### 10.8 Reduccion de exposicion de datos sensibles

El backend evita exponer directamente la contrasena del usuario mediante control de serializacion. Como mejora futura, se recomienda ampliar el uso de DTOs para separar las entidades internas de las respuestas publicas de la API.

### 10.9 Consistencia en el registro de ventas

El registro de ventas se ejecuta de forma transaccional. Esto permite que el descuento de stock y el guardado de la venta se comporten como una sola operacion, reduciendo el riesgo de inconsistencias si ocurre un error durante el proceso.

## 11. Conclusion tecnica

El backend de "MiniMarket Plus" cuenta con una base de seguridad adecuada para proteger operaciones criticas mediante Spring Security, JWT, BCrypt y autorizacion por roles.

La configuracion distingue tres perfiles funcionales: cliente, cajero y administrador. Esta separacion permite limitar el acceso segun la responsabilidad de cada usuario dentro del sistema. El cliente conserva acceso al flujo de consulta y carrito, el cajero ejecuta operaciones de venta e inventario, y el administrador gestiona productos y usuarios.

Las pruebas automatizadas confirman el comportamiento esperado de autenticacion y autorizacion. Se valida que las solicitudes sin token sean rechazadas con `401 Unauthorized`, que los usuarios con rol insuficiente reciban `403 Forbidden`, y que los roles autorizados puedan ejecutar las operaciones permitidas.

La suite completa del proyecto se ejecuta correctamente con 73 pruebas aprobadas. Ademas, el reporte JaCoCo evidencia cobertura general del backend y una cobertura destacada en los paquetes asociados a seguridad.

Como mejoras futuras se recomienda ampliar el uso de DTOs, agregar control de propiedad de recursos, reforzar la trazabilidad de eventos sensibles y aumentar la cobertura de pruebas sobre controladores adicionales.
