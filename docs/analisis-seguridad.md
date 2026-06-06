# Análisis de seguridad del backend - MiniMarket Plus

## 1. Contexto del backend analizado

El backend proporcionado corresponde a una aplicación desarrollada con Spring Boot para el sistema "MiniMarket Plus". La aplicación mantiene una arquitectura por capas, separando controladores, servicios, repositorios, entidades y configuración de seguridad.

Durante la revisión del proyecto se identificó que el backend ya incorpora Spring Security como framework de seguridad, además de entidades relacionadas con usuarios y roles. También se verificó que el proyecto cuenta con autenticación basada en JWT, configuración stateless, servicio UserDetailsService, cifrado de contraseñas con BCrypt y reglas de autorización por roles. Por lo tanto, el análisis actual se enfoca en validar, documentar y reforzar la seguridad existente de acuerdo con los requerimientos de MiniMarket Plus.

## 2. Componentes relevantes de seguridad existentes

En el backend se identifican los siguientes componentes relacionados con seguridad:

- Entidad `Usuario`, utilizada para representar las cuentas del sistema.
- Entidad `Rol`, utilizada para representar los permisos o niveles de acceso.
- Repositorio `UsuarioRepository`, utilizado para buscar usuarios registrados.
- Repositorio `RolRepository`, utilizado para gestionar roles.
- Clase `CustomUserDetails`, encargada de adaptar la entidad `Usuario` al modelo de autenticación de Spring Security.
- Clase `CustomUserDetailsService`, encargada de cargar usuarios desde la base de datos.
- Clase `SecurityConfig`, donde se define la configuración principal de Spring Security.
- Clase `JwtUtil`, encargada de generar, firmar, validar y leer tokens JWT.
- Clase `JwtAuthenticationFilter`, encargada de interceptar las solicitudes protegidas y validar el token recibido en el encabezado `Authorization`.
- DTOs `LoginRequest`, `RegisterRequest` y `AuthResponse`, utilizados para el flujo de autenticación y registro.
- Controlador `AuthController`, encargado de exponer los endpoints de login y registro.
- Bean `BCryptPasswordEncoder`, utilizado para proteger contraseñas mediante cifrado unidireccional.

Estos componentes demuestran que la aplicación ya posee una base funcional de autenticación y autorización mediante Spring Security y JWT. La tarea principal consiste en validar que la configuración esté correctamente aplicada, documentar el funcionamiento, probar los accesos por rol y reforzar la protección frente a amenazas comunes.

## 3. Puntos críticos identificados

A partir de la revisión del backend se identifican los siguientes puntos críticos:

1. La aplicación utiliza Spring Security con configuración stateless y autenticación mediante JWT.
2. Existen entidades `Usuario` y `Rol`, junto con reglas de autorización por tipo de usuario.
3. El sistema requiere proteger operaciones sensibles como gestión de usuarios, productos, ventas, carrito e inventario.
4. El backend puede exponer información sensible si devuelve directamente entidades completas, especialmente en el caso de usuarios con contraseña.
5. Es necesario asegurar que las contraseñas se almacenen usando `BCryptPasswordEncoder` y no en texto plano.
6. Existen usuarios iniciales para probar autenticación y autorización: cliente, empleado y gerente.
7. JWT se encuentra implementado mediante `JwtUtil` y `JwtAuthenticationFilter`, por lo que corresponde mantener esta estrategia y documentarla correctamente.
8. Existe manejo básico de errores de autenticación y autorización, pero aún se recomienda fortalecer el registro de eventos sospechosos, como intentos fallidos de autenticación, accesos denegados y operaciones sensibles.

## 4. Necesidades de seguridad del cliente

Para el caso del sistema "MiniMarket Plus", el cliente requiere proteger los componentes backend frente a accesos indebidos y amenazas comunes. La aplicación administra información asociada a usuarios, roles, productos, carrito, ventas e inventario, por lo que se deben aplicar controles de autenticación, autorización, protección de datos y validación de solicitudes mediante JWT.

