# Tienda ísimo — API de Pedidos

Software de gestión de pedidos para la cadena de tiendas de descuento **ísimo** (modelo similar a D1), construido como caso de negocio para el módulo de integración continua.  
**Módulo:** Énfasis Profesional I (Integración Continua)  
**Institución:** Politécnico Grancolombiano

La plataforma está integrada con cuatro herramientas de integración continua: **Docker** (contenedores), **Jenkins**, **Travis CI** y **Codeship**.

## Arquitectura

El proyecto consta de dos contenedores Docker comunicados mediante una red bridge personalizada:

| Contenedor | Tecnología | Puerto |
|---|---|---|
| `ci-api` | Spring Boot 3 + Java 17 | 8080 |
| `ci-mysql` | MySQL 8.0 | 3307 (host) → 3306 (container) |

## Requisitos previos

- Docker >= 20.10
- Docker Compose >= 2.0
- Git

## Levantar el proyecto

```bash
# Clonar el repositorio
git clone {repositorio}
cd ci-project

# Construir y levantar los contenedores
docker compose up --build -d

# Verificar que ambos contenedores están corriendo
docker compose ps
```

## Arquitectura por capas

Cada módulo de negocio sigue la separación **controlador → servicio → repositorio**:
el controlador expone la API REST, el servicio concentra la lógica de negocio y el
repositorio gestiona el acceso a datos con Spring Data JPA.

## Módulos y endpoints

### Pedidos (`/api/pedidos`)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/pedidos` | Listar todos los pedidos |
| GET | `/api/pedidos/{id}` | Obtener pedido por ID |
| POST | `/api/pedidos` | Crear nuevo pedido |
| PUT | `/api/pedidos/{id}` | Actualizar pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar pedido |

### Inventario (`/api/inventario`)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/inventario` | Listar productos |
| GET | `/api/inventario/{id}` | Obtener producto por ID |
| POST | `/api/inventario` | Registrar producto |
| PUT | `/api/inventario/{id}` | Actualizar producto |
| DELETE | `/api/inventario/{id}` | Eliminar producto |
| PATCH | `/api/inventario/{id}/descontar?cantidad=N` | Descontar stock al despachar |

### Usuarios (`/api/usuarios`)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID |
| POST | `/api/usuarios` | Crear usuario |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |

### Ejemplo de uso con curl

```bash
# Crear un pedido
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cliente": "Carlos Gomez", "producto": "Arroz 500g", "cantidad": 12, "total": 30000}'

# Listar pedidos
curl http://localhost:8080/api/pedidos
```

## Detener el proyecto

```bash
docker compose down

# Para eliminar también los datos persistidos:
docker compose down -v
```
