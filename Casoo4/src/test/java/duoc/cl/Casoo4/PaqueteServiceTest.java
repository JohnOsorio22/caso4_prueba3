package duoc.cl.Casoo4;

import duoc.cl.Casoo4.exception.LogisticaException;
import duoc.cl.Casoo4.model.AuditLog;
import duoc.cl.Casoo4.model.Paquete;
import duoc.cl.Casoo4.repository.AuditLogRepository;
import duoc.cl.Casoo4.repository.PaqueteRepository;
import duoc.cl.Casoo4.service.PaqueteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaqueteServiceTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private PaqueteService paqueteService;

    private Paquete paqueteBase;

    @BeforeEach
    void setUp() {
        paqueteBase = new Paquete();
        paqueteBase.setNumeroSeguimiento("PKT-001");
        paqueteBase.setDireccionEntrega("Av. Test 123");
        paqueteBase.setResponsable("Juan Test");
        paqueteBase.setEstado("EN_RUTA");
        paqueteBase.setActivo(true);
        paqueteBase.setVehiculoId(1L);
    }

    // ============================================================
    // TESTS PARA crear()
    // ============================================================

    @Test
    @DisplayName("crear() – debe asignar estado EN_RUTA y activo=true")
    void crear_debeAsignarEstadoYActivoCorrectamente() {
        when(paqueteRepository.existsById("PKT-001")).thenReturn(false);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteBase);

        Paquete resultado = paqueteService.crear(paqueteBase);

        assertThat(resultado.getEstado()).isEqualTo("EN_RUTA");
        assertThat(resultado.isActivo()).isTrue();
        verify(paqueteRepository).save(paqueteBase);
    }

    @Test
    @DisplayName("crear() – debe registrar AuditLog con tipoMovimiento INGRESO_RUTA")
    void crear_debeRegistrarAuditLogDeIngreso() {
        when(paqueteRepository.existsById("PKT-001")).thenReturn(false);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteBase);

        paqueteService.crear(paqueteBase);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog log = captor.getValue();
        assertThat(log.getTipoMovimiento()).isEqualTo("INGRESO_RUTA");
        assertThat(log.getNumeroSeguimiento()).isEqualTo("PKT-001");
        assertThat(log.getEstadoNuevo()).isEqualTo("EN_RUTA");
        assertThat(log.getEstadoAnterior()).isNull();
    }

    @Test
    @DisplayName("crear() – debe lanzar excepción si el número de seguimiento ya existe")
    void crear_debeLanzarExcepcionSiYaExiste() {
        when(paqueteRepository.existsById("PKT-001")).thenReturn(true);

        assertThatThrownBy(() -> paqueteService.crear(paqueteBase))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("PKT-001");

        verify(paqueteRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear() – debe registrar el usuario 'SISTEMA' en el AuditLog por defecto")
    void crear_debeRegistrarUsuarioSistemaEnAudit() {
        when(paqueteRepository.existsById("PKT-001")).thenReturn(false);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(paqueteBase);

        paqueteService.crear(paqueteBase);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog log = captor.getValue();
        assertThat(log.getUsuario()).isEqualTo("SISTEMA");
    }

    // ============================================================
    // TESTS PARA listar() y entregando()
    // ============================================================

    @Test
    @DisplayName("listar() – debe retornar solo paquetes activos")
    void listar_debeRetornarSoloPaquetesActivos() {
        when(paqueteRepository.findByActivoTrue()).thenReturn(List.of(paqueteBase));

        List<Paquete> resultado = paqueteService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isActivo()).isTrue();
    }

    @Test
    @DisplayName("entregando() – debe retornar paquetes con estado ENTREGANDO")
    void entregando_debeRetornarPaquetesEnEstadoEntregando() {
        paqueteBase.setEstado("ENTREGANDO");
        when(paqueteRepository.findByActivoTrueAndEstado("ENTREGANDO"))
                .thenReturn(List.of(paqueteBase));

        List<Paquete> resultado = paqueteService.entregando();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo("ENTREGANDO");
    }

    // ============================================================
    // TESTS PARA actualizar()
    // ============================================================

    @Test
    @DisplayName("actualizar() – debe cambiar estado y registrar AuditLog CAMBIO_ESTADO")
    void actualizar_debeCambiarEstadoYRegistrarAudit() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));
        Paquete actualizado = new Paquete();
        actualizado.setNumeroSeguimiento("PKT-001");
        actualizado.setEstado("ENTREGANDO");
        actualizado.setActivo(true);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(actualizado);

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGANDO");

        Optional<Paquete> resultado = paqueteService.actualizar("PKT-001", cambio);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo("ENTREGANDO");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertThat(log.getTipoMovimiento()).isEqualTo("CAMBIO_ESTADO");
        assertThat(log.getEstadoAnterior()).isEqualTo("EN_RUTA");
        assertThat(log.getEstadoNuevo()).isEqualTo("ENTREGANDO");
    }

    @Test
    @DisplayName("actualizar() – debe retornar Optional.empty() si el paquete no existe")
    void actualizar_debeRetornarVacioSiNoExiste() {
        when(paqueteRepository.findById("PKT-999")).thenReturn(Optional.empty());

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGANDO");

        Optional<Paquete> resultado = paqueteService.actualizar("PKT-999", cambio);

        assertThat(resultado).isEmpty();
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() – debe lanzar excepción si el estado es inválido")
    void actualizar_debeLanzarExcepcionConEstadoInvalido() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("PERDIDO");

        assertThatThrownBy(() -> paqueteService.actualizar("PKT-001", cambio))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("Estado inválido");

        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() – debe lanzar excepción si el paquete está inactivo (soft deleted)")
    void actualizar_debeLanzarExcepcionSiPaqueteInactivo() {
        paqueteBase.setActivo(false);
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGANDO");

        assertThatThrownBy(() -> paqueteService.actualizar("PKT-001", cambio))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("eliminado");
    }

    @Test
    @DisplayName("actualizar() – debe validar transiciones de estado inválidas (EN_RUTA → ENTREGADO)")
    void actualizar_debeValidarTransicionesInvalidas() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGADO");

        assertThatThrownBy(() -> paqueteService.actualizar("PKT-001", cambio))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("Transición inválida");
    }

    @Test
    @DisplayName("actualizar() – debe permitir transición ENTREGANDO → ENTREGADO")
    void actualizar_debePermitirTransicionEntregandoAEntregado() {
        paqueteBase.setEstado("ENTREGANDO");
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGADO");

        Paquete actualizado = new Paquete();
        actualizado.setNumeroSeguimiento("PKT-001");
        actualizado.setEstado("ENTREGADO");
        actualizado.setActivo(true);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(actualizado);

        Optional<Paquete> resultado = paqueteService.actualizar("PKT-001", cambio);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo("ENTREGADO");
    }

    @Test
    @DisplayName("actualizar() – debe permitir mantener el mismo estado (no hacer nada)")
    void actualizar_debePermitirMismoEstado() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("EN_RUTA");

        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Paquete> resultado = paqueteService.actualizar("PKT-001", cambio);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo("EN_RUTA");
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() – debe permitir actualizar dirección y responsable junto con el estado")
    void actualizar_debePermitirActualizarCamposAdicionales() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGANDO");
        cambio.setDireccionEntrega("Nueva Dirección 456");
        cambio.setResponsable("María González");

        Paquete actualizado = new Paquete();
        actualizado.setNumeroSeguimiento("PKT-001");
        actualizado.setEstado("ENTREGANDO");
        actualizado.setDireccionEntrega("Nueva Dirección 456");
        actualizado.setResponsable("María González");
        actualizado.setActivo(true);
        actualizado.setVehiculoId(1L);

        when(paqueteRepository.save(any(Paquete.class))).thenReturn(actualizado);

        Optional<Paquete> resultado = paqueteService.actualizar("PKT-001", cambio);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDireccionEntrega()).isEqualTo("Nueva Dirección 456");
        assertThat(resultado.get().getResponsable()).isEqualTo("María González");
    }

    @Test
    @DisplayName("actualizar() – debe permitir cambiar el vehículo asignado")
    void actualizar_debePermitirCambiarVehiculo() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setVehiculoId(5L);

        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Paquete> resultado = paqueteService.actualizar("PKT-001", cambio);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getVehiculoId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("actualizar() – debe registrar el usuario 'SISTEMA' en el AuditLog al cambiar estado")
    void actualizar_debeRegistrarUsuarioSistemaEnAuditLog() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));

        Paquete cambio = new Paquete();
        cambio.setEstado("ENTREGANDO");

        Paquete actualizado = new Paquete();
        actualizado.setNumeroSeguimiento("PKT-001");
        actualizado.setEstado("ENTREGANDO");
        actualizado.setActivo(true);
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(actualizado);

        paqueteService.actualizar("PKT-001", cambio);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog log = captor.getValue();
        assertThat(log.getUsuario()).isEqualTo("SISTEMA");
        assertThat(log.getTipoMovimiento()).isEqualTo("CAMBIO_ESTADO");
    }

    // ============================================================
    // TESTS PARA eliminar()
    // ============================================================

    @Test
    @DisplayName("eliminar() – debe marcar activo=false y registrar AuditLog EGRESO_SOFT_DELETE")
    void eliminar_debeMarcaInactivoYRegistrarAudit() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean resultado = paqueteService.eliminar("PKT-001");

        assertThat(resultado).isTrue();
        assertThat(paqueteBase.isActivo()).isFalse();

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertThat(log.getTipoMovimiento()).isEqualTo("EGRESO_SOFT_DELETE");
        assertThat(log.getEstadoAnterior()).isEqualTo("EN_RUTA");
        assertThat(log.getEstadoNuevo()).isNull();
    }

    @Test
    @DisplayName("eliminar() – debe retornar false si el paquete no existe")
    void eliminar_debeRetornarFalseSiNoExiste() {
        when(paqueteRepository.findById("PKT-999")).thenReturn(Optional.empty());

        boolean resultado = paqueteService.eliminar("PKT-999");

        assertThat(resultado).isFalse();
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() – debe registrar el usuario 'SISTEMA' en AuditLog")
    void eliminar_debeRegistrarUsuarioEnAuditLog() {
        when(paqueteRepository.findById("PKT-001")).thenReturn(Optional.of(paqueteBase));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(inv -> inv.getArgument(0));

        paqueteService.eliminar("PKT-001");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog log = captor.getValue();
        assertThat(log.getUsuario()).isEqualTo("SISTEMA");
        assertThat(log.getTipoMovimiento()).isEqualTo("EGRESO_SOFT_DELETE");
    }

    // ============================================================
    // TESTS PARA obtenerHistorial()
    // ============================================================

    @Test
    @DisplayName("obtenerHistorial() – debe retornar lista de AuditLogs del paquete")
    void obtenerHistorial_debeRetornarLogs() {
        AuditLog log = new AuditLog("PKT-001", "INGRESO_RUTA", null, "EN_RUTA", "Ingreso");
        when(paqueteRepository.existsById("PKT-001")).thenReturn(true);
        when(auditLogRepository.findByNumeroSeguimientoOrderByFechaHoraAsc("PKT-001"))
                .thenReturn(List.of(log));

        List<AuditLog> historial = paqueteService.obtenerHistorial("PKT-001");

        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).getTipoMovimiento()).isEqualTo("INGRESO_RUTA");
    }

    @Test
    @DisplayName("obtenerHistorial() – debe lanzar excepción si el paquete no existe")
    void obtenerHistorial_debeLanzarExcepcionSiNoExiste() {
        when(paqueteRepository.existsById("PKT-999")).thenReturn(false);

        assertThatThrownBy(() -> paqueteService.obtenerHistorial("PKT-999"))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("PKT-999");
    }

    @Test
    @DisplayName("obtenerHistorial() – debe retornar historial vacío si no hay eventos")
    void obtenerHistorial_debeRetornarVacioSiNoHayEventos() {
        when(paqueteRepository.existsById("PKT-001")).thenReturn(true);
        when(auditLogRepository.findByNumeroSeguimientoOrderByFechaHoraAsc("PKT-001"))
                .thenReturn(List.of());

        List<AuditLog> historial = paqueteService.obtenerHistorial("PKT-001");

        assertThat(historial).isEmpty();
    }
}