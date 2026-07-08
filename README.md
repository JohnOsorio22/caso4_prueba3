# FastTrack Courier - Microservicio de Rastreo Logístico

## Descripción del Proyecto

**FastTrack Courier - Rastreo Logístico** es un microservicio responsable de la gestión y rastreo de paquetes en tránsito para la empresa logística FastTrack Courier. Este servicio implementa una **Caja Negra de Viaje** que registra cada evento de forma inalterable para auditoría completa.

### Contexto del Dominio
El microservicio resuelve la necesidad de:
- **Registro de paquetes**: Alta de nuevos paquetes con código de seguimiento único
- **Control de estado**: EN_RUTA → ENTREGANDO → ENTREGADO
- **Caja Negra de Viaje**: Auditoría inalterable de todos los eventos (INGRESO_RUTA, CAMBIO_ESTADO, EGRESO_SOFT_DELETE)
- **Proxy de Flota**: Consulta al microservicio de Gestión de Flota (puerto 16000)

---

## Equipo de Desarrollo

| Nombre | Rol |
|--------|-----|
| Sebastián Saavedra | Desarrollador Full Stack |
| [Nombre Compañero 1] | Desarrollador Backend |
| [Nombre Compañero 2] | Desarrollador Backend |

---

## Arquitectura del Microservicio
┌─────────────────────────────────────────────────────────────────┐
│ Rastreo Logístico (Puerto 28000) │
├─────────────────────────────────────────────────────────────────┤
│ │
│ ┌──────────────┐ ┌──────────────────┐ ┌──────────────┐ │
│ │ Controller │───▶│ Service │───▶│ Repository │ │
│ │ (REST API) │ │ (Lógica Negocio)│ │ (JPA/Hiber) │ │
│ └──────────────┘ └──────────────────┘ └──────────────┘ │
│ │ │ │ │
│ ▼ ▼ ▼ │
│ ┌──────────────┐ ┌──────────────────┐ ┌──────────────┐ │
│ │ DTO/Model │ │ FlotaService │ │ Oracle DB │ │
│ │ (Entidades) │ │ (Proxy REST) │ │(paquetes/audit)│ │
│ └──────────────┘ └──────────────────┘ └──────────────┘ │
│ │ │
│ ▼ │
│ ┌──────────────────┐ │
│ │ Gestión de Flota │ │
│ │ (Puerto 16000) │ │
│ └──────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

text

### Comunicación entre Microservicios
Este microservicio se comunica con el **Microservicio de Gestión de Flota** (puerto 16000) mediante REST, consumiendo sus endpoints para obtener información de los vehículos.

---

## Documentación Swagger

### Local
- **Swagger UI**: [http://localhost:28000/swagger-ui.html](http://localhost:28000/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:28000/api-docs](http://localhost:28000/api-docs)

### Remota (Producción)
- **Swagger UI**: [https://api.fasttrack.cl/swagger-ui.html](https://api.fasttrack.cl/swagger-ui.html)
- **OpenAPI JSON**: [https://api.fasttrack.cl/api-docs](https://api.fasttrack.cl/api-docs)

---

## Rutas Principales (Endpoints)

### Gestión de Paquetes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/paquetes` | Registrar un nuevo paquete |
| GET | `/api/v1/paquetes` | Listar paquetes activos |
| GET | `/api/v1/paquetes/entregando` | Listar paquetes en estado ENTREGANDO |
| PUT | `/api/v1/paquetes/{id}` | Actualizar estado del paquete |
| DELETE | `/api/v1/paquetes/{id}` | Eliminación lógica (soft delete) |
| GET | `/api/v1/paquetes/{id}/historial` | Caja Negra de Viaje (auditoría) |

### Proxy de Flota (Comunicación entre Microservicios)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/flota/vehiculos` | Listar vehículos desde el microservicio de Flota |
| GET | `/api/v1/flota/vehiculos/{id}` | Obtener vehículo por ID desde el microservicio de Flota |

### Diagnóstico

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/diagnostico/flota/{id}` | Probar conexión con el microservicio de Flota |
| GET | `/health` | Verificar estado del microservicio |

---

### Ejemplos de Requests

#### Crear un paquete
```bash
curl -X POST http://localhost:28000/api/v1/paquetes \
  -H "Content-Type: application/json" \
  -d '{
    "numeroSeguimiento": "PKT-001",
    "direccionEntrega": "Av. Providencia 1234, Santiago",
    "responsable": "Juan Pérez",
    "vehiculoId": 1
  }'
