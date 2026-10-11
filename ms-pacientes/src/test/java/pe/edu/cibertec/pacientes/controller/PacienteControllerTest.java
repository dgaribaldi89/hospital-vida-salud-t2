package pe.edu.cibertec.pacientes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.cibertec.pacientes.bean.dto.PacienteRequest;
import pe.edu.cibertec.pacientes.bean.entity.Paciente;
import pe.edu.cibertec.pacientes.usecase.PacienteService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private PacienteController pacienteController;

    private WebTestClient webTestClient;

    @BeforeEach
    void configurar() {
        webTestClient = WebTestClient
                .bindToController(pacienteController)
                .build();
    }

    @Test
    void listarPacientes_Retorna200YLista() {

        Paciente paciente1 = Paciente.builder()
                .id(1L)
                .tipoDocumento("DNI")
                .numeroDocumento("45879632")
                .nombres("Juan Carlos")
                .apellidos("Perez Gomez")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .telefono("999888777")
                .correo("juan.perez@gmail.com")
                .direccion("Lima")
                .fechaRegistro(LocalDate.of(2026, 10, 11))
                .estado("ACTIVO")
                .build();

        Paciente paciente2 = Paciente.builder()
                .id(2L)
                .tipoDocumento("DNI")
                .numeroDocumento("74125896")
                .nombres("Maria Elena")
                .apellidos("Torres Ruiz")
                .fechaNacimiento(LocalDate.of(1995, 8, 20))
                .telefono("988777666")
                .correo("maria.torres@gmail.com")
                .direccion("Lima")
                .fechaRegistro(LocalDate.of(2026, 10, 11))
                .estado("ACTIVO")
                .build();

        when(pacienteService.listarTodos())
                .thenReturn(Flux.just(paciente1, paciente2));

        webTestClient.get()
                .uri("/pacientes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].nombres").isEqualTo("Juan Carlos")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].nombres").isEqualTo("Maria Elena");
    }

    @Test
    void buscarPacientePorId_Existe_Retorna200() {

        Paciente paciente = Paciente.builder()
                .id(1L)
                .tipoDocumento("DNI")
                .numeroDocumento("45879632")
                .nombres("Juan Carlos")
                .apellidos("Perez Gomez")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .telefono("999888777")
                .correo("juan.perez@gmail.com")
                .direccion("Lima")
                .fechaRegistro(LocalDate.of(2026, 10, 11))
                .estado("ACTIVO")
                .build();

        when(pacienteService.buscarPorId(1L))
                .thenReturn(Mono.just(paciente));

        webTestClient.get()
                .uri("/pacientes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.numeroDocumento").isEqualTo("45879632")
                .jsonPath("$.nombres").isEqualTo("Juan Carlos")
                .jsonPath("$.estado").isEqualTo("ACTIVO");
    }

    @Test
    void buscarPacientePorId_NoExiste_Retorna404() {

        when(pacienteService.buscarPorId(99L))
                .thenReturn(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Paciente no encontrado"
                                )
                        )
                );

        webTestClient.get()
                .uri("/pacientes/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void buscarPacientePorDocumento_Retorna200() {

        Paciente paciente = Paciente.builder()
                .id(1L)
                .tipoDocumento("DNI")
                .numeroDocumento("45879632")
                .nombres("Juan Carlos")
                .apellidos("Perez Gomez")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .telefono("999888777")
                .correo("juan.perez@gmail.com")
                .direccion("Lima")
                .fechaRegistro(LocalDate.of(2026, 10, 11))
                .estado("ACTIVO")
                .build();

        when(
                pacienteService.buscarPorDocumento(
                        "DNI",
                        "45879632"
                )
        ).thenReturn(Mono.just(paciente));

        webTestClient.get()
                .uri("/pacientes/documento/DNI/45879632")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.tipoDocumento").isEqualTo("DNI")
                .jsonPath("$.numeroDocumento").isEqualTo("45879632");
    }

    @Test
    void registrarPaciente_DatosValidos_Retorna201() {

        Paciente pacienteGuardado = Paciente.builder()
                .id(2L)
                .tipoDocumento("DNI")
                .numeroDocumento("74125896")
                .nombres("Maria Elena")
                .apellidos("Torres Ruiz")
                .fechaNacimiento(LocalDate.of(1995, 8, 20))
                .telefono("988777666")
                .correo("maria.torres@gmail.com")
                .direccion("Lima")
                .fechaRegistro(LocalDate.of(2026, 10, 11))
                .estado("ACTIVO")
                .build();

        when(pacienteService.registrar(any(PacienteRequest.class)))
                .thenReturn(Mono.just(pacienteGuardado));

        String json = """
                {
                    "tipoDocumento": "DNI",
                    "numeroDocumento": "74125896",
                    "nombres": "Maria Elena",
                    "apellidos": "Torres Ruiz",
                    "fechaNacimiento": "1995-08-20",
                    "telefono": "988777666",
                    "correo": "maria.torres@gmail.com",
                    "direccion": "Lima"
                }
                """;

        webTestClient.post()
                .uri("/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(2)
                .jsonPath("$.numeroDocumento").isEqualTo("74125896")
                .jsonPath("$.nombres").isEqualTo("Maria Elena")
                .jsonPath("$.estado").isEqualTo("ACTIVO");
    }
}