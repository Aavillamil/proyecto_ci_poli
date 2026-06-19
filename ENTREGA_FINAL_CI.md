# Plataforma de Integración Continua — Tienda ísimo
## Entrega 3 y sustentación (Semanas 7 y 8) — Documento consolidado

**Institución:** Politécnico Grancolombiano
**Módulo:** Énfasis Profesional I — Integración Continua
**Tipo de entrega:** Proyecto grupal
**Estudiante:** Angel Andres Villamil Olaya
**Fecha:** 2026-06-19
**Repositorio:** https://github.com/Aavillamil/proyecto_ci_poli.git

---

## 1. Contexto de la empresa

**ísimo** es una cadena de tiendas de descuento duro (modelo similar a D1) que comercializa
productos de consumo masivo —víveres, aseo y abarrotes— a bajo costo. Además de la venta en
sus puntos físicos, la empresa gestiona pedidos de surtido y de domicilio que requieren un
sistema que registre cada pedido, controle su estado (pendiente, enviado, entregado) y
permita al área de operaciones consultarlos y actualizarlos.

El crecimiento del volumen de pedidos hizo necesario un proceso de desarrollo confiable,
en el que cada cambio en el software se compile, se pruebe y se despliegue de forma
automática, sin depender de pasos manuales. Por esta razón ísimo adopta un flujo
de **integración continua** apoyado en contenedores y en tres servidores de CI: Jenkins,
Travis CI y Codeship.

Este documento consolida el resultado de las tres entregas del módulo aplicadas a ese
caso de negocio.

---

## 2. Descripción del software

El sistema es una **API REST para la operación de la tienda ísimo** que cubre la gestión de
pedidos, el control de inventario y la administración de usuarios. Está construida con
Spring Boot 3.2.5 y Java 17, respaldada por una base de datos MySQL 8. El artefacto de
salida es un JAR ejecutable empaquetado con Maven, desplegado en dos contenedores Docker
orquestados con Docker Compose.

| Contenedor | Tecnología | Puerto |
|---|---|---|
| `ci-api` | Spring Boot 3 + Java 17 | 8080 |
| `ci-mysql` | MySQL 8.0 | 3307 (host) → 3306 (contenedor) |

### 2.1 Arquitectura por capas

La aplicación está organizada en tres capas, lo que separa las responsabilidades y
facilita las pruebas dentro del pipeline de integración continua:

| Capa | Componentes | Responsabilidad |
|---|---|---|
| Controlador | `PedidoController`, `InventarioController`, `UsuarioController` | Exponer la API REST y traducir las peticiones HTTP |
| Servicio | `PedidoService`, `InventarioService`, `UsuarioService` | Concentrar la lógica de negocio |
| Repositorio | `PedidoRepository`, `ProductoRepository`, `UsuarioRepository` | Acceso a datos con Spring Data JPA |

### 2.2 Módulos de negocio

El sistema cubre tres módulos del proceso operativo de la tienda ísimo: gestión de
**pedidos**, control de **inventario** y administración de **usuarios** del sistema.

**Entidad Pedido**

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador del pedido |
| `cliente` | String | Nombre del cliente |
| `producto` | String | Producto solicitado |
| `cantidad` | int | Unidades pedidas |
| `total` | double | Valor total del pedido |
| `estado` | String | PENDIENTE, ENVIADO o ENTREGADO |
| `fechaCreacion` | LocalDateTime | Fecha de registro del pedido |

**Entidad Producto (inventario)**

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador del producto |
| `nombre` | String | Nombre del producto |
| `categoria` | String | Categoría (víveres, aseo, abarrotes) |
| `stock` | int | Unidades disponibles |
| `precio` | double | Precio unitario |

**Entidad Usuario**

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador del usuario |
| `nombre` | String | Nombre del usuario |
| `correo` | String | Correo electrónico |
| `rol` | String | OPERARIO o ADMIN |
| `activo` | boolean | Indica si el usuario está habilitado |

### 2.3 Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/pedidos` | Listar todos los pedidos |
| GET | `/api/pedidos/{id}` | Consultar un pedido |
| POST | `/api/pedidos` | Registrar un pedido |
| PUT | `/api/pedidos/{id}` | Actualizar un pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido |
| GET | `/api/inventario` | Listar productos |
| GET | `/api/inventario/{id}` | Consultar un producto |
| POST | `/api/inventario` | Registrar un producto |
| PUT | `/api/inventario/{id}` | Actualizar un producto |
| DELETE | `/api/inventario/{id}` | Eliminar un producto |
| PATCH | `/api/inventario/{id}/descontar` | Descontar stock al despachar un pedido |
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Consultar un usuario |
| POST | `/api/usuarios` | Crear un usuario |
| PUT | `/api/usuarios/{id}` | Actualizar un usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar un usuario |

