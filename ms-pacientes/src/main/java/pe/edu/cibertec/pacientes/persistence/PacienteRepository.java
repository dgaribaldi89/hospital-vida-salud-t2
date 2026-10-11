package pe.edu.cibertec.pacientes.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import pe.edu.cibertec.pacientes.bean.entity.Paciente;
import reactor.core.publisher.Mono;

public interface PacienteRepository
        extends ReactiveCrudRepository<Paciente, Long> {

    Mono<Paciente> findByTipoDocumentoAndNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );

    Mono<Boolean> existsByTipoDocumentoAndNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );
}