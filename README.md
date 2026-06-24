# Backend — Sistema Clínico HIS/LIS

Conjunto de microservicios Spring Boot que implementan el núcleo clínico y
administrativo del sistema. Cada servicio es independiente y se comunica
únicamente a través del api-gateway, Feign (HTTP síncrono) o RabbitMQ
(eventos asíncronos).

---

## Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9+ |
| Docker Desktop | Cualquier versión reciente |

> Los microservicios **no** levantan sus propias BDs. Antes de correr
> cualquier servicio, la infraestructura base debe estar `healthy`.
> Ver [`infraestructura/README.md`](../infraestructura/README.md).

---

## Estructura

```
backend/
├── config-server/          # Configuración centralizada (puerto 8888)
├── eureka-server/          # Service discovery (puerto 8761)
├── api-gateway/            # Punto de entrada + validación JWT (puerto 8080)
├── ms-personal/            # Personal médico y especialidades (8087)
├── ms-horarios/            # Consultorios y turnos maestros (8081)
├── ms-pacientes/           # Índice maestro de pacientes (8083)
├── ms-farmacia/            # Catálogo de medicamentos e inventario (8084)
├── ms-laboratorio/         # Catálogo de exámenes y autorizaciones (8085)
├── ms-historias-clinicas/  # EHR en MongoDB (8086)
├── ms-citas/               # Agendamiento y ciclo de vida de citas (8082)
├── ms-atencion-medica/     # Borrador de atención en Redis (8089)
├── ms-caja/                # Cobros, proformas y comprobantes (8088)
└── ms-notificaciones/      # Envío de correos vía RabbitMQ (8090)
```

---

## Cómo correr un microservicio en local

### 1. Levantar la infraestructura base

```bash
cd infraestructura
docker compose up -d
# Esperar a que todos los servicios estén healthy
docker compose ps
```

Esto levanta PostgreSQL, MongoDB, Redis, RabbitMQ y Keycloak.
El realm de Keycloak se importa automáticamente.

### 2. Correr el microservicio desde el IDE

Abre el módulo deseado en IntelliJ o VS Code y ejecuta la clase principal
`*Application.java`. No se requiere ningún perfil especial — la configuración
se obtiene automáticamente del config-server en `localhost:8888`.

### 2b. O correr con Maven desde la terminal

```bash
cd backend/ms-personal
mvn spring-boot:run
```

### 3. Verificar que se registró en Eureka

Abrir **http://localhost:8761** — el servicio debe aparecer como `UP`.

### 4. Probar vía api-gateway

Todas las rutas pasan por `http://localhost:8080/api/{servicio}/...`
con el header `Authorization: Bearer <token>`.

Para obtener un token de prueba ver la sección **Keycloak** en
[`infraestructura/README.md`](../infraestructura/README.md#configuración-de-keycloak-una-sola-vez).

---

## Rutas del gateway

| Prefijo | Microservicio | Puerto directo |
|---|---|---|
| `/api/personal/**` | ms-personal | 8087 |
| `/api/horarios/**` | ms-horarios | 8081 |
| `/api/pacientes/**` | ms-pacientes | 8083 |
| `/api/farmacia/**` | ms-farmacia | 8084 |
| `/api/laboratorio/**` | ms-laboratorio | 8085 |
| `/api/historias/**` | ms-historias-clinicas | 8086 |
| `/api/citas/**` | ms-citas | 8082 |
| `/api/atenciones/**` | ms-atencion-medica | 8089 |
| `/api/caja/**` | ms-caja | 8088 |

> El gateway aplica `StripPrefix=2`, por lo que `/api/personal/personal/todos`
> llega al microservicio como `GET /personal/todos`.

---

## Documentación Swagger

Cada microservicio expone su Swagger UI en `http://localhost:{puerto}/swagger-ui.html`.

| Servicio | URL |
|---|---|
| ms-personal | http://localhost:8087/swagger-ui.html |
| ms-horarios | http://localhost:8081/swagger-ui.html |
| ms-pacientes | http://localhost:8083/swagger-ui.html |
| ms-farmacia | http://localhost:8084/swagger-ui.html |
| ms-laboratorio | http://localhost:8085/swagger-ui.html |
| ms-historias-clinicas | http://localhost:8086/swagger-ui.html |
| ms-citas | http://localhost:8082/swagger-ui.html |
| ms-atencion-medica | http://localhost:8089/swagger-ui.html |
| ms-caja | http://localhost:8088/swagger-ui.html |
| ms-notificaciones | http://localhost:8090/swagger-ui.html |

> El swagger solo es accesible **directamente al microservicio** (puerto
> propio), no a través del gateway.

---

## Stack técnico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Service discovery | Spring Cloud Eureka 2025.1.1 |
| Config centralizada | Spring Cloud Config |
| Gateway | Spring Cloud Gateway (WebFlux) |
| ORM relacional | Spring Data JPA + Hibernate |
| BD documental | Spring Data MongoDB |
| Caché / sesión | Spring Data Redis |
| Mensajería | Spring AMQP (RabbitMQ) |
| HTTP entre servicios | Spring Cloud OpenFeign |
| Autenticación | Keycloak 23 (JWT / JWKS) |
| Documentación API | springdoc-openapi 2.8.9 |

---

## Reglas de arquitectura (resumen)

- **Database-per-service**: ningún microservicio accede a la BD de otro.
- **Sin precios fuera de ms-caja**: farmacia y laboratorio exponen `/precio`
  exclusivamente para ms-caja.
- **EHR inmutable**: los episodios clínicos nunca se actualizan; las
  correcciones se registran como `AdendaClinica`.
- **Sin efecto financiero hasta el pago**: recetar u ordenar un examen no
  descuenta stock ni autoriza nada; el efecto ocurre solo cuando ms-caja
  confirma el pago.
- **Sagas orquestadas**: el pago de consulta (Saga 14.1) y el pago de
  proforma (Saga 14.2) tienen compensaciones explícitas.
  Ver `docs/transversales/sagas.md`.