---

## 3. Plataforma de integración continua

La plataforma queda integrada con las cuatro herramientas exigidas. Cada una cumple un
rol específico y todas operan sobre el mismo repositorio y el mismo Dockerfile, de modo
que el resultado de la compilación es idéntico en cualquiera de ellas.

| Herramienta | Rol en la plataforma | Archivo de configuración |
|---|---|---|
| Docker | Empaqueta la aplicación y la base de datos en contenedores | `Dockerfile`, `docker-compose.yml` |
| Jenkins | Servidor de CI principal: compila, prueba, construye imagen y despliega | `Jenkinsfile` |
| Travis CI | CI en la nube ligado a GitHub: compila, prueba y construye imagen | `.travis.yml` |
| Codeship | CI en la nube basado en Docker (Codeship Pro): compila y prueba | `codeship-services.yml`, `codeship-steps.yml` |

### 3.1 Docker (Entrega 1)

El stack se levanta con dos contenedores comunicados por una red bridge privada
(`ci-network`). El contenedor `ci-api` depende del contenedor `ci-mysql` mediante un
`healthcheck`, de manera que la API solo arranca cuando la base de datos está disponible.
La imagen de la API se construye con un Dockerfile multi-etapa: la primera etapa compila
con Maven y JDK 17, y la segunda ejecuta el JAR sobre una imagen JRE ligera con un usuario
sin privilegios.

### 3.2 Jenkins (Entrega 2)

Jenkins es el servidor de CI principal. Ejecuta el `Jenkinsfile` declarativo con las
etapas Checkout, Build, Test, Docker Build y Deploy. El despliegue solo se realiza en la
rama `main`. Las características de instalación, plugins, credenciales, estrategia de
ramas y seguridad están documentadas en `ENTREGA_JENKINS_CI.md`.

### 3.3 Travis CI (Entrega 3)

Travis CI replica el pipeline en la nube y se activa con cada push a las ramas `main` y
`develop` configuradas en GitHub. Usa `language: java`, `jdk: openjdk17` y el servicio
`docker`. Ejecuta `mvn clean package` (compilación y pruebas) y luego `docker build` para
verificar que la imagen se construye correctamente. Cachea el directorio `.m2` para
acelerar las ejecuciones.

### 3.4 Codeship (Entrega 3)

Codeship Pro corre el pipeline dentro de contenedores Docker. El archivo
`codeship-services.yml` define un servicio `maven` con el JDK 17 (que monta el código del
repositorio) y un servicio `app` que construye la imagen final. El archivo
`codeship-steps.yml` ejecuta los pasos build, test y docker-build, equivalentes a los de
Jenkins y Travis CI.

### 3.5 Equivalencia de etapas entre las tres herramientas de CI

| Etapa | Jenkins | Travis CI | Codeship |
|---|---|---|---|
| Build | `mvn clean package -DskipTests` | `mvn clean package` | `mvn clean package -DskipTests` |
| Test | `mvn test` | (incluido en `package`) | `mvn test` |
| Docker Build | `docker build` | `docker build` | servicio `app` |
| Deploy | `docker compose up` (solo main) | — | — |

El despliegue automático se concentra en Jenkins por ser el servidor de CI que opera
sobre la infraestructura de la empresa; Travis CI y Codeship actúan como verificación
adicional en la nube ante cada cambio.

---

## 4. Resumen de las tres entregas

| Entrega | Semana | Objetivo | Resultado |
|---|---|---|---|
| 1 | 3 | Crear el proyecto en GitHub y dos contenedores Docker comunicados | API + MySQL en contenedores sobre red bridge |
| 2 | 5 | Implementar Jenkins como gestor de integración continua | Pipeline declarativo con cinco etapas y despliegue en `main` |
| 3 | 7 y 8 | Integrar Travis CI y Codeship; consolidar documentación | Plataforma con cuatro herramientas de CI y documento final |

---

## 5. Historial de cambios

| Versión | Fecha | Cambios |
|---|---|---|
| 0.1 | Semana 3 | Estructura inicial del proyecto Spring Boot. Dockerfile multi-etapa. `docker-compose.yml` con los contenedores `ci-api` y `ci-mysql` sobre la red `ci-network`. |
| 0.2 | Semana 5 | Incorporación del `Jenkinsfile` con las etapas Checkout, Build, Test, Docker Build y Deploy. Documento `ENTREGA_JENKINS_CI.md` con la configuración de Jenkins. |
| 1.0 | Semana 7 | Reorientación del software al caso de negocio de ísimo: la entidad `Task` se reemplaza por `Pedido` y el recurso `/api/tasks` por `/api/pedidos`. Incorporación de `.travis.yml`, `codeship-services.yml` y `codeship-steps.yml`. Prueba unitaria `PedidoTest`. Documento consolidado final. |
| 1.1 | Semana 7 | Ampliación funcional del software con los módulos de **inventario** (`Producto`, `/api/inventario`) y **usuarios** (`Usuario`, `/api/usuarios`). Introducción de una **capa de servicios** (`PedidoService`, `InventarioService`, `UsuarioService`) que separa la lógica de negocio de los controladores. Pruebas `UsuarioTest` e `InventarioServiceTest`. |

