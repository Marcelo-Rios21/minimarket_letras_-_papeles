# Análisis de seguridad inicial - LETRAS & PAPELES

## 1. Contexto del backend analizado

El backend proporcionado corresponde a una aplicación desarrollada con Spring Boot para el sistema "LETRAS & PAPELES". La aplicación mantiene una arquitectura por capas, separando controladores, servicios, repositorios, entidades y configuración de seguridad.

Durante la revisión inicial se identificó que el proyecto ya incorpora Spring Security como framework de seguridad, además de entidades relacionadas con usuarios y roles. Sin embargo, la configuración actual se encuentra en una etapa inicial y requiere ajustes para cumplir correctamente con los requerimientos de autenticación, autorización y protección de datos.

El proyecto fue validado mediante compilación y ejecución de pruebas base usando Maven, obteniendo un resultado exitoso. Antes de esta validación fue necesario corregir el archivo `application.properties`, ya que presentaba un problema de codificación que impedía procesar correctamente los recursos del proyecto.

## 2. Componentes relevantes de seguridad existentes

En el backend se identifican los siguientes componentes relacionados con seguridad:

- Entidad `Usuario`, utilizada para representar las cuentas del sistema.
- Entidad `Rol`, utilizada para representar los permisos o niveles de acceso.
- Repositorio `UsuarioRepository`, utilizado para buscar usuarios registrados.
- Repositorio `RolRepository`, utilizado para gestionar roles.
- Clase `CustomUserDetails`, encargada de adaptar la entidad `Usuario` al modelo de autenticación de Spring Security.
- Clase `CustomUserDetailsService`, encargada de cargar usuarios desde la base de datos.
- Clase `SecurityConfig`, donde se define la configuración principal de Spring Security.
- Bean `BCryptPasswordEncoder`, utilizado para proteger contraseñas mediante cifrado unidireccional.

Estos componentes demuestran que la aplicación ya posee una base para implementar autenticación con nombre de usuario y contraseña. Sin embargo, todavía se requiere ordenar y completar la configuración para que los roles tengan efecto real sobre los endpoints protegidos.

## 3. Puntos críticos identificados

A partir de la revisión del backend se identifican los siguientes puntos críticos:

1. La aplicación utiliza Spring Security, pero la configuración actual solo diferencia entre rutas públicas y rutas autenticadas.
2. Existen entidades `Usuario` y `Rol`, pero todavía no hay una autorización completa por tipo de usuario.
3. El sistema requiere proteger operaciones sensibles como gestión de usuarios, productos, ventas, carrito e inventario.
4. El backend puede exponer información sensible si devuelve directamente entidades completas, especialmente en el caso de usuarios con contraseña.
5. Es necesario asegurar que las contraseñas se almacenen usando `BCryptPasswordEncoder` y no en texto plano.
6. No existe una estrategia clara de usuarios iniciales para probar autenticación y autorización.
7. La clase relacionada con JWT no presenta una implementación funcional, por lo que no corresponde utilizar JWT como estrategia principal en esta etapa.
8. No se identifica un mecanismo claro de registro o monitoreo de eventos sospechosos, como accesos denegados o intentos fallidos de autenticación.

## 4. Necesidades de seguridad del cliente

Para el caso del sistema "LETRAS & PAPELES", el cliente requiere proteger los componentes backend frente a accesos indebidos y amenazas comunes. La aplicación administra información asociada a usuarios, roles, productos, carrito, ventas e inventario, por lo que se deben aplicar controles mínimos de autenticación, autorización y protección de datos.

Las principales necesidades son:

- Permitir que solo usuarios autenticados accedan a recursos privados.
- Diferenciar permisos entre clientes, empleados y gerentes.
- Proteger credenciales mediante almacenamiento seguro de contraseñas.
- Evitar exposición de datos sensibles en respuestas de la API.
- Restringir operaciones administrativas solo a usuarios autorizados.
- Registrar eventos relevantes de seguridad para apoyar la trazabilidad.
- Mantener una configuración clara y coherente con el alcance inicial del proyecto.

## 5. Amenazas potenciales identificadas

A partir del análisis del backend proporcionado y de los requerimientos de la asignatura, se identifican las siguientes amenazas potenciales que pueden afectar al sistema "LETRAS & PAPELES":

### 5.1 Accesos no autorizados

El backend expone endpoints asociados a usuarios, productos, categorías, carrito, ventas, detalles de venta e inventario. Si estos recursos no se protegen correctamente, un usuario sin permisos podría acceder a información o ejecutar operaciones que no le corresponden.

