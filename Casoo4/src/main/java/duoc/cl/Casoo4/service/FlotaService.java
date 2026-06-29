package duoc.cl.Casoo4.service;

import duoc.cl.Casoo4.dto.VehiculoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
        String url = flotaUrl + "/api/vehiculos/" + id;
        return restTemplate.getForObject(url, VehiculoDTO.class);
    }

    public VehiculoDTO[] listarVehiculos() {
        String url = flotaUrl + "/api/vehiculos";
        return restTemplate.getForObject(url, VehiculoDTO[].class);
    }
}