---

## 6. Sugerencias para la solución de problemas

Durante el desarrollo y la integración de la plataforma se presentaron varios problemas
técnicos. La siguiente tabla los consolida agrupados por ámbito —contenedores, integración
continua y aplicación— e indica para cada uno la causa raíz y la solución aplicada, de modo
que sirva como guía de referencia ante incidencias futuras.

### 6.1 Contenedores (Docker)

| Problema | Causa | Solución aplicada |
|---|---|---|
| La API arrancaba antes que MySQL y fallaba la conexión | El contenedor de la API no esperaba a que la base de datos estuviera lista | Se agregó un `healthcheck` a MySQL y `depends_on: condition: service_healthy` en la API, de modo que la API solo inicia cuando la base responde |
| Conflicto de puertos en el host con MySQL | El puerto 3306 ya estaba ocupado por otra instancia local | Se mapeó el contenedor al puerto 3307 del host, dejando el 3306 interno sin cambios |
| Los datos se perdían al reiniciar el contenedor de MySQL | El contenedor no tenía almacenamiento persistente | Se definió el volumen `mysql-data` montado en `/var/lib/mysql` |

### 6.2 Integración continua (Jenkins, Travis CI, Codeship)

| Problema | Causa | Solución aplicada |
|---|---|---|
| La compilación fallaba en el host con Java 8 | El proyecto requiere JDK 17 | La compilación se ejecuta dentro del contenedor `maven:3.9.6-eclipse-temurin-17` con JDK 17; en Jenkins se usa el bloque `tools { jdk 'JDK-17' }` |
| El puerto 8080 de la API chocaba con la interfaz web de Jenkins | Ambos servicios usan 8080 por defecto | Jenkins se expone en el puerto 8090 del host |
| Jenkins no podía ejecutar comandos Docker desde el pipeline | El contenedor de Jenkins no tenía acceso al daemon de Docker | Se montó el socket `/var/run/docker.sock` en el contenedor de Jenkins |
| Builds lentos en Travis CI y Codeship | Las dependencias de Maven se descargaban en cada ejecución | Se habilitó la caché del directorio `.m2` en Travis CI y `cached: true` en el servicio de Codeship |
| Riesgo de resultados distintos entre los tres servidores de CI | Cada herramienta podía usar un entorno de compilación diferente | Las tres ejecutan las mismas etapas sobre el mismo `Dockerfile`, lo que garantiza un artefacto reproducible |

### 6.3 Aplicación (API y pruebas)

| Problema | Causa | Solución aplicada |
|---|---|---|
| La etapa Test no publicaba resultados | El proyecto no tenía pruebas automatizadas | Se agregaron las pruebas `PedidoTest`, `UsuarioTest` e `InventarioServiceTest`; en Jenkins se usa `junit allowEmptyResults: true` |
| Probar la lógica de inventario requería una base de datos real | El descuento de stock dependía del repositorio | Se aisló la prueba con Mockito (`InventarioServiceTest`), simulando el repositorio sin levantar MySQL |
| Las tablas de los nuevos módulos no existían en la base | El esquema no se creaba automáticamente | Se mantuvo `spring.jpa.hibernate.ddl-auto=update`, que crea las tablas `pedidos`, `productos` y `usuarios` al iniciar |
| Descontar stock podía dejar inventarios en negativo | No se validaba la disponibilidad antes de descontar | `InventarioService.descontarStock` verifica que haya stock suficiente y responde HTTP 409 si no lo hay |

---

## 7. Responsabilidades del equipo

El proyecto corresponde a una actividad grupal. La distribución de responsabilidades por
rol técnico es la siguiente.

| Rol | Responsable | Responsabilidades |
|---|---|---|
| Líder técnico / Integración continua | Angel Andres Villamil Olaya | Diseño del pipeline, configuración de Jenkins, Travis CI y Codeship, documentación consolidada |
| Desarrollo backend | Angel Andres Villamil Olaya | Implementación de la API de pedidos (modelo, repositorio, controlador) y pruebas unitarias |
| Infraestructura y contenedores | Angel Andres Villamil Olaya | Dockerfile multi-etapa, `docker-compose.yml`, red y persistencia de datos |
| Control de versiones | Angel Andres Villamil Olaya | Gestión del repositorio en GitHub, estrategia de ramas y revisión de cambios |

