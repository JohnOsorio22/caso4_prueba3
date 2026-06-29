package duoc.cl.Casoo4.controller;

import duoc.cl.Casoo4.dto.VehiculoDTO;
import duoc.cl.Casoo4.service.FlotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/diagnostico")
@Tag(name = "Diagnóstico", description = "Endpoints de verificación de conectividad entre microservicios")
public class PruebaComunicacion {

    private final FlotaService flotaService;

    public PruebaComunicacion(FlotaService flotaService) {
        this.flotaService = flotaService;
    }

    @Operation(summary = "Probar conexión con microservicio de Flota",
               description = "Consulta un vehículo por ID para verificar la comunicación REST entre microservicios.")
    @GetMapping("/flota/{id}")
    public String probar(@PathVariable Long id) {
        try {
            VehiculoDTO v = flotaService.obtenerVehiculo(id);
            return "Conexión exitosa. Vehículo: " + v.getPatente()
                    + " - " + v.getMarca() + " " + v.getModelo();
        } catch (Exception e) {
            return "Error de conexión con Flota: " + e.getMessage();
        }
    }
}