Las principales necesidades son:

- Permitir que solo usuarios autenticados accedan a recursos privados.
- Diferenciar permisos entre clientes, empleados y gerentes.
- Proteger credenciales mediante almacenamiento seguro de contraseñas.
- Evitar exposición de datos sensibles en respuestas de la API.
- Restringir operaciones administrativas solo a usuarios autorizados.
- Registrar eventos relevantes de seguridad para apoyar la trazabilidad.
- Mantener una configuración clara y coherente con el alcance inicial del proyecto.

## 5. Amenazas potenciales identificadas

A partir del análisis del backend proporcionado y de los requerimientos de la asignatura, se identifican las siguientes amenazas potenciales que pueden afectar al sistema "MiniMarket Plus":

### 5.1 Accesos no autorizados

El backend expone endpoints asociados a usuarios, productos, categorías, carrito, ventas, detalles de venta e inventario. Si estos recursos no se protegen correctamente, un usuario sin permisos podría acceder a información o ejecutar operaciones que no le corresponden.

Esta amenaza puede impactar directamente en la confidencialidad e integridad del sistema, ya que permitiría consultar o modificar información sensible, como datos de usuarios, ventas registradas o movimientos de inventario.

### 5.2 Manipulación o uso indebido de tokens JWT

Dado que el backend utiliza autenticación mediante JWT, una amenaza relevante es el uso de tokens inválidos, expirados, mal formados o manipulados. Si el token no se valida correctamente, un atacante podría intentar acceder a recursos protegidos sin una autenticación legítima.

Esta amenaza se mitiga mediante la firma del token, la validación de expiración, la extracción segura del usuario autenticado y la verificación del token en cada solicitud protegida mediante el filtro JWT.

### 5.3 Exposición de datos sensibles

La entidad `Usuario` contiene información sensible, especialmente el campo `password`. Si el backend devuelve directamente entidades completas en las respuestas de la API, existe el riesgo de exponer contraseñas u otros datos internos que no deberían ser visibles para los clientes del sistema.

Esta amenaza afecta principalmente la confidencialidad de los datos personales y credenciales de acceso.

### 5.4 Almacenamiento inseguro de contraseñas

Aunque el proyecto ya define un `BCryptPasswordEncoder`, es necesario asegurar que las contraseñas sean cifradas antes de almacenarse en la base de datos. Si una contraseña se guarda en texto plano, un acceso indebido a la base de datos permitiría comprometer directamente las cuentas de los usuarios.

Esta amenaza afecta la seguridad de la autenticación y puede facilitar accesos no autorizados.

### 5.5 Falta de autorización por roles

El sistema define usuarios y roles, pero la seguridad debe garantizar que cada tipo de usuario solo acceda a las funciones que le corresponden. Sin una configuración clara de autorización, un cliente podría acceder a funciones administrativas, o un empleado podría ejecutar acciones reservadas para un gerente.

Esta amenaza afecta la separación de responsabilidades dentro del sistema.

### 5.6 Acceso indebido a recursos de otros usuarios

Además del control por roles, el backend debe considerar el riesgo de acceso indebido a recursos ajenos. Por ejemplo, un cliente podría intentar modificar el identificador de una venta, carrito o usuario para acceder a información que no le corresponde.

Esta amenaza se conoce como IDOR y puede afectar la confidencialidad de datos personales y transaccionales. Para mitigarla, no basta con validar el rol del usuario; también se debe validar la relación entre el recurso solicitado y el usuario autenticado cuando corresponda.

### 5.7 Ataques CSRF

En aplicaciones web tradicionales basadas en sesiones y cookies, puede existir riesgo de ataques CSRF, donde un atacante intenta forzar acciones no deseadas aprovechando una sesión activa del usuario.

En este backend, el riesgo se reduce porque la autenticación se basa en JWT y en una arquitectura stateless. Las solicitudes protegidas deben incluir explícitamente el token en el encabezado `Authorization`, por lo que una petición sin token válido debe ser rechazada por el backend.