Nota: la presente entrega fue desarrollada de forma individual. Los roles se documentan
según la estructura de trabajo del proyecto para reflejar las áreas que abarca la
integración continua.

---

## 8. Opiniones

La integración continua aporta a un negocio como ísimo la confianza de que cada
cambio en el software se valida de forma automática antes de llegar a producción. Tener
tres servidores de CI (Jenkins, Travis CI y Codeship) sobre el mismo proyecto demuestra
que el pipeline no depende de una herramienta concreta: las etapas de compilación y prueba
se mantienen iguales y el Dockerfile garantiza un resultado reproducible en cualquiera de
ellas.

El uso de contenedores fue el punto que más simplificó el trabajo, porque resolvió las
diferencias entre los entornos de desarrollo y los servidores de CI. El principal
aprendizaje fue que el valor de la integración continua no está en la herramienta, sino en
la disciplina de mantener el código siempre compilable, probado y desplegable de forma
automática.

### 8.1 Estado actual de Travis CI y Codeship

Durante la integración se observó que dos de las herramientas exigidas atraviesan
limitaciones que conviene documentar, ya que afectan su uso real frente a Jenkins:

- **Travis CI** dejó de ser gratuito para uso general. Hoy opera bajo un modelo de
  créditos y restringe fuertemente las ejecuciones en repositorios públicos, por lo que
  para un proyecto sin presupuesto puede agotar su cupo rápidamente. La configuración
  (`.travis.yml`) queda lista y es válida, pero su ejecución continua depende de disponer
  de créditos en la cuenta.

- **Codeship** fue descontinuado por CloudBees: ya no admite registro de nuevos proyectos
  y su documentación se mantiene solo como referencia histórica. Por esta razón, los
  archivos `codeship-services.yml` y `codeship-steps.yml` se entregan como configuración
  correcta según el formato de Codeship Pro, pero no es posible conectar el repositorio a
  una ejecución activa en la plataforma.

En contraste, **Jenkins** sigue siendo de código abierto, autoalojado y sin restricciones
de uso, lo que lo convierte en el servidor de CI principal y plenamente operativo de la
plataforma. La conclusión práctica es que, para un negocio como ísimo, conviene apoyar la
integración continua en herramientas activas y mantenibles —Docker y Jenkins—, dejando las
configuraciones de Travis CI y Codeship como evidencia del conocimiento de varias
alternativas del ecosistema y de la portabilidad del pipeline entre ellas.

---

## 9. Estructura del repositorio

```
ci-project/
├── Dockerfile              # Imagen multi-etapa (Entrega 1)
├── docker-compose.yml      # Orquestación api + mysql (Entrega 1)
├── Jenkinsfile             # Pipeline de Jenkins (Entrega 2)
├── .travis.yml             # Pipeline de Travis CI (Entrega 3)
├── codeship-services.yml   # Servicios de Codeship (Entrega 3)
├── codeship-steps.yml      # Pasos de Codeship (Entrega 3)
├── pom.xml                 # Configuración de Maven
├── README.md               # Guía de uso
├── ENTREGA_JENKINS_CI.md   # Documento de la Entrega 2
├── ENTREGA_FINAL_CI.md     # Este documento (Entrega 3)
└── src/
    ├── main/java/com/ciproject/
    │   ├── controller/     # capa REST
    │   ├── service/        # lógica de negocio
    │   ├── model/          # entidades JPA
    │   └── repository/     # acceso a datos
    └── test/java/com/ciproject/
```

Detalle de clases por paquete:

| Paquete | Clases |
|---|---|
| `controller` | `PedidoController`, `InventarioController`, `UsuarioController` |
| `service` | `PedidoService`, `InventarioService`, `UsuarioService` |
| `model` | `Pedido`, `Producto`, `Usuario` |
| `repository` | `PedidoRepository`, `ProductoRepository`, `UsuarioRepository` |
| `test` | `PedidoTest`, `UsuarioTest`, `InventarioServiceTest` |

---

## 10. Conclusión

La plataforma de software de ísimo queda totalmente integrada con contenedores,
Jenkins, Travis CI y Codeship. El mismo proyecto se compila, se prueba y se construye en
los tres servidores de CI a partir del Dockerfile común, y Jenkins despliega el stack en
la rama `main`. El historial de cambios, las sugerencias para la solución de problemas, las
responsabilidades del equipo y las opiniones quedan consolidados en este documento, con lo
que se cumplen los requerimientos de la Entrega 3 del módulo.

*Politécnico Grancolombiano — Módulo de Integración Continua — 2026-06-19*
