package duoc.cl.Casoo4.service;

import duoc.cl.Casoo4.exception.LogisticaException;
import duoc.cl.Casoo4.model.AuditLog;
import duoc.cl.Casoo4.model.Paquete;
import duoc.cl.Casoo4.repository.AuditLogRepository;
import duoc.cl.Casoo4.repository.PaqueteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class PaqueteService {

    private static final List<String> ESTADOS_VALIDOS = List.of("EN_RUTA", "ENTREGANDO", "ENTREGADO");

    private final PaqueteRepository repository;
    private final AuditLogRepository auditLogRepository;

    public PaqueteService(PaqueteRepository repository,
                          AuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.auditLogRepository = auditLogRepository;
    }



    @Transactional
    public Paquete crear(Paquete paquete) {
        if (repository.existsById(paquete.getNumeroSeguimiento())) {
            throw new LogisticaException(
                    "Ya existe un paquete con seguimiento: " + paquete.getNumeroSeguimiento());
        }
        paquete.setEstado("EN_RUTA");
        paquete.setActivo(true);
        Paquete guardado = repository.save(paquete);


        auditLogRepository.save(new AuditLog(
                guardado.getNumeroSeguimiento(),
                "INGRESO_RUTA",
                null,
                "EN_RUTA",
                "Paquete ingresado al sistema de rastreo"
        ));

        return guardado;
    }



    public List<Paquete> listar() {
        return repository.findByActivoTrue();
    }

    public List<Paquete> entregando() {
        return repository.findByActivoTrueAndEstado("ENTREGANDO");
    }



    @Transactional
    public Optional<Paquete> actualizar(String id, Paquete nuevo) {
        Optional<Paquete> existente = repository.findById(id);
        if (existente.isEmpty()) return Optional.empty();

        Paquete paquete = existente.get();

        if (!paquete.isActivo()) {
            throw new LogisticaException(
                    "No se puede modificar un paquete eliminado: " + id);
        }

        String nuevoEstado = nuevo.getEstado();
        if (nuevoEstado != null && !ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new LogisticaException(
                    "Estado inválido: " + nuevoEstado + ". Válidos: " + ESTADOS_VALIDOS);
        }

        String estadoAnterior = paquete.getEstado();
        paquete.setEstado(nuevoEstado);
        Paquete actualizado = repository.save(paquete);


        auditLogRepository.save(new AuditLog(
                id,
                "CAMBIO_ESTADO",
                estadoAnterior,
                nuevoEstado,
                "Cambio de estado registrado"
        ));

        return Optional.of(actualizado);
    }


    @Transactional
    public boolean eliminar(String id) {
        Optional<Paquete> existente = repository.findById(id);
        if (existente.isPresent()) {
            Paquete paquete = existente.get();
            String estadoFinal = paquete.getEstado();
            paquete.setActivo(false);
            repository.save(paquete);

            auditLogRepository.save(new AuditLog(
                    id,
                    "EGRESO_SOFT_DELETE",
                    estadoFinal,
                    null,
                    "Paquete ocultado mediante soft delete (baja lógica)"
            ));

            return true;
        }
        return false;
    }


    public List<AuditLog> obtenerHistorial(String numeroSeguimiento) {
        if (!repository.existsById(numeroSeguimiento)) {
            throw new LogisticaException(
                    "No existe paquete con seguimiento: " + numeroSeguimiento);
        }
        return auditLogRepository
                .findByNumeroSeguimientoOrderByFechaHoraAsc(numeroSeguimiento);
    }
}
