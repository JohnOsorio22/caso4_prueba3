package duoc.cl.Casoo4.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.;
import jakarta.validation.constraints.;
import lombok.Data;

@Data
@Entity
@Table(name = "paquetes")
@Schema(description = "Entidad que representa un paquete en tránsito")
public class Paquete {

    @Id
    @Column(name = "numero_seguimiento", nullable = false, unique = true, length = 50)
    @NotBlank(message = "El número de seguimiento es obligatorio")
    @Pattern(regexp = "^[A-Z0-9\-]{6,20}$", 
             message = "Código de seguimiento inválido: debe tener 6-20 caracteres (mayúsculas, números y guiones)")
    @Schema(description = "Código único de seguimiento (6-20 chars, mayúsculas y números)", 
            example = "PKT-001")
    private String numeroSeguimiento;

    @Column(nullable = false)
    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    @Schema(description = "Dirección de entrega del paquete", 
            example = "Av. Providencia 1234, Santiago")
    private String direccionEntrega;

    @Column(nullable = false)
    @NotBlank(message = "El responsable es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del responsable debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre del responsable del paquete", 
            example = "Juan Pérez")
    private String responsable;

    @Column(nullable = false)
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(EN_RUTA|ENTREGANDO|ENTREGADO)$", 
             message = "Estado inválido: debe ser EN_RUTA, ENTREGANDO o ENTREGADO")
    @Schema(description = "Estado actual del paquete", 
            example = "EN_RUTA",
            allowableValues = {"EN_RUTA", "ENTREGANDO", "ENTREGADO"})
    private String estado = "EN_RUTA";

    @Column(nullable = false)
    @NotNull(message = "El ID del vehículo es obligatorio")
    @Min(value = 1, message = "El ID del vehículo debe ser mayor a 0")
    @Schema(description = "ID del vehículo asignado", 
            example = "1")
    private Long vehiculoId;

    @Column(nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    @Schema(description = "Indica si el paquete está activo (soft-delete)", 
            example = "true")
    private boolean activo = true;
}