Esta amenaza puede impactar directamente en la confidencialidad e integridad del sistema, ya que permitiría consultar o modificar información sensible, como datos de usuarios, ventas registradas o movimientos de inventario.

### 5.2 Exposición de datos sensibles

La entidad `Usuario` contiene información sensible, especialmente el campo `password`. Si el backend devuelve directamente entidades completas en las respuestas de la API, existe el riesgo de exponer contraseñas u otros datos internos que no deberían ser visibles para los clientes del sistema.

Esta amenaza afecta principalmente la confidencialidad de los datos personales y credenciales de acceso.

### 5.3 Almacenamiento inseguro de contraseñas

Aunque el proyecto ya define un `BCryptPasswordEncoder`, es necesario asegurar que las contraseñas sean cifradas antes de almacenarse en la base de datos. Si una contraseña se guarda en texto plano, un acceso indebido a la base de datos permitiría comprometer directamente las cuentas de los usuarios.

Esta amenaza afecta la seguridad de la autenticación y puede facilitar accesos no autorizados.

### 5.4 Falta de autorización por roles

El sistema define usuarios y roles, pero la seguridad debe garantizar que cada tipo de usuario solo acceda a las funciones que le corresponden. Sin una configuración clara de autorización, un cliente podría acceder a funciones administrativas, o un empleado podría ejecutar acciones reservadas para un gerente.

Esta amenaza afecta la separación de responsabilidades dentro del sistema.

### 5.5 Ataques CSRF

Si la aplicación utiliza autenticación basada en sesión o formularios, puede existir riesgo de ataques CSRF, donde un atacante intenta forzar acciones no deseadas aprovechando una sesión activa del usuario.

Esta amenaza debe evaluarse según el tipo de autenticación elegido y la forma en que se consumirá el backend.

### 5.6 Ataques XSS

Aunque el backend no presenta vistas HTML complejas, puede recibir y devolver datos ingresados por usuarios, como nombres de productos, categorías o usuarios. Si estos datos luego son mostrados por un frontend sin validación o escape adecuado, podrían facilitar ataques XSS.

Esta amenaza se mitiga principalmente validando entradas y evitando devolver contenido inseguro.

### 5.7 Inyección SQL

El proyecto utiliza Spring Data JPA y repositorios, lo que reduce el riesgo de inyección SQL al evitar la construcción manual de consultas inseguras. Sin embargo, la amenaza debe considerarse porque una mala práctica futura, como concatenar parámetros directamente en consultas, podría introducir vulnerabilidades.

Esta amenaza afecta la integridad y confidencialidad de los datos almacenados.

### 5.8 Falta de monitoreo y trazabilidad

Actualmente no se observa un mecanismo claro para registrar intentos fallidos de autenticación, accesos denegados u operaciones sensibles. Sin registros mínimos de seguridad, sería difícil detectar actividad sospechosa o investigar incidentes.

Esta amenaza afecta la capacidad de auditoría y respuesta ante incidentes.

## 6. Tipos de usuarios y requerimientos de seguridad

Para implementar una estrategia de seguridad adecuada en el backend de "LETRAS & PAPELES", se definen tres tipos principales de usuarios: clientes, empleados y gerentes. Cada uno tiene un nivel de acceso distinto según sus responsabilidades dentro del sistema.

### 6.1 Cliente

El cliente representa al usuario final que interactúa con el sistema para consultar productos, gestionar su carrito y realizar compras.

Permisos esperados:

- Consultar productos y categorías disponibles.
- Gestionar su propio carrito.
- Registrar compras o ventas asociadas a su cuenta.
- Consultar información relacionada con sus propias operaciones.

Nivel de seguridad requerido:

El cliente debe autenticarse con nombre de usuario y contraseña. Su acceso debe estar limitado a operaciones propias, evitando que pueda consultar o modificar información de otros usuarios, inventario interno o datos administrativos.

### 6.2 Empleado

El empleado representa al usuario interno encargado de apoyar la operación diaria del minimarket.

Permisos esperados:

- Consultar productos y categorías.
- Gestionar productos.
- Registrar o revisar ventas.
- Gestionar movimientos de inventario.
- Revisar carritos o ventas cuando sea necesario para la operación.

Nivel de seguridad requerido:

El empleado requiere autenticación obligatoria y autorización mediante rol. Su acceso debe permitir operaciones operativas, pero no debería incluir administración completa de usuarios o roles.

### 6.3 Gerente

