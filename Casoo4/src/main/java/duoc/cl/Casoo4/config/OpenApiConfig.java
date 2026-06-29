package duoc.cl.Casoo4.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fastTrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FastTrack Courier – API de Rastreo Logístico")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión y rastreo de paquetes en tránsito.
                                
                                **Características principales:**
                                - CRUD completo de paquetes con soft delete
                                - Caja Negra de Viaje: auditoría inalterable de cada evento
                                - Proxy de consulta al microservicio de Gestión de Flota
                                - Integración B2B: documentación técnica estándar OpenAPI 3.0
                                """)
                        .contact(new Contact()
                                .name("Equipo FastTrack – DUOC UC")
                                .email("soporte@fasttrack.cl"))
                        .license(new License()
                                .name("Uso interno FastTrack Courier")))
                .servers(List.of(
                        new Server().url("http://localhost:28000").description("Servidor local"),
                        new Server().url("https://api.fasttrack.cl").description("Producción")
                ));
    }
}