### 5.8 Ataques XSS

Aunque el backend no presenta vistas HTML complejas, puede recibir y devolver datos ingresados por usuarios, como nombres de productos, categorías o usuarios. Si estos datos luego son mostrados por un frontend sin validación o escape adecuado, podrían facilitar ataques XSS.

Esta amenaza se mitiga principalmente validando entradas y evitando devolver contenido inseguro.

### 5.9 Inyección SQL

El proyecto utiliza Spring Data JPA y repositorios, lo que reduce el riesgo de inyección SQL al evitar la construcción manual de consultas inseguras. Sin embargo, la amenaza debe considerarse porque una mala práctica futura, como concatenar parámetros directamente en consultas, podría introducir vulnerabilidades.

Esta amenaza afecta la integridad y confidencialidad de los datos almacenados.

### 5.10 Falta de monitoreo y trazabilidad

Actualmente existe un manejo básico de errores de autenticación y autorización, pero no se observa un sistema completo de monitoreo o auditoría para intentos fallidos, accesos denegados u operaciones sensibles. Sin registros suficientes de seguridad, sería difícil detectar actividad sospechosa o investigar incidentes.

Esta amenaza afecta la capacidad de auditoría y respuesta ante incidentes.

## 6. Tipos de usuarios y requerimientos de seguridad

Para implementar una estrategia de seguridad adecuada en el backend de "MiniMarket Plus", se definen tres tipos principales de usuarios: clientes, empleados y gerentes. Cada uno tiene un nivel de acceso distinto según sus responsabilidades dentro del sistema.

### 6.1 Cliente

El cliente representa al usuario final que interactúa con el sistema para consultar productos, gestionar su carrito y realizar compras.

Permisos esperados:

- Consultar productos y categorías disponibles.
- Gestionar su propio carrito.
- Registrar compras o ventas asociadas a su cuenta.
- Consultar información relacionada con sus propias operaciones.

Nivel de seguridad requerido:

El cliente debe autenticarse con nombre de usuario y contraseña mediante el endpoint de login. Una vez autenticado, debe utilizar el token JWT recibido para acceder a los recursos protegidos. Su acceso debe estar limitado a operaciones propias, evitando que pueda consultar o modificar información de otros usuarios, inventario interno o datos administrativos.

### 6.2 Empleado

El empleado representa al usuario interno encargado de apoyar la operación diaria del minimarket.

Permisos esperados:

- Consultar productos y categorías.
- Gestionar productos.
- Registrar o revisar ventas.
- Gestionar movimientos de inventario.
- Revisar carritos o ventas cuando sea necesario para la operación.

Nivel de seguridad requerido:

El empleado requiere autenticación obligatoria mediante JWT y autorización mediante rol. Su acceso debe permitir operaciones operativas, pero no debería incluir administración completa de usuarios o roles.

### 6.3 Gerente

El gerente representa al usuario con mayor nivel de responsabilidad dentro del sistema. Para efectos del caso MiniMarket Plus, este rol cumple la función de administrador del backend.

Permisos esperados:

- Gestionar usuarios.
- Gestionar roles.
- Administrar productos, ventas e inventario.
- Revisar información general del sistema.
- Acceder a operaciones administrativas sensibles.

Nivel de seguridad requerido:

El gerente requiere el nivel de seguridad más alto dentro de la implementación. Sus credenciales deben estar protegidas mediante BCrypt y su acceso debe estar limitado mediante autorización por rol y validación del token JWT, evitando que otros usuarios puedan ejecutar funciones administrativas.

### 6.4 Requerimientos generales de seguridad

A partir de los tipos de usuarios definidos, se establecen los siguientes requerimientos de seguridad:

