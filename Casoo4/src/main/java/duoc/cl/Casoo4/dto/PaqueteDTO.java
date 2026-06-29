package duoc.cl.Casoo4.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaqueteDTO {
    @NotBlank(message = "El número de seguimiento es obligatorio")
    private String numeroSeguimiento;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El responsable es obligatorio")
    private String responsable;
}
