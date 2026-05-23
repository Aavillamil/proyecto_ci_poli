# CI Project — Integración Continua

Proyecto de software basado en herramientas de integración continua.  
**Módulo:** Énfasis Profesional I (Integración Continua)  
**Institución:** Politécnico Grancolombiano

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

## Endpoints disponibles

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/tasks` | Listar todas las tareas |
| GET | `/api/tasks/{id}` | Obtener tarea por ID |
| POST | `/api/tasks` | Crear nueva tarea |
| PUT | `/api/tasks/{id}` | Actualizar tarea |
| DELETE | `/api/tasks/{id}` | Eliminar tarea |

### Ejemplo de uso con curl

```bash
# Crear una tarea
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Configurar Jenkins", "description": "Pipeline para CI/CD"}'

# Listar tareas
curl http://localhost:8080/api/tasks
```

## Detener el proyecto

```bash
docker compose down

# Para eliminar también los datos persistidos:
docker compose down -v
```
