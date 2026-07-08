package duoc.cl.Casoo4.service;

import duoc.cl.Casoo4.exception.LogisticaException;
import duoc.cl.Casoo4.model.AuditLog;
import duoc.cl.Casoo4.model.Paquete;
import duoc.cl.Casoo4.repository.AuditLogRepository;
import duoc.cl.Casoo4.repository.PaqueteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaqueteService {

    private static final Logger logger = LoggerFactory.getLogger(PaqueteService.class);
    private static final List<String> ESTADOS_VALIDOS = List.of("EN_RUTA", "ENTREGANDO", "ENTREGADO");
    private static final List<String> TRANSICIONES_VALIDAS = List.of(
            "EN_RUTA->ENTREGANDO",
            "ENTREGANDO->ENTREGADO",
            "EN_RUTA->ENTREGADO"
    );

    private final PaqueteRepository repository;
    private final AuditLogRepository auditLogRepository;

    public PaqueteService(PaqueteRepository repository,
                          AuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public Paquete crear(Paquete paquete) {
        logger.info("Creando nuevo paquete con seguimiento: {}", paquete.getNumeroSeguimiento());

        if (repository.existsById(paquete.getNumeroSeguimiento())) {
            logger.warn("Intento de duplicar número de seguimiento: {}", paquete.getNumeroSeguimiento());
            throw new LogisticaException(
                    "Ya existe un paquete con seguimiento: " + paquete.getNumeroSeguimiento());
        }



        paquete.setEstado("EN_RUTA");
        paquete.setActivo(true);
        Paquete guardado = repository.save(paquete);
        logger.info("Paquete guardado exitosamente: {}", guardado.getNumeroSeguimiento());


        AuditLog log = new AuditLog(
                guardado.getNumeroSeguimiento(),
                "INGRESO_RUTA",
                null,
                "EN_RUTA",
                "Paquete ingresado al sistema de rastreo",
                "SISTEMA"
        );
        auditLogRepository.save(log);
        logger.debug("AuditLog registrado: INGRESO_RUTA para {}", guardado.getNumeroSeguimiento());

        return guardado;
    }

    public List<Paquete> listar() {
        logger.debug("Listando todos los paquetes activos");
        return repository.findByActivoTrue();
    }

    public List<Paquete> entregando() {
        logger.debug("Listando paquetes en estado ENTREGANDO");
        return repository.findByActivoTrueAndEstado("ENTREGANDO");
    }

    @Transactional
    public Optional<Paquete> actualizar(String id, Paquete nuevo) {
        logger.info("Actualizando paquete: {}", id);

        Optional<Paquete> existente = repository.findById(id);
        if (existente.isEmpty()) {
            logger.warn("Paquete no encontrado para actualización: {}", id);
            return Optional.empty();
        }

        Paquete paquete = existente.get();

        if (!paquete.isActivo()) {
            logger.warn("Intento de modificar paquete eliminado: {}", id);
            throw new LogisticaException(
                    "No se puede modificar un paquete eliminado: " + id);
        }


        String nuevoEstado = nuevo.getEstado();
        if (nuevoEstado != null) {
            if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
                throw new LogisticaException(
                        "Estado inválido: " + nuevoEstado + ". Válidos: " + ESTADOS_VALIDOS);
            }

            String estadoActual = paquete.getEstado();
            String transicion = estadoActual + "->" + nuevoEstado;


            if (!TRANSICIONES_VALIDAS.contains(transicion) && !estadoActual.equals(nuevoEstado)) {
                logger.warn("Transición de estado inválida: {} -> {}", estadoActual, nuevoEstado);
                throw new LogisticaException(
                        "Transición inválida: " + estadoActual + " → " + nuevoEstado +
                                ". Transiciones permitidas: EN_RUTA→ENTREGANDO, ENTREGANDO→ENTREGADO");
            }

            String estadoAnterior = paquete.getEstado();
            paquete.setEstado(nuevoEstado);
            logger.info("Estado actualizado: {} → {}", estadoAnterior, nuevoEstado);


            AuditLog log = new AuditLog(
                    id,
                    "CAMBIO_ESTADO",
                    estadoAnterior,
                    nuevoEstado,
                    "Cambio de estado registrado",
                    "SISTEMA"
            );
            auditLogRepository.save(log);
            logger.debug("AuditLog registrado: CAMBIO_ESTADO para {}", id);
        }


        if (nuevo.getDireccionEntrega() != null) {
            paquete.setDireccionEntrega(nuevo.getDireccionEntrega());
        }
        if (nuevo.getResponsable() != null) {
            paquete.setResponsable(nuevo.getResponsable());
        }
        if (nuevo.getVehiculoId() != null) {
            paquete.setVehiculoId(nuevo.getVehiculoId());
        }

        Paquete actualizado = repository.save(paquete);
        logger.info("Paquete actualizado exitosamente: {}", id);
        return Optional.of(actualizado);
    }

    @Transactional
    public boolean eliminar(String id) {
        logger.info("Eliminando (soft delete) paquete: {}", id);

        Optional<Paquete> existente = repository.findById(id);
        if (existente.isPresent()) {
            Paquete paquete = existente.get();
            String estadoFinal = paquete.getEstado();
            paquete.setActivo(false);
            repository.save(paquete);
            logger.info("Paquete marcado como inactivo: {}", id);


            AuditLog log = new AuditLog(
                    id,
                    "EGRESO_SOFT_DELETE",
                    estadoFinal,
                    null,
                    "Paquete ocultado mediante soft delete (baja lógica)",
                    "SISTEMA"
            );
            auditLogRepository.save(log);
            logger.debug("AuditLog registrado: EGRESO_SOFT_DELETE para {}", id);

            return true;
        }

        logger.warn("Intento de eliminar paquete inexistente: {}", id);
        return false;
    }

    public List<AuditLog> obtenerHistorial(String numeroSeguimiento) {
        logger.info("Consultando historial del paquete: {}", numeroSeguimiento);

        if (!repository.existsById(numeroSeguimiento)) {
            logger.warn("Consulta de historial para paquete inexistente: {}", numeroSeguimiento);
            throw new LogisticaException(
                    "No existe paquete con seguimiento: " + numeroSeguimiento);
        }

        List<AuditLog> historial = auditLogRepository
                .findByNumeroSeguimientoOrderByFechaHoraAsc(numeroSeguimiento);

        logger.debug("Historial encontrado: {} registros", historial.size());
        return historial;
    }
}