# Implementación de Integración Continua con Jenkins
## Proyecto ci-project — Documento de entrega

**Institución:** Politécnico Grancolombiano
**Módulo:** Énfasis Profesional I — Integración Continua
**Estudiante:** Angel Andres Villamil Olaya

**Fecha:** 2026-06-05

---

## 1. Objetivo

Definir las características requeridas para implementar Jenkins como gestor de
integración continua del proyecto `ci-project` y documentar la validación del
pipeline ejecutada sobre la infraestructura del proyecto. El alcance cubre las
fases de compilación, pruebas, construcción de imagen Docker y despliegue del
stack.

---

## 2. Descripción del proyecto

`ci-project` es una API REST construida con Spring Boot 3.2.5 y Java 17,
respaldada por MySQL 8. El artefacto de salida es un JAR ejecutable empaquetado
con Maven. El stack se ejecuta en dos contenedores Docker orquestados con Docker
Compose.

| Contenedor | Tecnología | Puerto |
|---|---|---|
| `ci-api` | Spring Boot 3 + Java 17 | 8080 |
| `ci-mysql` | MySQL 8.0 | 3307 (host) → 3306 (contenedor) |

La API expone un CRUD de tareas en `/api/tasks`. La construcción de la imagen se
realiza con un Dockerfile multi-etapa: la primera etapa compila el proyecto con
`maven:3.9.6-eclipse-temurin-17-alpine` y la segunda empaqueta el JAR sobre
`eclipse-temurin:17-jre-alpine` ejecutando con un usuario sin privilegios.

El código fuente del proyecto se encuentra en el repositorio:
https://github.com/Aavillamil/proyecto_ci_poli.git

---

## 3. Requisitos de hardware

| Recurso | Mínimo | Recomendado |
|---|---|---|
| CPU | 2 núcleos | 4 núcleos |
| RAM | 2 GB | 4 GB |
| Disco | 20 GB libres | 50 GB libres |

Corresponden al host donde se ejecuta Jenkins. Si Jenkins se ejecuta como
contenedor Docker, el host debe cumplir estos valores.

---

## 4. Requisitos de software

| Software | Versión mínima | Función |
|---|---|---|
| Sistema operativo | Ubuntu 22.04 LTS | Host de Jenkins |
| Java JDK | 17 | Requerido por Jenkins LTS y por el proyecto |
| Docker | 20.10 | Construcción y ejecución de contenedores |
| Docker Compose | 2.0 | Orquestación del stack (api + mysql) |
| Git | 2.x | Control de versiones e integración con el pipeline |
| Maven | 3.9.6 | Compilación y empaquetado (puede instalarlo Jenkins) |
| Jenkins | LTS 2.504 | Servidor de CI/CD |

---

## 5. Instalación de Jenkins

Instalación recomendada mediante el contenedor oficial de Jenkins sobre Docker,
coherente con la infraestructura del proyecto.

### 5.1 Preparar recursos Docker

```bash
docker network create jenkins-net
docker volume create jenkins-data
```

### 5.2 Levantar el contenedor

```bash
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  -p 8090:8080 \
  -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17
```

El socket `/var/run/docker.sock` se monta para que Jenkins ejecute comandos
Docker desde el pipeline sin Docker-in-Docker. El puerto 8090 evita el conflicto
con la API del proyecto, que ocupa el 8080.

### 5.3 Contraseña inicial

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Acceder a `http://localhost:8090`, ingresar la contraseña obtenida y completar
el asistente de configuración inicial.

---

## 6. Plugins requeridos

Desde **Manage Jenkins > Plugins > Available plugins**:

| Plugin | Versión | Función |
|---|---|---|
| Git | >= 5.x | Clonar repositorios Git |
| Pipeline | >= 2.x | Soporte para Jenkinsfile declarativo |
| Maven Integration | >= 3.x | Construcción de proyectos Maven |
| Docker Pipeline | >= 1.x | Comandos Docker dentro del pipeline |
| JUnit | >= 1.x | Publicación de resultados de pruebas |
| Blue Ocean | >= 1.x | Visualización de pipelines |
| Workspace Cleanup | >= 0.x | Limpiar workspace antes de cada build |
| Email Extension | >= 2.x | Notificaciones por correo |

