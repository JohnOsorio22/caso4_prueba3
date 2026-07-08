package duoc.cl.Casoo4;

import duoc.cl.Casoo4.dto.VehiculoDTO;
import duoc.cl.Casoo4.exception.LogisticaException;
import duoc.cl.Casoo4.service.FlotaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlotaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FlotaService flotaService;

    @Test
    @DisplayName("obtenerVehiculo() – debe retornar vehículo cuando existe")
    void obtenerVehiculo_debeRetornarVehiculo() {

        VehiculoDTO vehiculo = new VehiculoDTO();
        vehiculo.setId(1L);
        vehiculo.setPatente("ABCD12");
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Hilux");
        vehiculo.setDisponible(true);

        when(restTemplate.getForObject(anyString(), eq(VehiculoDTO.class)))
                .thenReturn(vehiculo);


        VehiculoDTO resultado = flotaService.obtenerVehiculo(1L);


        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getPatente()).isEqualTo("ABCD12");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(VehiculoDTO.class));
    }

    @Test
    @DisplayName("obtenerVehiculo() – debe lanzar excepción cuando no existe (404)")
    void obtenerVehiculo_debeLanzarExcepcionCuandoNotFound() {

        when(restTemplate.getForObject(anyString(), eq(VehiculoDTO.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);


        assertThatThrownBy(() -> flotaService.obtenerVehiculo(99L))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("Vehículo no encontrado");
    }

    @Test
    @DisplayName("obtenerVehiculo() – debe lanzar excepción cuando falla la comunicación")
    void obtenerVehiculo_debeLanzarExcepcionCuandoFallaComunicacion() {

        when(restTemplate.getForObject(anyString(), eq(VehiculoDTO.class)))
                .thenThrow(new RestClientException("Connection refused"));


        assertThatThrownBy(() -> flotaService.obtenerVehiculo(1L))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("Error de comunicación con Flota");
    }

    @Test
    @DisplayName("obtenerVehiculo() – debe lanzar excepción cuando retorna null")
    void obtenerVehiculo_debeLanzarExcepcionCuandoRetornaNull() {

        when(restTemplate.getForObject(anyString(), eq(VehiculoDTO.class)))
                .thenReturn(null);


        assertThatThrownBy(() -> flotaService.obtenerVehiculo(1L))
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("Vehículo no encontrado");
    }

    @Test
    @DisplayName("listarVehiculos() – debe retornar array de vehículos")
    void listarVehiculos_debeRetornarArray() {

        VehiculoDTO[] vehiculos = new VehiculoDTO[2];
        vehiculos[0] = new VehiculoDTO();
        vehiculos[0].setId(1L);
        vehiculos[1] = new VehiculoDTO();
        vehiculos[1].setId(2L);

        when(restTemplate.getForObject(anyString(), eq(VehiculoDTO[].class)))
                .thenReturn(vehiculos);


        VehiculoDTO[] resultado = flotaService.listarVehiculos();


        assertThat(resultado).hasSize(2);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(VehiculoDTO[].class));
    }

    @Test
    @DisplayName("listarVehiculos() – debe lanzar excepción cuando falla la comunicación")
    void listarVehiculos_debeLanzarExcepcionCuandoFalla() {

        when(restTemplate.getForObject(anyString(), eq(VehiculoDTO[].class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> flotaService.listarVehiculos())
                .isInstanceOf(LogisticaException.class)
                .hasMessageContaining("Error al listar vehículos");
    }
}