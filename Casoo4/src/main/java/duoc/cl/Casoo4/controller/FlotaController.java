package duoc.cl.Casoo4.controller;

import duoc.cl.Casoo4.dto.VehiculoDTO;
import duoc.cl.Casoo4.service.FlotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/flota")
@Tag(name = "Flota (proxy)", description = "Consulta de vehículos del microservicio de Gestión de Flota")
public class FlotaController {

    private static final Logger logger = LoggerFactory.getLogger(FlotaController.class);
    private final FlotaService flotaService;

    public FlotaController(FlotaService flotaService) {
        this.flotaService = flotaService;
    }

    @Operation(summary = "Obtener un vehículo por ID",
            description = "Consulta el microservicio de Flota y retorna el vehículo correspondiente.")
    @ApiResponse(responseCode = "200", description = "Vehículo encontrado")
    @GetMapping("/vehiculos/{id}")
    public ResponseEntity<VehiculoDTO> obtenerVehiculo(
            @Parameter(description = "ID del vehículo", example = "1")
            @PathVariable Long id) {
        logger.info("Proxy: Consultando vehículo ID {} desde Flota", id);
        VehiculoDTO vehiculo = flotaService.obtenerVehiculo(id);
        logger.debug("Proxy: Vehículo obtenido: {}", vehiculo.getPatente());
        return ResponseEntity.ok(vehiculo);
    }

    @Operation(summary = "Listar todos los vehículos",
            description = "Retorna la lista completa de vehículos registrados en el microservicio de Flota.")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos")
    @GetMapping("/vehiculos")
    public ResponseEntity<VehiculoDTO[]> listarVehiculos() {
        logger.info("Proxy: Listando todos los vehículos desde Flota");
        return ResponseEntity.ok(flotaService.listarVehiculos());
    }
}