El gerente representa al usuario con mayor nivel de responsabilidad dentro del sistema.

Permisos esperados:

- Gestionar usuarios.
- Gestionar roles.
- Administrar productos, ventas e inventario.
- Revisar información general del sistema.
- Acceder a operaciones administrativas sensibles.

Nivel de seguridad requerido:

El gerente requiere el nivel de seguridad más alto dentro de la implementación inicial. Sus credenciales deben estar protegidas mediante BCrypt y su acceso debe estar limitado mediante autorización por rol, evitando que otros usuarios puedan ejecutar funciones administrativas.

### 6.4 Requerimientos generales de seguridad

A partir de los tipos de usuarios definidos, se establecen los siguientes requerimientos de seguridad:

1. Todo recurso privado del backend debe requerir autenticación.
2. Las contraseñas deben almacenarse cifradas mediante `BCryptPasswordEncoder`.
3. Los endpoints deben protegerse según el rol del usuario autenticado.
4. Los datos sensibles, como contraseñas, no deben exponerse en respuestas JSON.
5. Las operaciones administrativas deben estar restringidas al rol de gerente.
6. Las operaciones de inventario y productos deben restringirse a empleados y gerentes.
7. Las operaciones de carrito deben estar disponibles para usuarios autenticados con rol de cliente.
8. La aplicación debe registrar eventos relevantes de seguridad, como accesos denegados o intentos fallidos.
9. La configuración debe ser coherente con una implementación inicial basada en nombre de usuario y contraseña.

## 7. Comparación de estrategias de autenticación

Para seleccionar una estrategia de autenticación adecuada, se comparan distintas alternativas disponibles en el contexto de Spring Security y aplicaciones backend.

| Estrategia | Descripción | Ventajas | Desventajas | Aplicabilidad al proyecto |
|---|---|---|---|---|
| Autenticación en memoria | Los usuarios se definen directamente en la configuración de seguridad. | Fácil de implementar y útil para pruebas rápidas. | No es adecuada para producción, no permite administrar usuarios reales desde base de datos y no escala para un sistema con clientes, empleados y gerentes. | Baja. Puede servir para pruebas, pero no responde a las necesidades reales del sistema. |
| Autenticación con base de datos usando JPA | Los usuarios y roles se almacenan en la base de datos y se cargan mediante `UserDetailsService`. | Se integra bien con el backend actual, permite administrar usuarios reales y aprovecha las entidades `Usuario` y `Rol` existentes. | Requiere configurar correctamente el cifrado de contraseñas y la autorización por roles. | Alta. Es la estrategia más coherente con el estado actual del proyecto. |
| Autenticación JDBC | Spring Security consulta usuarios y roles directamente desde tablas mediante JDBC. | Es una estrategia válida y soportada por Spring Security. | El proyecto ya utiliza Spring Data JPA, por lo que usar JDBC directo sería menos coherente con la arquitectura existente. | Media. Es posible, pero no es la opción más alineada con el backend recibido. |
| LDAP | La autenticación se realiza contra un directorio corporativo externo. | Es útil en organizaciones con infraestructura centralizada de usuarios. | Es más compleja, requiere un servidor LDAP y no corresponde al alcance inicial del minimarket. | Baja. No existe requerimiento de integración con directorio corporativo. |
| JWT | La autenticación se realiza mediante tokens enviados por el cliente en cada solicitud. | Es adecuada para APIs stateless y aplicaciones con frontend separado o clientes móviles. | Requiere una implementación adicional de generación, validación y expiración de tokens. En el proyecto actual existe una clase `JwtUtil`, pero no está implementada. | Media a baja para esta etapa. Puede ser útil a futuro, pero no corresponde a la implementación inicial solicitada. |
| OAuth2 / SSO | La autenticación se delega a proveedores externos o sistemas de inicio de sesión único. | Útil para integraciones avanzadas y autenticación federada. | Es más compleja y excede los requerimientos iniciales del cliente. | Baja. No es necesaria para esta asignatura ni para el alcance actual del backend. |

### 7.1 Estrategia más adecuada

Considerando el estado del backend y los requerimientos de la asignatura, la estrategia más adecuada es implementar autenticación con nombre de usuario y contraseña, utilizando usuarios almacenados en base de datos mediante Spring Data JPA.

Esta decisión se justifica porque el proyecto ya cuenta con entidades `Usuario` y `Rol`, repositorios asociados y una clase `CustomUserDetailsService` preparada para cargar usuarios desde la base de datos. Por lo tanto, no es necesario incorporar una estrategia externa más compleja como LDAP, OAuth2 o JWT para esta implementación inicial.

