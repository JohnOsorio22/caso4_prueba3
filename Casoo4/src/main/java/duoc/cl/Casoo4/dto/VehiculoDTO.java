package duoc.cl.Casoo4.dto;

import lombok.Data;

@Data
public class VehiculoDTO {
    private Long id;
    private String patente;
    private String marca;
    private String modelo;
    private Boolean disponible;
}
