package duoc.cl.Casoo4.repository;

import duoc.cl.Casoo4.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, String> {

    List<Paquete> findByActivoTrue();

    List<Paquete> findByActivoTrueAndEstado(String estado);
}
