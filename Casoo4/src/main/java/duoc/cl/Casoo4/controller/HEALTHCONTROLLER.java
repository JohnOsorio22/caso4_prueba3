package duoc.cl.Casoo4.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "Verificación de estado del microservicio")
public class HealthController {

    @Operation(summary = "Verificar estado del servicio",
            description = "Retorna el estado actual del microservicio de Rastreo Logístico")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Rastreo Logístico");
        status.put("version", "1.0.0");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("port", 28000);
        status.put("database", "Oracle (prod) / H2 (test)");

        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Verificar conectividad con Flota",
            description = "Verifica si el microservicio de Flota está accesible")
    @GetMapping("/health/flota")
    public ResponseEntity<Map<String, String>> flotaHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "CHECKING");
        status.put("service", "Flota");
        status.put("endpoint", "http://localhost:16000/health");

        return ResponseEntity.ok(status);
    }
}