1. Todo recurso privado del backend debe requerir autenticación mediante token JWT válido.
2. Las contraseñas deben almacenarse cifradas mediante `BCryptPasswordEncoder`.
3. Los endpoints deben protegerse según el rol del usuario autenticado.
4. El token JWT debe ser validado en cada solicitud protegida mediante un filtro de seguridad.
5. Los datos sensibles, como contraseñas, no deben exponerse en respuestas JSON.
6. Las operaciones administrativas deben estar restringidas al rol de gerente.
7. Las operaciones de inventario y productos deben restringirse a empleados y gerentes.
8. Las operaciones de carrito deben estar disponibles para usuarios autenticados con rol de cliente.
9. La aplicación debe registrar eventos relevantes de seguridad, como accesos denegados o intentos fallidos.
10. La configuración debe ser coherente con una API REST stateless basada en JWT.

## 7. Comparación de estrategias de autenticación

Para seleccionar una estrategia de autenticación adecuada, se comparan distintas alternativas disponibles en el contexto de Spring Security y aplicaciones backend.

| Estrategia | Descripción | Ventajas | Desventajas | Aplicabilidad al proyecto |
|---|---|---|---|---|
| Autenticación en memoria | Los usuarios se definen directamente en la configuración de seguridad. | Fácil de implementar y útil para pruebas rápidas. | No es adecuada para producción, no permite administrar usuarios reales desde base de datos y no escala para un sistema con clientes, empleados y gerentes. | Baja. Puede servir para pruebas, pero no responde a las necesidades reales del sistema. |
| Autenticación con base de datos usando JPA | Los usuarios y roles se almacenan en la base de datos y se cargan mediante `UserDetailsService`. | Se integra bien con el backend actual, permite administrar usuarios reales y aprovecha las entidades `Usuario` y `Rol` existentes. | Requiere configurar correctamente el cifrado de contraseñas y la autorización por roles. | Alta. Es la estrategia más coherente con el estado actual del proyecto. |
| Autenticación JDBC | Spring Security consulta usuarios y roles directamente desde tablas mediante JDBC. | Es una estrategia válida y soportada por Spring Security. | El proyecto ya utiliza Spring Data JPA, por lo que usar JDBC directo sería menos coherente con la arquitectura existente. | Media. Es posible, pero no es la opción más alineada con el backend recibido. |
| LDAP / LDAPS | La autenticación se realiza contra un directorio corporativo externo, como Active Directory u OpenLDAP. En LDAPS, la comunicación se cifra mediante SSL/TLS. | Permite centralizar usuarios corporativos, especialmente empleados y administradores, evitando duplicar credenciales en cada sistema. LDAPS agrega protección en tránsito para credenciales y datos sensibles. | Requiere infraestructura externa, configuración de certificados, servidor de directorio y administración centralizada de usuarios. | Media. Es una alternativa relevante para un entorno empresarial, pero no se implementa directamente en esta entrega porque el backend base no dispone de un servidor LDAPS configurado. |
| JWT | La autenticación se realiza mediante tokens enviados por el cliente en cada solicitud protegida. | Es adecuada para APIs REST stateless, aplicaciones con frontend separado y clientes móviles. Además, permite evitar sesiones tradicionales en el servidor. | Requiere una implementación correcta de generación, firma, expiración y validación de tokens. También exige proteger adecuadamente la clave secreta. | Alta. Es la estrategia seleccionada porque el proyecto ya cuenta con `JwtUtil`, `JwtAuthenticationFilter`, login mediante `AuthController` y configuración stateless en `SecurityConfig`. |
| IDaaS / OAuth2 / SSO | La autenticación se delega a un proveedor externo de identidad, como un servicio en la nube para gestión de usuarios, inicio de sesión único, autenticación multifactor y control centralizado de accesos. | Mejora la interoperabilidad, permite escalar la gestión de identidades, facilita MFA y reduce la necesidad de construir todo el sistema de identidad desde cero. | Requiere integración con un proveedor externo, configuración de aplicaciones, manejo de flujos OAuth2/OIDC y dependencia de un servicio externo. | Media. Es una alternativa adecuada para una evolución futura de MiniMarket Plus, pero no se implementa directamente en esta entrega por alcance e infraestructura disponible. |