La estrategia seleccionada permite cumplir los objetivos principales:

- Autenticar usuarios mediante nombre de usuario y contraseña.
- Almacenar contraseñas de forma segura usando `BCryptPasswordEncoder`.
- Diferenciar permisos mediante roles.
- Proteger endpoints sensibles del backend.
- Mantener una solución coherente con la arquitectura existente del proyecto.

## 8. Estrategia seleccionada y matriz inicial de permisos

Luego de comparar las estrategias de autenticación disponibles, se selecciona como estrategia principal la autenticación con nombre de usuario y contraseña utilizando Spring Security, usuarios almacenados en base de datos, contraseñas protegidas con `BCryptPasswordEncoder` y autorización basada en roles.

Esta estrategia se considera la más adecuada para el backend de "LETRAS & PAPELES" porque se alinea con la estructura existente del proyecto. La aplicación ya cuenta con entidades `Usuario` y `Rol`, repositorios JPA y clases de integración con Spring Security, por lo que la implementación puede realizarse sin incorporar mecanismos externos más complejos.

No se selecciona autenticación en memoria porque solo es útil para pruebas simples y no permite administrar usuarios reales. Tampoco se selecciona LDAP porque no existe un requerimiento de integración con un directorio corporativo. La autenticación JWT se considera una alternativa válida para una etapa futura, especialmente si el sistema evoluciona hacia una API stateless consumida por frontend separado o aplicaciones móviles, pero no corresponde a la implementación inicial solicitada, ya que el requerimiento indica explícitamente autenticación con nombre de usuario y contraseña.

### 8.1 Framework seleccionado

El framework seleccionado es `Spring Security`, debido a que se integra directamente con Spring Boot y permite implementar autenticación, autorización, protección de rutas, manejo de usuarios, roles y cifrado de contraseñas.

Además, el proyecto ya incluye la dependencia de Spring Security y una configuración inicial mediante la clase `SecurityConfig`, por lo que la tarea principal consiste en completar y ordenar esta configuración.

### 8.2 Método de autenticación seleccionado

El método seleccionado es autenticación con nombre de usuario y contraseña.

La implementación se realizará utilizando:

- `Usuario` como entidad principal de autenticación.
- `Rol` como entidad para definir niveles de acceso.
- `UsuarioRepository` para buscar usuarios por nombre de usuario.
- `CustomUserDetailsService` para cargar usuarios desde base de datos.
- `CustomUserDetails` para adaptar los usuarios del sistema al modelo de Spring Security.
- `BCryptPasswordEncoder` para proteger las contraseñas antes de almacenarlas.
- Reglas de autorización por endpoint dentro de `SecurityConfig`.

### 8.3 Roles definidos

Se definen tres roles principales:

| Rol | Tipo de usuario | Descripción |
|---|---|---|
| `ROLE_CLIENTE` | Cliente | Usuario final que consulta productos, administra su carrito y realiza compras. |
| `ROLE_EMPLEADO` | Empleado | Usuario interno que gestiona productos, ventas e inventario. |
| `ROLE_GERENTE` | Gerente | Usuario administrativo con permisos para gestionar usuarios, roles y operaciones críticas. |

### 8.4 Matriz inicial de permisos

| Recurso / Endpoint | Cliente | Empleado | Gerente |
|---|---:|---:|---:|
| `/public/**` | Permitido | Permitido | Permitido |
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

La matriz de permisos busca separar las responsabilidades de cada tipo de usuario. El cliente solo debe acceder a funciones relacionadas con la consulta de productos, carrito y creación de compras. El empleado requiere permisos operativos para administrar productos, categorías, ventas e inventario, pero no debería tener control total sobre usuarios. El gerente posee el mayor nivel de acceso, por lo que puede administrar usuarios y realizar operaciones críticas del sistema.

Esta separación permite mitigar accesos no autorizados y reducir el impacto de una cuenta comprometida, ya que cada usuario queda limitado a las funciones propias de su rol.

## 9. Conclusión inicial

El backend cuenta con una base adecuada para implementar seguridad mediante Spring Security, pero requiere completar la estrategia de autenticación y autorización. Dado que el requerimiento de la asignatura solicita una implementación inicial con nombre de usuario y contraseña, la estrategia más adecuada será utilizar Spring Security con usuarios almacenados en base de datos, contraseñas protegidas mediante BCrypt y autorización por roles.