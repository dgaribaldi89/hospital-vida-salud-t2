package pe.edu.cibertec.citas.persistence;

import java.time.LocalDateTime;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import pe.edu.cibertec.citas.bean.entity.Cita;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CitaRepository
        extends ReactiveCrudRepository<Cita, Long> {

    Mono<Cita> findByCodigoCita(String codigoCita);

    Flux<Cita> findByPacienteId(Long pacienteId);

    Mono<Boolean> existsByPacienteIdAndFechaHora(
            Long pacienteId,
            LocalDateTime fechaHora
    );

    Mono<Boolean> existsByProfesionalCodigoAndFechaHora(
            String profesionalCodigo,
            LocalDateTime fechaHora
    );
}