### 7.1 Consideración de LDAPS e IDaaS

Además de las estrategias locales de autenticación, se analizaron mecanismos externos de identidad como LDAPS e IDaaS.

LDAPS resulta relevante en escenarios donde MiniMarket Plus necesite autenticar empleados o administradores contra un directorio corporativo centralizado, como Active Directory u OpenLDAP. A diferencia de LDAP tradicional, LDAPS cifra la comunicación mediante SSL/TLS, evitando que credenciales y datos sensibles viajen expuestos por la red.

IDaaS, por su parte, permite delegar la gestión de identidad a un proveedor externo. Este enfoque es útil cuando una organización requiere inicio de sesión único, autenticación multifactor, administración centralizada de usuarios, integración con múltiples sistemas e interoperabilidad con servicios externos.

En esta implementación no se integró un servidor LDAPS ni un proveedor IDaaS real, ya que el backend proporcionado no incluye dicha infraestructura externa y el requerimiento funcional directo solicita JWT para una API REST stateless. Sin embargo, ambas alternativas se consideran relevantes para una evolución futura del sistema.

### 7.2 Estrategia más adecuada

Considerando el estado del backend y los requerimientos de la asignatura, la estrategia más adecuada es utilizar autenticación con nombre de usuario y contraseña para el inicio de sesión, junto con generación de JWT para proteger las solicitudes posteriores en una arquitectura stateless.

Esta decisión se justifica porque el proyecto ya cuenta con entidades `Usuario` y `Rol`, repositorios asociados, una clase `CustomUserDetailsService` preparada para cargar usuarios desde la base de datos, configuración de Spring Security, generación de tokens mediante `JwtUtil` y validación de solicitudes mediante `JwtAuthenticationFilter`.

LDAPS e IDaaS se consideran alternativas válidas para escenarios empresariales con autenticación externa, directorios corporativos, inicio de sesión único o autenticación multifactor. Sin embargo, para esta entrega se selecciona JWT como mecanismo funcional principal porque responde directamente al requerimiento de trabajar con una API REST stateless y no depende de infraestructura externa adicional.

La estrategia seleccionada permite cumplir los objetivos principales:

- Autenticar usuarios mediante nombre de usuario y contraseña.
- Generar un token JWT después de un login correcto.
- Validar el token JWT en cada solicitud protegida.
- Almacenar contraseñas de forma segura usando `BCryptPasswordEncoder`.
- Diferenciar permisos mediante roles.
- Proteger endpoints sensibles del backend.
- Mantener una solución stateless coherente con la arquitectura REST del proyecto.

## 8. Estrategia seleccionada y matriz inicial de permisos

Luego de comparar las estrategias de autenticación disponibles, se selecciona como estrategia principal el uso de Spring Security con autenticación mediante nombre de usuario y contraseña, generación de tokens JWT, usuarios almacenados en base de datos, contraseñas protegidas con `BCryptPasswordEncoder` y autorización basada en roles.

Esta estrategia se considera la más adecuada para el backend de "MiniMarket Plus" porque se alinea con la estructura existente del proyecto y con los requerimientos de la asignatura. La aplicación ya cuenta con entidades `Usuario` y `Rol`, repositorios JPA, clases de integración con Spring Security, configuración stateless, generación de tokens mediante `JwtUtil` y validación de solicitudes mediante `JwtAuthenticationFilter`.

No se selecciona autenticación en memoria porque solo es útil para pruebas simples y no permite administrar usuarios reales. JWT se selecciona como parte central de la solución, ya que permite proteger una API REST stateless, evita mantener sesiones tradicionales en el servidor y se ajusta a los requerimientos de autenticación segura del proyecto.

