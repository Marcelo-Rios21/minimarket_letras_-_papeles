# MiniMarket Plus - Backend Spring Boot

Backend desarrollado en Spring Boot para el sistema MiniMarket Plus, orientado a la gestion de productos, inventario, carritos de compra, ventas y usuarios con autenticacion y autorizacion por roles.

## 1. Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- H2 Database
- Maven
- JUnit 5
- Mockito
- MockMvc
- JaCoCo

## 2. Estructura principal del proyecto

src/main/java/com/minimarket
- config
- controller
- entity
- repository
- security
- service

src/test/java/com/minimarket
- entity
- security
- service

## 3. Roles del sistema

La pauta trabaja con los perfiles cliente, cajero y administrador. En el backend estos perfiles se representan tecnicamente de la siguiente forma:

| Perfil funcional | Rol tecnico |
|---|---|
| Cliente | ROLE_CLIENTE |
| Cajero | ROLE_EMPLEADO |
| Administrador | ROLE_GERENTE |

## 4. Usuarios de prueba

El sistema inicializa usuarios demo mediante DataInitializer:

| Usuario | Contrasena | Rol |
|---|---|---|
| cliente | cliente123 | ROLE_CLIENTE |
| empleado | empleado123 | ROLE_EMPLEADO |
| gerente | gerente123 | ROLE_GERENTE |

## 5. Autenticacion

La autenticacion se realiza mediante JWT.

Endpoint de login:

POST /api/auth/login

Ejemplo de body:

{
  "username": "empleado",
  "password": "empleado123"
}

El token recibido se debe enviar en las solicitudes protegidas:

Authorization: Bearer <token>

## 6. Matriz resumida de permisos

| Recurso | Cliente | Cajero / Empleado | Administrador / Gerente |
|---|---:|---:|---:|
| Consultar productos | Si | Si | Si |
| Crear productos | No | No | Si |
| Editar productos | No | No | Si |
| Eliminar productos | No | No | Si |
| Registrar inventario | No | Si | Si |
| Generar ventas | No | Si | No |
| Gestionar usuarios | No | No | Si |

## 7. Pruebas implementadas

El proyecto cuenta con pruebas unitarias y pruebas de autorizacion.

Areas cubiertas:

- Entidades: Producto, Carrito e Inventario.
- Servicios: Carrito, Inventario, Usuario y Venta.
- Seguridad: autenticacion, autorizacion por rol y proteccion de endpoints.

Pruebas de seguridad agregadas:

- AuthProductoAuthorizationTest
- InventarioAuthorizationTest
- VentaAuthorizationTest

Casos validados:

- Login valido.
- Login invalido.
- Acceso sin token JWT.
- Acceso con rol insuficiente.
- Acceso con rol autorizado.
- Restriccion de modificacion de productos solo a administrador.
- Restriccion de inventario a cajero y administrador.
- Restriccion de generacion de ventas solo a cajero.

## 8. Ejecucion de pruebas

Para ejecutar todas las pruebas:

mvn clean test

Resultado validado:

73 pruebas ejecutadas correctamente
BUILD SUCCESS

Para ejecutar solo las pruebas de autorizacion:

mvn clean "-Dtest=*AuthorizationTest" test

## 9. Reportes de pruebas

Los reportes de Surefire se generan en:

target/surefire-reports

El reporte de cobertura JaCoCo se genera con:

mvn jacoco:report

Ruta del reporte HTML:

target/site/jacoco/index.html

Resultado de cobertura observado:

| Metrica | Resultado |
|---|---:|
| Cobertura de instrucciones | 69% |
| Cobertura de ramas | 52% |
| Cobertura paquete security.config | 100% |
| Cobertura paquete security.handler | 100% |
| Cobertura paquete security.jwt | 93% |
| Cobertura paquete entity | 91% |

## 10. Ajustes tecnicos realizados

Durante la actividad se realizaron los siguientes ajustes:

1. Se ajustaron las reglas de seguridad por rol en SecurityConfig.
2. Se restringio la modificacion de productos al rol administrador.
3. Se restringio la generacion de ventas al rol cajero.
4. Se agregaron pruebas automaticas de autenticacion y autorizacion con MockMvc.
5. Se limpiaron clases vacias no utilizadas.
6. Se agrego manejo transaccional al registro de ventas para asegurar consistencia al descontar stock y registrar la venta.

## 11. Mejoras propuestas

Como mejoras futuras se proponen:

- Implementar DTOs para evitar exponer entidades directamente.
- Agregar control de propiedad para que un cliente solo acceda a sus propios carritos o ventas.
- Ampliar pruebas MockMvc para todos los controladores.
- Incorporar alertas de stock minimo en inventario.
- Centralizar respuestas de error mediante un manejador global.
- Reforzar trazabilidad de eventos sensibles como login fallido y accesos denegados.