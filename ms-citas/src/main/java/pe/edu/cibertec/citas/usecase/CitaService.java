package pe.edu.cibertec.citas.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.citas.bean.dto.CitaRequest;
import pe.edu.cibertec.citas.bean.dto.EstadoCitaRequest;
import pe.edu.cibertec.citas.bean.entity.Cita;
import pe.edu.cibertec.citas.client.PacienteClient;
import pe.edu.cibertec.citas.persistence.CitaRepository;
import pe.edu.cibertec.citas.publisher.CitaEventoPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteClient pacienteClient;
    private final CitaEventoPublisher citaEventoPublisher;

    public CitaService(
            CitaRepository citaRepository,
            PacienteClient pacienteClient,
            CitaEventoPublisher citaEventoPublisher) {

        this.citaRepository = citaRepository;
        this.pacienteClient = pacienteClient;
        this.citaEventoPublisher = citaEventoPublisher;
    }

    public Flux<Cita> listarTodos() {
        return citaRepository.findAll();
    }

    public Mono<Cita> buscarPorId(Long id) {

        return citaRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cita no encontrada"
                                )
                        )
                );
    }

    public Flux<Cita> listarPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    public Mono<Cita> registrar(CitaRequest request) {

        return pacienteClient
                .buscarPorId(request.getPacienteId())

                .flatMap(paciente -> {

                    if (!"ACTIVO".equalsIgnoreCase(paciente.getEstado())) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "El paciente se encuentra inactivo"
                                )
                        );
                    }

                    return Mono.zip(
                            citaRepository
                                    .existsByPacienteIdAndFechaHora(
                                            request.getPacienteId(),
                                            request.getFechaHora()
                                    ),

                            citaRepository
                                    .existsByProfesionalCodigoAndFechaHora(
                                            request.getProfesionalCodigo(),
                                            request.getFechaHora()
                                    )
                    );
                })

                .flatMap(validaciones -> {

                    Boolean pacienteOcupado = validaciones.getT1();
                    Boolean profesionalOcupado = validaciones.getT2();

                    if (pacienteOcupado) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "El paciente ya tiene una cita en esa fecha y hora"
                                )
                        );
                    }

                    if (profesionalOcupado) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "El profesional ya tiene una cita en esa fecha y hora"
                                )
                        );
                    }

                    LocalDateTime ahora = LocalDateTime.now();

                    Cita cita = Cita.builder()
                            .codigoCita(generarCodigoCita())
                            .pacienteId(request.getPacienteId())
                            .especialidad(request.getEspecialidad())
                            .fechaHora(request.getFechaHora())
                            .profesionalCodigo(request.getProfesionalCodigo())
                            .motivoAdministrativo(
                                    request.getMotivoAdministrativo()
                            )
                            .estado("PENDIENTE")
                            .fechaCreacion(ahora)
                            .ultimaActualizacion(ahora)
                            .build();

                    return citaRepository.save(cita);
                });
    }

    public Mono<Cita> cambiarEstado(
            Long id,
            EstadoCitaRequest request) {

        String nuevoEstado = request.getEstado().toUpperCase();

        return citaRepository.findById(id)

                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cita no encontrada"
                                )
                        )
                )

                .flatMap(cita -> {

                    String estadoActual = cita.getEstado();

                    if (estadoActual.equals(nuevoEstado)) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "La cita ya se encuentra en estado "
                                                + nuevoEstado
                                )
                        );
                    }

                    if (!transicionPermitida(
                            estadoActual,
                            nuevoEstado)) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "No se puede cambiar la cita de "
                                                + estadoActual
                                                + " a "
                                                + nuevoEstado
                                )
                        );
                    }

                    cita.setEstado(nuevoEstado);
                    cita.setUltimaActualizacion(
                            LocalDateTime.now()
                    );

                    return citaRepository.save(cita);
                })

                .flatMap(citaGuardada -> {

                    if ("CONFIRMADA".equals(citaGuardada.getEstado())) {

                        return Mono
                                .fromRunnable(() ->
                                        citaEventoPublisher
                                                .publicar(citaGuardada)
                                )
                                .subscribeOn(Schedulers.boundedElastic())
                                .thenReturn(citaGuardada);
                    }

                    return Mono.just(citaGuardada);
                });
    }

    private boolean transicionPermitida(
            String estadoActual,
            String nuevoEstado) {

        return switch (estadoActual) {

            case "PENDIENTE" ->
                    nuevoEstado.equals("CONFIRMADA")
                    || nuevoEstado.equals("CANCELADA");

            case "CONFIRMADA" ->
                    nuevoEstado.equals("ATENDIDA")
                    || nuevoEstado.equals("CANCELADA")
                    || nuevoEstado.equals("AUSENTE");

            case "ATENDIDA",
                 "CANCELADA",
                 "AUSENTE" -> false;

            default -> false;
        };
    }

    private String generarCodigoCita() {

        return "CITA-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}