LDAPS e IDaaS se analizan como alternativas de integración con servicios externos de identidad. LDAPS sería apropiado si MiniMarket Plus contara con un directorio corporativo para autenticar empleados y administradores. IDaaS sería apropiado si la empresa buscara inicio de sesión único, autenticación multifactor y administración centralizada de identidades en la nube. En esta entrega no se implementan directamente porque requieren infraestructura externa adicional, pero se consideran alternativas relevantes para mejorar interoperabilidad y escalabilidad en una evolución futura.

### 8.1 Framework seleccionado

El framework seleccionado es `Spring Security`, debido a que se integra directamente con Spring Boot y permite implementar autenticación, autorización, protección de rutas, manejo de usuarios, roles y cifrado de contraseñas.

Además, el proyecto ya incluye la dependencia de Spring Security y una configuración mediante la clase `SecurityConfig`, donde se definen rutas públicas, rutas protegidas, manejo stateless, filtro JWT, reglas por rol y codificación de contraseñas. Por lo tanto, la tarea principal consiste en validar, documentar y probar correctamente esta configuración.

### 8.2 Método de autenticación seleccionado

El método seleccionado es autenticación con nombre de usuario y contraseña para el inicio de sesión, junto con generación y validación de tokens JWT para proteger las solicitudes posteriores.

La implementación se realiza utilizando:

- `Usuario` como entidad principal de autenticación.
- `Rol` como entidad para definir niveles de acceso.
- `UsuarioRepository` para buscar usuarios por nombre de usuario.
- `CustomUserDetailsService` para cargar usuarios desde base de datos.
- `CustomUserDetails` para adaptar los usuarios del sistema al modelo de Spring Security.
- `BCryptPasswordEncoder` para proteger las contraseñas antes de almacenarlas.
- `AuthController` para exponer los endpoints de registro e inicio de sesión.
- `LoginRequest`, `RegisterRequest` y `AuthResponse` como DTOs del flujo de autenticación.
- `JwtUtil` para generar, firmar, validar y leer tokens JWT.
- `JwtAuthenticationFilter` para validar el token en cada solicitud protegida.
- Reglas de autorización por endpoint dentro de `SecurityConfig`.

### 8.3 Roles definidos

Se definen tres roles principales:

| Rol | Tipo de usuario | Descripción |
|---|---|---|
| `ROLE_CLIENTE` | Cliente | Usuario final que consulta productos, administra su carrito y realiza compras. |
| `ROLE_EMPLEADO` | Empleado | Usuario interno que gestiona productos, ventas e inventario. |
| `ROLE_GERENTE` | Gerente / Administrador | Usuario administrativo con permisos para gestionar usuarios, roles y operaciones críticas. |

### 8.4 Matriz inicial de permisos

| Recurso / Endpoint | Cliente | Empleado | Gerente |
|---|---:|---:|---:|
| `/api/auth/**` | Público | Público | Público |
| `/public/**` | Público | Público | Público |
| `GET /api/productos/**` | Permitido | Permitido | Permitido |
| `POST /api/productos/**` | Denegado | Permitido | Permitido |
| `PUT /api/productos/**` | Denegado | Permitido | Permitido |
| `DELETE /api/productos/**` | Denegado | Denegado | Permitido |
| `GET /api/categorias/**` | Permitido | Permitido | Permitido |
| `POST /api/categorias/**` | Denegado | Permitido | Permitido |
| `PUT /api/categorias/**` | Denegado | Permitido | Permitido |
| `DELETE /api/categorias/**` | Denegado | Denegado | Permitido |
| `/api/carrito/**` | Permitido | Permitido | Permitido |
| `GET /api/ventas/**` | Denegado | Permitido | Permitido |
| `POST /api/ventas/**` | Permitido | Permitido | Permitido |
| `/api/detalle-ventas/**` | Denegado | Permitido | Permitido |
| `/api/inventario/**` | Denegado | Permitido | Permitido |
| `/api/usuarios/**` | Denegado | Denegado | Permitido |

### 8.5 Justificación de la matriz de permisos

