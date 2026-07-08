package duoc.cl.Casoo4.controller;

import duoc.cl.Casoo4.model.AuditLog;
import duoc.cl.Casoo4.model.Paquete;
import duoc.cl.Casoo4.service.PaqueteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/paquetes")
@Tag(name = "Paquetes", description = "Gestión y rastreo de paquetes en tránsito")
public class PaqueteController {

    private static final Logger logger = LoggerFactory.getLogger(PaqueteController.class);
    private final PaqueteService service;

    public PaqueteController(PaqueteService service) {
        this.service = service;
    }

    @Operation(summary = "Registrar un nuevo paquete",
            description = "Crea un paquete con estado inicial EN_RUTA y registra el evento en la Caja Negra de Viaje.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paquete creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Paquete.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o número de seguimiento duplicado"),
            @ApiResponse(responseCode = "422", description = "Validación de negocio fallida")
    })
    @PostMapping
    public ResponseEntity<Paquete> crear(@Valid @RequestBody Paquete paquete) {
        logger.info("Solicitud de creación de paquete: {}", paquete.getNumeroSeguimiento());
        Paquete nuevo = service.crear(paquete);
        logger.info("Paquete creado exitosamente: {}", nuevo.getNumeroSeguimiento());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @Operation(summary = "Listar paquetes activos",
            description = "Retorna todos los paquetes con activo=true (excluye los eliminados por soft delete).")
    @ApiResponse(responseCode = "200", description = "Lista de paquetes activos")
    @GetMapping
    public ResponseEntity<List<Paquete>> listar() {
        logger.debug("Listando todos los paquetes activos");
        return ResponseEntity.ok(service.listar());
    }

    @Operation(summary = "Listar paquetes en estado ENTREGANDO",
            description = "Filtra los paquetes activos que están en proceso de entrega.")
    @ApiResponse(responseCode = "200", description = "Lista de paquetes en estado ENTREGANDO")
    @GetMapping("/entregando")
    public ResponseEntity<List<Paquete>> entregando() {
        logger.debug("Listando paquetes en estado ENTREGANDO");
        return ResponseEntity.ok(service.entregando());
    }

    @Operation(summary = "Actualizar estado de un paquete",
            description = "Modifica el estado del paquete (EN_RUTA → ENTREGANDO → ENTREGADO) y registra la transición en la Caja Negra.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o paquete eliminado"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Paquete> actualizar(
            @Parameter(description = "Número de seguimiento del paquete",
                    example = "PKT-001")
            @PathVariable String id,
            @Valid @RequestBody Paquete paquete) {
        logger.info("Solicitud de actualización de paquete: {}", id);
        return service.actualizar(id, paquete)
                .map(p -> {
                    logger.info("Paquete actualizado: {}", id);
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar paquete (Soft Delete)",
            description = "Marca el paquete como inactivo (activo=false). El paquete desaparece de las listas pero su historial queda en la Caja Negra de Viaje.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquete eliminado lógicamente"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> eliminar(
            @Parameter(description = "Número de seguimiento del paquete",
                    example = "PKT-001")
            @PathVariable String id) {
        logger.info("Solicitud de eliminación de paquete: {}", id);
        if (service.eliminar(id)) {
            logger.info("Paquete eliminado (soft delete): {}", id);
            return ResponseEntity.ok(new ResponseMessage(
                    "Paquete eliminado (soft delete). Historial disponible en /historial/" + id
            ));
        }
        logger.warn("Intento de eliminar paquete inexistente: {}", id);
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Consultar Caja Negra de Viaje (historial de auditoría)",
            description = "Retorna el historial completo e inalterable de todos los eventos ocurridos sobre el paquete, ordenados cronológicamente. Disponible incluso para paquetes eliminados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de auditoría del paquete"),
            @ApiResponse(responseCode = "404", description = "Paquete no encontrado en el sistema")
    })
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<AuditLog>> historial(
            @Parameter(description = "Número de seguimiento del paquete",
                    example = "PKT-001")
            @PathVariable String id) {
        logger.debug("Consultando historial del paquete: {}", id);
        return ResponseEntity.ok(service.obtenerHistorial(id));
    }


    public static class ResponseMessage {
        private final String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}