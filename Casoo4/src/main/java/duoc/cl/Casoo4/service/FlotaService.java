package duoc.cl.Casoo4.service;

import duoc.cl.Casoo4.dto.VehiculoDTO;
import duoc.cl.Casoo4.exception.LogisticaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class FlotaService {

    private final RestTemplate restTemplate;
    private final String flotaUrl;

    public FlotaService(RestTemplate restTemplate,
                        @Value("${flota.url}") String flotaUrl) {
        this.restTemplate = restTemplate;
        this.flotaUrl = flotaUrl;
    }

    public VehiculoDTO obtenerVehiculo(Long id) {
        try {
            String url = flotaUrl + "/api/vehiculos/" + id;
            return restTemplate.getForObject(url, VehiculoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new LogisticaException("Vehículo no encontrado en Flota: " + id);
        } catch (RestClientException e) {
            throw new LogisticaException("Error de comunicación con Flota: " + e.getMessage());
        }
    }

    public VehiculoDTO[] listarVehiculos() {
        try {
            String url = flotaUrl + "/api/vehiculos";
            return restTemplate.getForObject(url, VehiculoDTO[].class);
        } catch (RestClientException e) {
            throw new LogisticaException("Error al listar vehículos desde Flota: " + e.getMessage());
        }
    }
}
