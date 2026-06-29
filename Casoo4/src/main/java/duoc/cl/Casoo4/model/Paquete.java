package duoc.cl.Casoo4.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "paquetes")
@Schema(description = "Entidad que representa un paquete en tránsito")
public class Paquete {

    @Id
    @Column(name = "numero_seguimiento", nullable = false, unique = true, length = 50)
    @NotBlank(message = "El número de seguimiento es obligatorio")
    @Pattern(regexp = "^[A-Z0-9\\-]{6,20}$", message = "Código de seguimiento inválido")
    @Schema(description = "Código único de seguimiento (6-20 chars, mayúsculas y números)", example = "PKT-001")
    private String numeroSeguimiento;

    @Column(nullable = false)
    @NotBlank(message = "Debe tener dirección")
    @Schema(description = "Dirección de entrega del paquete", example = "Av. Providencia 1234, Santiago")
    private String direccionEntrega;

    @Column(nullable = false)
    @NotBlank(message = "Debe tener responsable")
    @Schema(description = "Nombre del responsable del paquete", example = "Juan Pérez")
    private String responsable;

    @Column(nullable = false)
    @Schema(description = "Estado actual del paquete", example = "EN_RUTA",
            allowableValues = {"EN_RUTA", "ENTREGANDO", "ENTREGADO"})
    private String estado = "EN_RUTA";

    @Column(nullable = false)
    @Schema(description = "ID del vehículo asignado", example = "1")
    private Long vehiculoId;

    @Column(nullable = false)
    @Schema(description = "Indica si el paquete está activo (soft-delete)", example = "true")
    private boolean activo = true;
}
