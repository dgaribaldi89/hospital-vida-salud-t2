package pe.edu.cibertec.citas.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.citas.bean.dto.CitaRequest;
import pe.edu.cibertec.citas.bean.dto.EstadoCitaRequest;
import pe.edu.cibertec.citas.bean.dto.PacienteResponse;
import pe.edu.cibertec.citas.bean.entity.Cita;
import pe.edu.cibertec.citas.client.PacienteClient;
import pe.edu.cibertec.citas.persistence.CitaRepository;
import pe.edu.cibertec.citas.publisher.CitaEventoPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteClient pacienteClient;

    @Mock
    private CitaEventoPublisher citaEventoPublisher;

    @Mock
    private CitaRequest citaRequest;

    @Mock
    private EstadoCitaRequest estadoCitaRequest;

    @Mock
    private PacienteResponse pacienteResponse;

    @InjectMocks
    private CitaService citaService;


    @Test
    void buscarPorId_CitaExiste_RetornaCita() {

        Cita cita = Cita.builder()
                .id(1L)
                .codigoCita("CITA-ABC12345")
                .pacienteId(1L)
                .especialidad("Medicina General")
                .fechaHora(LocalDateTime.of(2026, 10, 20, 10, 0))
                .profesionalCodigo("MED-001")
                .motivoAdministrativo("Consulta médica")
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .ultimaActualizacion(LocalDateTime.now())
                .build();

        when(citaRepository.findById(1L))
                .thenReturn(Mono.just(cita));

        StepVerifier.create(citaService.buscarPorId(1L))
                .assertNext(resultado -> {
                    assertEquals(1L, resultado.getId());
                    assertEquals("CITA-ABC12345", resultado.getCodigoCita());
                    assertEquals("PENDIENTE", resultado.getEstado());
                })
                .verifyComplete();
    }


    @Test
    void buscarPorId_CitaNoExiste_RetornaNotFound() {

        when(citaRepository.findById(99L))
                .thenReturn(Mono.empty());

        StepVerifier.create(citaService.buscarPorId(99L))
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.NOT_FOUND,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "Cita no encontrada",
                            excepcion.getReason()
                    );
                })
                .verify();
    }


    @Test
    void registrar_CitaCorrecta_GuardaEnPendiente() {

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 11, 10, 10, 0);

        when(citaRequest.getPacienteId())
                .thenReturn(1L);

        when(citaRequest.getEspecialidad())
                .thenReturn("Cardiología");

        when(citaRequest.getFechaHora())
                .thenReturn(fechaCita);

        when(citaRequest.getProfesionalCodigo())
                .thenReturn("MED-002");

        when(citaRequest.getMotivoAdministrativo())
                .thenReturn("Control cardiológico");

        when(pacienteResponse.getEstado())
                .thenReturn("ACTIVO");

        when(pacienteClient.buscarPorId(1L))
                .thenReturn(Mono.just(pacienteResponse));

        when(
                citaRepository.existsByPacienteIdAndFechaHora(
                        1L,
                        fechaCita
                )
        ).thenReturn(Mono.just(false));

        when(
                citaRepository.existsByProfesionalCodigoAndFechaHora(
                        "MED-002",
                        fechaCita
                )
        ).thenReturn(Mono.just(false));

        when(citaRepository.save(any(Cita.class)))
                .thenAnswer(invocacion -> {

                    Cita cita = invocacion.getArgument(0);
                    cita.setId(2L);

                    return Mono.just(cita);
                });

        StepVerifier.create(
                citaService.registrar(citaRequest)
        )
                .assertNext(resultado -> {

                    assertEquals(2L, resultado.getId());
                    assertEquals(1L, resultado.getPacienteId());
                    assertEquals("Cardiología", resultado.getEspecialidad());
                    assertEquals("MED-002", resultado.getProfesionalCodigo());
                    assertEquals("PENDIENTE", resultado.getEstado());

                    assertTrue(
                            resultado.getCodigoCita()
                                    .startsWith("CITA-")
                    );
                })
                .verifyComplete();

        verify(citaRepository)
                .save(any(Cita.class));
    }


    @Test
    void registrar_PacienteInactivo_RetornaBadRequest() {

        when(citaRequest.getPacienteId())
                .thenReturn(1L);

        when(pacienteResponse.getEstado())
                .thenReturn("INACTIVO");

        when(pacienteClient.buscarPorId(1L))
                .thenReturn(Mono.just(pacienteResponse));

        StepVerifier.create(
                citaService.registrar(citaRequest)
        )
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "El paciente se encuentra inactivo",
                            excepcion.getReason()
                    );
                })
                .verify();

        verify(
                citaRepository,
                never()
        ).save(any(Cita.class));
    }


    @Test
    void registrar_PacienteYaTieneCita_RetornaConflict() {

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 11, 15, 9, 0);

        when(citaRequest.getPacienteId())
                .thenReturn(1L);

        when(citaRequest.getFechaHora())
                .thenReturn(fechaCita);

        when(citaRequest.getProfesionalCodigo())
                .thenReturn("MED-003");

        when(pacienteResponse.getEstado())
                .thenReturn("ACTIVO");

        when(pacienteClient.buscarPorId(1L))
                .thenReturn(Mono.just(pacienteResponse));

        when(
                citaRepository.existsByPacienteIdAndFechaHora(
                        1L,
                        fechaCita
                )
        ).thenReturn(Mono.just(true));

        when(
                citaRepository.existsByProfesionalCodigoAndFechaHora(
                        "MED-003",
                        fechaCita
                )
        ).thenReturn(Mono.just(false));

        StepVerifier.create(
                citaService.registrar(citaRequest)
        )
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.CONFLICT,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "El paciente ya tiene una cita en esa fecha y hora",
                            excepcion.getReason()
                    );
                })
                .verify();

        verify(
                citaRepository,
                never()
        ).save(any(Cita.class));
    }


    @Test
    void registrar_ProfesionalYaTieneCita_RetornaConflict() {

        LocalDateTime fechaCita =
                LocalDateTime.of(2026, 11, 16, 11, 0);

        when(citaRequest.getPacienteId())
                .thenReturn(1L);

        when(citaRequest.getFechaHora())
                .thenReturn(fechaCita);

        when(citaRequest.getProfesionalCodigo())
                .thenReturn("MED-004");

        when(pacienteResponse.getEstado())
                .thenReturn("ACTIVO");

        when(pacienteClient.buscarPorId(1L))
                .thenReturn(Mono.just(pacienteResponse));

        when(
                citaRepository.existsByPacienteIdAndFechaHora(
                        1L,
                        fechaCita
                )
        ).thenReturn(Mono.just(false));

        when(
                citaRepository.existsByProfesionalCodigoAndFechaHora(
                        "MED-004",
                        fechaCita
                )
        ).thenReturn(Mono.just(true));

        StepVerifier.create(
                citaService.registrar(citaRequest)
        )
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.CONFLICT,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "El profesional ya tiene una cita en esa fecha y hora",
                            excepcion.getReason()
                    );
                })
                .verify();

        verify(
                citaRepository,
                never()
        ).save(any(Cita.class));
    }


    @Test
    void cambiarEstado_PendienteAConfirmada_PublicaEvento() {

        Cita cita = Cita.builder()
                .id(3L)
                .codigoCita("CITA-5637F584")
                .pacienteId(1L)
                .especialidad("Cardiología")
                .fechaHora(
                        LocalDateTime.of(
                                2026,
                                10,
                                22,
                                9,
                                0
                        )
                )
                .profesionalCodigo("MED-002")
                .motivoAdministrativo("Control")
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .ultimaActualizacion(LocalDateTime.now())
                .build();

        when(estadoCitaRequest.getEstado())
                .thenReturn("CONFIRMADA");

        when(citaRepository.findById(3L))
                .thenReturn(Mono.just(cita));

        when(citaRepository.save(any(Cita.class)))
                .thenAnswer(invocacion ->
                        Mono.just(invocacion.getArgument(0))
                );

        StepVerifier.create(
                citaService.cambiarEstado(
                        3L,
                        estadoCitaRequest
                )
        )
                .assertNext(resultado -> {
                    assertEquals(
                            "CONFIRMADA",
                            resultado.getEstado()
                    );
                })
                .verifyComplete();

        verify(citaEventoPublisher)
                .publicar(any(Cita.class));
    }


    @Test
    void cambiarEstado_EstadoRepetido_RetornaConflict() {

        Cita cita = Cita.builder()
                .id(4L)
                .codigoCita("CITA-TEST0001")
                .pacienteId(1L)
                .estado("CONFIRMADA")
                .build();

        when(estadoCitaRequest.getEstado())
                .thenReturn("CONFIRMADA");

        when(citaRepository.findById(4L))
                .thenReturn(Mono.just(cita));

        StepVerifier.create(
                citaService.cambiarEstado(
                        4L,
                        estadoCitaRequest
                )
        )
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.CONFLICT,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "La cita ya se encuentra en estado CONFIRMADA",
                            excepcion.getReason()
                    );
                })
                .verify();

        verify(
                citaRepository,
                never()
        ).save(any(Cita.class));

        verify(
                citaEventoPublisher,
                never()
        ).publicar(any(Cita.class));
    }


    @Test
    void cambiarEstado_ConfirmadaAPendiente_RetornaBadRequest() {

        Cita cita = Cita.builder()
                .id(5L)
                .codigoCita("CITA-TEST0002")
                .pacienteId(1L)
                .estado("CONFIRMADA")
                .build();

        when(estadoCitaRequest.getEstado())
                .thenReturn("PENDIENTE");

        when(citaRepository.findById(5L))
                .thenReturn(Mono.just(cita));

        StepVerifier.create(
                citaService.cambiarEstado(
                        5L,
                        estadoCitaRequest
                )
        )
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "No se puede cambiar la cita de CONFIRMADA a PENDIENTE",
                            excepcion.getReason()
                    );
                })
                .verify();

        verify(
                citaRepository,
                never()
        ).save(any(Cita.class));

        verify(
                citaEventoPublisher,
                never()
        ).publicar(any(Cita.class));
    }
}