---

## 7. Configuración de herramientas globales

En **Manage Jenkins > Tools**:

- **JDK:** nombre `JDK-17`, versión `17`, instalación automática desde Adoptium.
- **Maven:** nombre `Maven-3.9.6`, versión `3.9.6`, instalación automática desde Apache.

Los nombres `JDK-17` y `Maven-3.9.6` deben coincidir exactamente con los
declarados en el bloque `tools` del Jenkinsfile.

---

## 8. Diseño del pipeline

### 8.1 Etapas

```
Checkout  >  Build  >  Test  >  Docker Build  >  Deploy  >  Notificación
```

| Etapa | Comando | Condición |
|---|---|---|
| Checkout | `cleanWs` + `checkout scm` | Siempre |
| Build | `mvn clean package -DskipTests -B` | Siempre |
| Test | `mvn test -B` | Siempre |
| Docker Build | `docker build -t ci-project:1.0.0 .` | Siempre |
| Deploy | `docker compose up --build -d` | Solo rama `main` |
| Notificación | correo / log | Siempre (post) |

### 8.2 Jenkinsfile

Ubicado en la raíz del repositorio (`Jenkinsfile`):

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        jdk   'JDK-17'
    }

    environment {
        APP_NAME    = 'ci-project'
        APP_VERSION = '1.0.0'
        IMAGE_NAME  = "ci-project:${APP_VERSION}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker compose down --remove-orphans || true'
                sh 'docker compose up --build -d'
                sh 'docker compose ps'
            }
        }
    }

    post {
        failure {
            mail to: 'aanvillamilo@poligran.edu.co',
                 subject: "Build fallido: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Revisar la salida del pipeline en: ${env.BUILD_URL}"
        }
        always {
            cleanWs()
        }
    }
}
```

La opción `junit allowEmptyResults: true` evita que la etapa Test falle mientras
el proyecto no incluya pruebas automatizadas.

---

## 9. Configuración del job en Jenkins

1. **New Item** > nombre `ci-project-pipeline` > tipo **Pipeline** > OK.
2. En **Build Triggers**, activar **Poll SCM** con la expresión `H/5 * * * *`
   para revisar el repositorio cada cinco minutos. Si el repositorio está en
   GitHub, activar además **GitHub hook trigger for GITScm polling** y
   configurar el webhook en el repositorio remoto.
3. En la sección **Pipeline**:

| Campo | Valor |
|---|---|
| Definition | Pipeline script from SCM |
| SCM | Git |
| Repository URL | `https://github.com/Aavillamil/proyecto_ci_poli.git` |
| Branch | `*/main` |
| Script Path | `Jenkinsfile` |

---

## 10. Gestión de credenciales

Las credenciales se almacenan en **Manage Jenkins > Credentials**, nunca en el
código fuente:

| ID | Tipo | Uso |
|---|---|---|
| `git-credentials` | Username/Password o SSH Key | Autenticación al repositorio |
| `mysql-app-password` | Secret text | Contraseña de la base de datos |

Referencia en el Jenkinsfile:

```groovy
environment {
    DB_PASS = credentials('mysql-app-password')
}
```

---

## 11. Estrategia de ramas

| Rama | Etapas ejecutadas | Despliegue |
|---|---|---|
| `main` | Checkout, Build, Test, Docker Build, Deploy | Sí |
| `develop` | Checkout, Build, Test, Docker Build | No |
| `feature/*` | Checkout, Build, Test | No |

---

## 12. Permisos de Docker

El usuario que ejecuta Jenkins necesita acceso al daemon de Docker. Si Jenkins
corre como proceso en el host:

