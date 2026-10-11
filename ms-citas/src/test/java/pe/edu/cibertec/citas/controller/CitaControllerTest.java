package pe.edu.cibertec.citas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.citas.bean.dto.CitaRequest;
import pe.edu.cibertec.citas.bean.dto.EstadoCitaRequest;
import pe.edu.cibertec.citas.bean.entity.Cita;
import pe.edu.cibertec.citas.usecase.CitaService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaService citaService;

    @InjectMocks
    private CitaController citaController;

    private WebTestClient webTestClient;

    @BeforeEach
    void configurar() {

        webTestClient = WebTestClient
                .bindToController(citaController)
                .build();
    }

    @Test
    void listarCitas_Retorna200YLista() {

        Cita cita1 = Cita.builder()
                .id(1L)
                .codigoCita("CITA-001")
                .pacienteId(1L)
                .especialidad("Medicina General")
                .fechaHora(LocalDateTime.of(2026, 10, 20, 10, 0))
                .profesionalCodigo("MED-001")
                .motivoAdministrativo("Consulta programada")
                .estado("CONFIRMADA")
                .fechaCreacion(LocalDateTime.of(2026, 10, 11, 8, 0))
                .ultimaActualizacion(LocalDateTime.of(2026, 10, 11, 8, 30))
                .build();

        Cita cita2 = Cita.builder()
                .id(2L)
                .codigoCita("CITA-002")
                .pacienteId(1L)
                .especialidad("Cardiología")
                .fechaHora(LocalDateTime.of(2026, 10, 22, 9, 0))
                .profesionalCodigo("MED-002")
                .motivoAdministrativo("Control administrativo")
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.of(2026, 10, 11, 9, 0))
                .ultimaActualizacion(LocalDateTime.of(2026, 10, 11, 9, 0))
                .build();

        when(citaService.listarTodos())
                .thenReturn(Flux.just(cita1, cita2));

        webTestClient.get()
                .uri("/citas")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].especialidad").isEqualTo("Medicina General")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].especialidad").isEqualTo("Cardiología");
    }

    @Test
    void buscarCitaPorId_Existe_Retorna200() {

        Cita cita = Cita.builder()
                .id(3L)
                .codigoCita("CITA-5637F584")
                .pacienteId(1L)
                .especialidad("Cardiología")
                .fechaHora(LocalDateTime.of(2026, 10, 22, 9, 0))
                .profesionalCodigo("MED-002")
                .motivoAdministrativo("Consulta")
                .estado("CONFIRMADA")
                .fechaCreacion(LocalDateTime.of(2026, 10, 11, 8, 0))
                .ultimaActualizacion(LocalDateTime.of(2026, 10, 11, 8, 30))
                .build();

        when(citaService.buscarPorId(3L))
                .thenReturn(Mono.just(cita));

        webTestClient.get()
                .uri("/citas/3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.codigoCita").isEqualTo("CITA-5637F584")
                .jsonPath("$.pacienteId").isEqualTo(1)
                .jsonPath("$.estado").isEqualTo("CONFIRMADA");
    }

    @Test
    void buscarCitaPorId_NoExiste_Retorna404() {

        when(citaService.buscarPorId(99L))
                .thenReturn(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cita no encontrada"
                                )
                        )
                );

        webTestClient.get()
                .uri("/citas/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listarCitasPorPaciente_Retorna200() {

        Cita cita = Cita.builder()
                .id(4L)
                .codigoCita("CITA-90390B4B")
                .pacienteId(1L)
                .especialidad("Pediatría")
                .fechaHora(LocalDateTime.of(2026, 10, 23, 10, 0))
                .profesionalCodigo("MED-003")
                .motivoAdministrativo("Consulta")
                .estado("CONFIRMADA")
                .fechaCreacion(LocalDateTime.of(2026, 10, 11, 8, 0))
                .ultimaActualizacion(LocalDateTime.of(2026, 10, 11, 8, 30))
                .build();

        when(citaService.listarPorPaciente(1L))
                .thenReturn(Flux.just(cita));

        webTestClient.get()
                .uri("/citas/paciente/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].pacienteId").isEqualTo(1)
                .jsonPath("$[0].especialidad").isEqualTo("Pediatría");
    }

    @Test
    void registrarCita_DatosValidos_Retorna201() {

        Cita citaGuardada = Cita.builder()
                .id(5L)
                .codigoCita("CITA-ABC12345")
                .pacienteId(1L)
                .especialidad("Traumatología")
                .fechaHora(LocalDateTime.of(2026, 10, 25, 11, 0))
                .profesionalCodigo("MED-004")
                .motivoAdministrativo("Consulta programada")
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.of(2026, 10, 11, 10, 0))
                .ultimaActualizacion(LocalDateTime.of(2026, 10, 11, 10, 0))
                .build();

        when(citaService.registrar(any(CitaRequest.class)))
                .thenReturn(Mono.just(citaGuardada));

        String json = """
                {
                    "pacienteId": 1,
                    "especialidad": "Traumatología",
                    "fechaHora": "2026-10-25T11:00:00",
                    "profesionalCodigo": "MED-004",
                    "motivoAdministrativo": "Consulta programada"
                }
                """;

        webTestClient.post()
                .uri("/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(5)
                .jsonPath("$.pacienteId").isEqualTo(1)
                .jsonPath("$.especialidad").isEqualTo("Traumatología")
                .jsonPath("$.estado").isEqualTo("PENDIENTE");
    }

    @Test
    void cambiarEstadoCita_Retorna200() {

        Cita citaConfirmada = Cita.builder()
                .id(5L)
                .codigoCita("CITA-ABC12345")
                .pacienteId(1L)
                .especialidad("Traumatología")
                .fechaHora(LocalDateTime.of(2026, 10, 25, 11, 0))
                .profesionalCodigo("MED-004")
                .motivoAdministrativo("Consulta programada")
                .estado("CONFIRMADA")
                .fechaCreacion(LocalDateTime.of(2026, 10, 11, 10, 0))
                .ultimaActualizacion(LocalDateTime.of(2026, 10, 11, 10, 30))
                .build();

        when(
                citaService.cambiarEstado(
                        eq(5L),
                        any(EstadoCitaRequest.class)
                )
        ).thenReturn(Mono.just(citaConfirmada));

        String json = """
                {
                    "estado": "CONFIRMADA"
                }
                """;

        webTestClient.patch()
                .uri("/citas/5/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(5)
                .jsonPath("$.estado").isEqualTo("CONFIRMADA");
    }
}