package duoc.cl.Casoo4.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "audit_log_paquetes")
@Schema(description = "Registro de auditoría inalterable de eventos sobre paquetes")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado del registro de auditoría")
    private Long id;

    @Column(name = "numero_seguimiento", nullable = false, length = 50)
    @Schema(description = "Número de seguimiento del paquete afectado", 
            example = "PKT-001")
    private String numeroSeguimiento;

    @Column(name = "tipo_movimiento", nullable = false, length = 50)
    @Schema(description = "Tipo de movimiento registrado",
            example = "INGRESO_RUTA",
            allowableValues = {"INGRESO_RUTA", "CAMBIO_ESTADO", "EGRESO_SOFT_DELETE"})
    private String tipoMovimiento;

    @Column(name = "estado_anterior", length = 50)
    @Schema(description = "Estado previo al movimiento", 
            example = "EN_RUTA")
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 50)
    @Schema(description = "Nuevo estado tras el movimiento", 
            example = "ENTREGANDO")
    private String estadoNuevo;

    @Column(name = "fecha_hora", nullable = false)
    @Schema(description = "Fecha y hora exacta del movimiento")
    private LocalDateTime fechaHora;

    @Column(length = 200)
    @Schema(description = "Observación adicional sobre el movimiento", 
            example = "Paquete marcado como eliminado (soft delete)")
    private String observacion;

    @Column(name = "usuario", length = 50)
    @Schema(description = "Usuario que realizó la acción", 
            example = "admin@fasttrack.cl")
    private String usuario;

    public AuditLog(String numeroSeguimiento, String tipoMovimiento,
                    String estadoAnterior, String estadoNuevo, 
                    String observacion, String usuario) {
        this.numeroSeguimiento = numeroSeguimiento;
        this.tipoMovimiento = tipoMovimiento;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.observacion = observacion;
        this.usuario = usuario != null ? usuario : "SISTEMA";
        this.fechaHora = LocalDateTime.now();
    }

    public AuditLog(String numeroSeguimiento, String tipoMovimiento,
                    String estadoAnterior, String estadoNuevo, 
                    String observacion) {
        this(numeroSeguimiento, tipoMovimiento, estadoAnterior, 
             estadoNuevo, observacion, "SISTEMA");
    }
}
