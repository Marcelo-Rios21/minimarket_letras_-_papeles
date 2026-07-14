# MiniMarket Plus - Backend Spring Boot

Backend desarrollado en Spring Boot para el sistema MiniMarket Plus, orientado a la gestion de productos, inventario, carritos de compra, ventas y usuarios con autenticacion JWT, autorizacion por roles y documentacion tecnica mediante OpenAPI.

## 1. Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Spring HATEOAS
- Jakarta Bean Validation
- H2 Database
- Maven
- JUnit 5
- Mockito
- MockMvc
- JaCoCo
- springdoc-openapi
- Swagger UI

## 2. Estructura principal del proyecto

src/main/java/com/minimarket

- config
- controller
- dto
- entity
- exception
- hateoas
- repository
- security
- service

src/test/java/com/minimarket

- controller
- entity
- security
- service

docs

- analisis-seguridad.md
- pruebas-seguridad.md
- testing-unitario.md
- openapi.json

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

## 6. Documentacion OpenAPI y Swagger UI

El backend utiliza springdoc-openapi para generar documentacion tecnica de los endpoints REST.

Con la aplicacion ejecutandose, se puede acceder a la interfaz interactiva de Swagger UI en:

    http://localhost:8080/swagger-ui/index.html

La especificacion OpenAPI en formato JSON se encuentra disponible en:

    http://localhost:8080/v3/api-docs

Ademas, la especificacion exportada se mantiene en el proyecto en la siguiente ruta:

    docs/openapi.json

La documentacion OpenAPI incluye descripciones de operaciones, parametros, codigos de respuesta y ejemplos de cuerpos JSON para los controladores documentados.

Las respuestas de los recursos incorporan Spring HATEOAS:

- EntityModel para recursos individuales.
- CollectionModel para listados.
- Enlaces dinamicos generados mediante linkTo(methodOn(...)).
- Propiedades _links y _embedded para navegar entre recursos.

Controladores documentados:

- AuthController
- ProductoController
- CategoriaController
- CarritoController
- UsuarioController
- InventarioController
- VentaController
- DetalleVentaController

Endpoints representativos documentados:

| Recurso | Metodo | Ruta | Descripcion |
|---|---|---|---|
| Productos | GET | /api/productos | Lista los productos registrados |
| Productos | GET | /api/productos/{id} | Obtiene un producto por ID |
| Productos | POST | /api/productos | Crea un nuevo producto |
| Productos | PUT | /api/productos/{id} | Actualiza un producto existente |
| Productos | DELETE | /api/productos/{id} | Elimina un producto existente |
| Carrito | GET | /api/carrito | Lista los registros del carrito |
| Carrito | GET | /api/carrito/{id} | Obtiene un item del carrito por ID |
| Carrito | POST | /api/carrito | Agrega un item al carrito |
| Carrito | PUT | /api/carrito/{id} | Actualiza un item del carrito |
| Carrito | DELETE | /api/carrito/{id} | Elimina un item del carrito |

## 7. Ejecucion del proyecto

Para iniciar la aplicacion en entorno local:

    .\mvnw.cmd spring-boot:run

La aplicacion queda disponible en:

    http://localhost:8080

Con la aplicacion iniciada, se puede validar Swagger UI desde el navegador:

    http://localhost:8080/swagger-ui/index.html

## 8. Matriz resumida de permisos

| Recurso | Cliente | Cajero / Empleado | Administrador / Gerente |
|---|---:|---:|---:|
| Consultar productos | Si | Si | Si |
| Crear productos | No | No | Si |
| Editar productos | No | No | Si |
| Eliminar productos | No | No | Si |
| Registrar inventario | No | Si | Si |
| Generar ventas | No | Si | No |
| Gestionar usuarios | No | No | Si |
| Consultar documentacion Swagger/OpenAPI | Si | Si | Si |

## 9. Pruebas implementadas

El proyecto cuenta con pruebas unitarias y pruebas de autorizacion.

Areas cubiertas:

- Entidades: Producto, Carrito e Inventario.
- Servicios: Carrito, Inventario, Usuario y Venta.
- Seguridad: autenticacion, autorizacion por rol y proteccion de endpoints.
- Controladores: validaciones, codigos HTTP y manejo global de errores.
- HATEOAS: enlaces _links y colecciones _embedded.

Pruebas de seguridad:

- AuthProductoAuthorizationTest
- InventarioAuthorizationTest
- VentaAuthorizationTest
- ProductoControllerValidationTest
- HateoasIntegrationTest

Casos validados:

- Login valido.
- Login invalido.
- Acceso sin token JWT.
- Acceso con rol insuficiente.
- Acceso con rol autorizado.
- Restriccion de modificacion de productos solo a administrador.
- Restriccion de inventario a cajero y administrador.
- Restriccion de generacion de ventas solo a cajero.

## 10. Ejecucion de pruebas

Para ejecutar todas las pruebas:

    .\mvnw.cmd test

Para validar compilacion sin ejecutar pruebas:

    .\mvnw.cmd -DskipTests compile

Para ejecutar solo las pruebas de autorizacion:

    .\mvnw.cmd "-Dtest=*AuthorizationTest" test

## 11. Reportes de pruebas

Los reportes de Surefire se generan en:

    target/surefire-reports

El reporte de cobertura JaCoCo se genera con:

    .\mvnw.cmd jacoco:report

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

## 12. Caracteristicas tecnicas del sistema

El backend cuenta con las siguientes caracteristicas tecnicas:

- API REST desarrollada con Spring Boot.
- Persistencia mediante Spring Data JPA.
- Base de datos H2 para ejecucion local y pruebas.
- Autenticacion stateless mediante JWT.
- Autorizacion por roles funcionales del minimarket.
- Proteccion de endpoints segun perfil de usuario.
- Validacion de datos mediante Jakarta Bean Validation y texto seguro.
- Manejo global y estandarizado de errores, autenticacion y acceso denegado.
- Pruebas unitarias y pruebas de autorizacion con MockMvc.
- Reporte de cobertura mediante JaCoCo.
- Respuestas HATEOAS con EntityModel, CollectionModel y enlaces dinamicos.
- Documentacion tecnica de endpoints mediante OpenAPI 3.1.
- Interfaz interactiva Swagger UI para consulta y validacion de contratos REST.
- Especificacion OpenAPI exportada en formato JSON.

## 13. Mejoras propuestas

Como mejoras futuras se proponen:

- Implementar DTOs para evitar exponer entidades directamente.
- Agregar control de propiedad para que un cliente solo acceda a sus propios carritos o ventas.
- Ampliar pruebas MockMvc para todos los controladores.
- Incorporar alertas de stock minimo en inventario.
- Reforzar trazabilidad de eventos sensibles como login fallido y accesos denegados.
- Incorporar esquemas DTO especificos para mejorar la claridad de los contratos publicados en Swagger UI.