La matriz de permisos busca separar las responsabilidades de cada tipo de usuario. El cliente solo debe acceder a funciones relacionadas con la consulta de productos, carrito y creación de compras. El empleado requiere permisos operativos para administrar productos, categorías, ventas e inventario, pero no debería tener control total sobre usuarios. El gerente representa el perfil administrador del sistema y posee el mayor nivel de acceso, por lo que puede administrar usuarios y realizar operaciones críticas.

Esta separación permite mitigar accesos no autorizados y reducir el impacto de una cuenta comprometida, ya que cada usuario queda limitado a las funciones propias de su rol.

## 9. Configuración de autorización implementada

Luego de seleccionar Spring Security como framework de seguridad, se configuró una matriz de autorización basada en roles dentro de la clase `SecurityConfig`. Esta configuración permite definir rutas públicas, rutas protegidas, reglas por método HTTP y restricciones según el rol del usuario autenticado.

La autorización se implementó utilizando los roles definidos previamente:

- `ROLE_CLIENTE`
- `ROLE_EMPLEADO`
- `ROLE_GERENTE`

En la configuración de Spring Security se utilizaron reglas por endpoint y método HTTP, permitiendo que cada tipo de usuario acceda únicamente a las operaciones correspondientes a su responsabilidad dentro del sistema. Además, la configuración utiliza una arquitectura stateless, por lo que cada solicitud protegida debe incluir un token JWT válido.

### 9.1 Endpoints públicos

Se mantienen como públicos los endpoints necesarios para iniciar sesión, registrar usuarios y realizar pruebas básicas de disponibilidad.

```text
/api/auth/**
/public/**
/error
/h2-console/**
```

## 10. Mitigación de amenazas mediante la configuración implementada

La configuración de seguridad aplicada en el backend de "MiniMarket Plus" permite reducir los riesgos asociados a accesos indebidos, exposición de datos sensibles y ataques comunes contra aplicaciones backend.

### 10.1 Accesos no autorizados

La amenaza de accesos no autorizados se mitiga mediante autenticación obligatoria para todos los endpoints privados. La configuración de Spring Security permite el acceso público únicamente a rutas controladas como `/api/auth/**` y `/public/**`, mientras que los recursos protegidos del backend requieren un token JWT válido.

Además, el uso de JWT permite mantener una arquitectura stateless. Esto significa que el backend no mantiene sesiones tradicionales en el servidor, sino que valida el token enviado por el cliente en cada solicitud protegida.

Ejemplo de protección aplicada:

- Un cliente autenticado puede consultar productos.
- Un cliente autenticado no puede acceder a la administración de usuarios.
- Un gerente autenticado sí puede acceder a la administración de usuarios.

### 10.2 Exposición de datos sensibles

La exposición de datos sensibles se mitiga evitando que el campo `password` de la entidad `Usuario` sea devuelto en las respuestas JSON de la API.

Para esto se aplicó `@JsonIgnore` sobre el campo `password`, permitiendo que la contraseña siga existiendo internamente para el proceso de autenticación, pero sin ser visible para los consumidores de la API.

Esta medida protege la confidencialidad de las credenciales y reduce el impacto ante una consulta indebida al endpoint de usuarios.

### 10.3 Almacenamiento inseguro de contraseñas

El almacenamiento inseguro de contraseñas se mitiga mediante el uso de `BCryptPasswordEncoder`.

Las contraseñas de los usuarios iniciales se guardan cifradas durante la carga de datos mediante `DataInitializer`. Además, el servicio de usuarios codifica las contraseñas antes de persistirlas, evitando que se almacenen en texto plano.

Esta medida es fundamental porque, aunque alguien accediera a la base de datos, no obtendría directamente las contraseñas originales de los usuarios.

### 10.4 Ataques de inyección SQL

