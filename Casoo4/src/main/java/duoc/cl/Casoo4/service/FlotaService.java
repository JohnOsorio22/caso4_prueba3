package duoc.cl.Casoo4.service;

import duoc.cl.Casoo4.dto.VehiculoDTO;
import duoc.cl.Casoo4.exception.LogisticaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class FlotaService {

    private static final Logger logger = LoggerFactory.getLogger(FlotaService.class);
    private final RestTemplate restTemplate;
    private final String flotaUrl;

    public FlotaService(RestTemplate restTemplate,
                        @Value("${flota.url}") String flotaUrl) {
        this.restTemplate = restTemplate;
        this.flotaUrl = flotaUrl;
        logger.info("FlotaService inicializado con URL: {}", flotaUrl);
    }

    public VehiculoDTO obtenerVehiculo(Long id) {
        logger.info("Consultando vehículo ID {} desde Flota", id);
        try {
            String url = flotaUrl + "/api/vehiculos/" + id;
            VehiculoDTO vehiculo = restTemplate.getForObject(url, VehiculoDTO.class);

            if (vehiculo == null) {
                logger.warn("Vehículo ID {} no encontrado en Flota", id);
                throw new LogisticaException("Vehículo no encontrado en Flota: " + id);
            }

            logger.debug("Vehículo obtenido: {}", vehiculo.getPatente());
            return vehiculo;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Vehículo ID {} no encontrado (404)", id);
            throw new LogisticaException("Vehículo no encontrado en Flota: " + id);
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al consultar Flota: {}", e.getStatusCode());
            throw new LogisticaException("Error en Flota: " + e.getStatusText());
        } catch (RestClientException e) {
            logger.error("Error de comunicación con Flota: {}", e.getMessage());
            throw new LogisticaException("Error de comunicación con Flota: " + e.getMessage());
        }
    }

    public VehiculoDTO[] listarVehiculos() {
        logger.info("Listando todos los vehículos desde Flota");
        try {
            String url = flotaUrl + "/api/vehiculos";
            VehiculoDTO[] vehiculos = restTemplate.getForObject(url, VehiculoDTO[].class);

            int count = vehiculos != null ? vehiculos.length : 0;
            logger.debug("Vehículos obtenidos: {}", count);
            return vehiculos;
        } catch (RestClientException e) {
            logger.error("Error al listar vehículos desde Flota: {}", e.getMessage());
            throw new LogisticaException("Error al listar vehículos desde Flota: " + e.getMessage());
        }
    }
}