```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

Si Jenkins corre como contenedor, el socket queda disponible con el montaje
indicado en la sección 5.2.

---

## 13. Seguridad

- Habilitar autenticación en **Manage Jenkins > Security > Security Realm**
  (base de datos interna de Jenkins o LDAP).
- Configurar matriz de autorización por roles en **Authorization**.
- No exponer el puerto 8090 directamente a internet; usar un proxy inverso
  (Nginx) con TLS si el servidor es accesible desde fuera de la red local.
- Rotar periódicamente las credenciales almacenadas en Jenkins.

---

## 14. Validación del pipeline

Para verificar la implementación antes de la entrega se ejecutaron localmente
las mismas etapas definidas en el Jenkinsfile, sobre la infraestructura Docker
del proyecto.

### 14.1 Entorno de la prueba

| Componente | Valor |
|---|---|
| Docker | 25.0.3 |
| Docker Compose | v2.24.6 |
| Maven | 3.9.2 (host) |
| Java (host) | 1.8 |

Nota: el host empleado en la prueba tiene Java 8, por lo que las etapas Build y
Test no se ejecutan directamente sobre el host. La compilación se realiza dentro
del contenedor `maven:3.9.6-eclipse-temurin-17-alpine` definido en el
Dockerfile, que aporta el JDK 17 requerido. En Jenkins esta limitación no
aplica, ya que el bloque `tools { jdk 'JDK-17' }` instala el JDK correcto.

### 14.2 Resultados por etapa

| Etapa | Comando ejecutado | Resultado |
|---|---|---|
| Docker Build | `docker build -t ci-project:1.0.0 .` | Imagen `ci-project:1.0.0` construida (231 MB) |
| Deploy | `docker compose up --build -d` | `ci-mysql` saludable y `ci-api` iniciada en el puerto 8080 |
| Verificación | `docker compose ps` | Ambos contenedores en estado activo |

Arranque de la aplicación registrado en los logs del contenedor:

```
Tomcat started on port 8080 (http) with context path ''
Started CiProjectApplication in 5.639 seconds
```

### 14.3 Prueba de humo de la API

Se validó el CRUD de tareas contra `http://localhost:8080/api/tasks`:

| Petición | Resultado |
|---|---|
| `GET /api/tasks` | HTTP 200 — devuelve las tareas existentes |
| `POST /api/tasks` | HTTP 201 — crea la tarea y retorna el recurso con `id` |
| `GET /api/tasks` | HTTP 200 — la nueva tarea aparece persistida en MySQL |

Ejemplo de respuesta del `POST`:

```json
{
  "id": 2,
  "title": "Probar CI con Jenkins",
  "description": "Validacion del pipeline",
  "completed": false,
  "createdAt": "2026-06-06T00:18:40"
}
```

La persistencia en MySQL se confirmó al recuperar la tarea creada en una
consulta posterior, lo que valida la conexión entre el contenedor de la API y el
de la base de datos.

---

## 15. Archivos del proyecto

| Archivo | Estado | Descripción |
|---|---|---|
| `Jenkinsfile` | Nuevo | Definición del pipeline |
| `ENTREGA_JENKINS_CI.md` | Nuevo | Este documento de entrega |
| `Dockerfile` | Existente | Sin cambios |
| `docker-compose.yml` | Existente | Sin cambios |
| `pom.xml` | Existente | Sin cambios |
| `src/main/resources/application.properties` | Modificado | Corrección de comentario |

---

## 16. Recomendaciones

- Incorporar al menos una prueba unitaria en `src/test` para que la etapa Test
  ejecute pruebas reales y publique resultados JUnit.
- Definir las ramas `develop` y `feature/*` según la estrategia de la sección 11
  para aprovechar la ejecución condicional del pipeline.
- Configurar el webhook de GitHub para disparar builds automáticos en cada push,
  como complemento al Poll SCM.

---

## 17. Conclusión

La implementación de Jenkins queda definida en cuanto a hardware, software,
plugins, herramientas globales, diseño del pipeline, credenciales, estrategia de
ramas y seguridad. El pipeline fue validado de extremo a extremo sobre la
infraestructura Docker del proyecto: construcción de la imagen, despliegue del
stack y prueba funcional de la API con persistencia en MySQL. El proyecto queda
listo para que Jenkins ejecute el Jenkinsfile incluido en la raíz del
repositorio.

---

*Politécnico Grancolombiano — Módulo de Integración Continua — 2026-06-05*