El riesgo de inyección SQL se reduce mediante el uso de Spring Data JPA y repositorios, ya que el acceso a datos se realiza principalmente a través de métodos definidos en interfaces como `UsuarioRepository`, `ProductoRepository`, `RolRepository` y otros repositorios del proyecto.

Al no construir consultas SQL manuales concatenando parámetros ingresados por el usuario, se disminuye la posibilidad de inyección SQL.

De todos modos, esta mitigación depende de mantener buenas prácticas en futuras modificaciones, evitando consultas dinámicas inseguras o concatenación directa de valores externos.

### 10.5 Ataques XSS

El backend no renderiza vistas HTML complejas ni genera páginas dinámicas con datos ingresados por usuarios. Sin embargo, puede recibir y devolver información como nombres de productos, categorías o usuarios.

El riesgo de XSS se considera principalmente asociado a un posible frontend que consuma esta API. Para mitigarlo, el backend debe mantener respuestas JSON controladas, evitar devolver contenido innecesario o sensible y validar adecuadamente los datos que recibe.

En esta implementación inicial, la principal medida aplicada es reducir la exposición de información sensible y mantener la API separada de la generación directa de HTML.

### 10.6 Ataques CSRF

La configuración actual deshabilita CSRF porque el backend funciona como una API REST stateless protegida mediante JWT. En este enfoque, el servidor no mantiene sesiones tradicionales basadas en cookies, sino que cada solicitud protegida debe incluir explícitamente el token en el encabezado `Authorization`.

Esta decisión es coherente con el uso de JWT, ya que una solicitud sin token válido debe ser rechazada por el backend. Por lo tanto, el riesgo CSRF se reduce en comparación con aplicaciones web tradicionales basadas en sesiones y formularios.

Sin embargo, si el sistema evoluciona hacia una aplicación web que utilice cookies de sesión, formularios tradicionales o autenticación basada en navegador, la protección CSRF debería ser evaluada nuevamente.

### 10.7 Monitoreo y trazabilidad básica

Se incorporaron manejadores personalizados para controlar respuestas ante eventos relevantes de seguridad:

- `CustomAuthenticationEntryPoint`: registra intentos de acceso sin autenticación o con credenciales inválidas.
- `CustomAccessDeniedHandler`: registra accesos denegados por falta de permisos.

Estos manejadores permiten diferenciar errores de autenticación y autorización, mejorando la respuesta del backend frente a accesos no autorizados. Como mejora futura, se recomienda ampliar estos controles con registros de auditoría más detallados.

### 10.8 Relación entre amenazas y medidas aplicadas

| Amenaza identificada | Medida aplicada |
|---|---|
| Acceso no autorizado | Autenticación obligatoria con Spring Security y JWT |
| Usuario autenticado accediendo a funciones indebidas | Autorización por roles |
| Manipulación o uso indebido de token JWT | Firma, expiración y validación del token en cada solicitud protegida |
| Acceso a recursos de otros usuarios | Validación de permisos por rol y recomendación de validar propiedad del recurso |
| Exposición de contraseñas | `@JsonIgnore` en el campo `password` |
| Contraseñas en texto plano | Cifrado con `BCryptPasswordEncoder` |
| Inyección SQL | Uso de Spring Data JPA y repositorios |
| XSS | API JSON sin renderizado HTML directo y reducción de exposición de datos |
| CSRF | Uso de API REST stateless con token JWT en encabezado `Authorization` |
| Falta de monitoreo | Logs personalizados para 401 y 403 |

## 11. Conclusión inicial

El backend cuenta con una base adecuada de seguridad mediante Spring Security, autenticación con JWT, contraseñas protegidas con BCrypt y autorización basada en roles. Esta estrategia permite proteger los endpoints privados, diferenciar permisos entre clientes, empleados y gerentes, y mantener una arquitectura REST stateless.

A partir del análisis realizado, se concluye que la prioridad no es reemplazar la seguridad existente, sino validarla, documentarla y reforzarla mediante pruebas de acceso, pruebas contra amenazas comunes y actualización de la documentación técnica.