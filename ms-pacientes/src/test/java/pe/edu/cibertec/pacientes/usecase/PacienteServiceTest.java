package pe.edu.cibertec.pacientes.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.pacientes.bean.dto.PacienteRequest;
import pe.edu.cibertec.pacientes.bean.entity.Paciente;
import pe.edu.cibertec.pacientes.persistence.PacienteRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PacienteRequest pacienteRequest;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    void buscarPorId_PacienteExiste_RetornaPaciente() {

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
                .fechaRegistro(LocalDate.now())
                .estado("ACTIVO")
                .build();

        when(pacienteRepository.findById(1L))
                .thenReturn(Mono.just(paciente));

        StepVerifier.create(pacienteService.buscarPorId(1L))
                .assertNext(resultado -> {
                    assertEquals(1L, resultado.getId());
                    assertEquals("45879632", resultado.getNumeroDocumento());
                    assertEquals("Juan Carlos", resultado.getNombres());
                    assertEquals("ACTIVO", resultado.getEstado());
                })
                .verifyComplete();
    }

    @Test
    void buscarPorId_PacienteNoExiste_RetornaNotFound() {

        when(pacienteRepository.findById(99L))
                .thenReturn(Mono.empty());

        StepVerifier.create(pacienteService.buscarPorId(99L))
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.NOT_FOUND,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "Paciente no encontrado",
                            excepcion.getReason()
                    );
                })
                .verify();
    }

    @Test
    void buscarPorDocumento_PacienteExiste_RetornaPaciente() {

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
                .fechaRegistro(LocalDate.now())
                .estado("ACTIVO")
                .build();

        when(
                pacienteRepository.findByTipoDocumentoAndNumeroDocumento(
                        "DNI",
                        "45879632"
                )
        ).thenReturn(Mono.just(paciente));

        StepVerifier.create(
                pacienteService.buscarPorDocumento(
                        "DNI",
                        "45879632"
                )
        )
                .assertNext(resultado -> {
                    assertEquals("DNI", resultado.getTipoDocumento());
                    assertEquals("45879632", resultado.getNumeroDocumento());
                    assertEquals("Juan Carlos", resultado.getNombres());
                })
                .verifyComplete();
    }

    @Test
    void registrar_PacienteNuevo_GuardaCorrectamente() {

        when(pacienteRequest.getTipoDocumento())
                .thenReturn("DNI");

        when(pacienteRequest.getNumeroDocumento())
                .thenReturn("87654321");

        when(pacienteRequest.getNombres())
                .thenReturn("Maria Elena");

        when(pacienteRequest.getApellidos())
                .thenReturn("Lopez Torres");

        when(pacienteRequest.getFechaNacimiento())
                .thenReturn(LocalDate.of(1995, 8, 20));

        when(pacienteRequest.getTelefono())
                .thenReturn("987654321");

        when(pacienteRequest.getCorreo())
                .thenReturn("maria.lopez@gmail.com");

        when(pacienteRequest.getDireccion())
                .thenReturn("Lima");

        when(
                pacienteRepository
                        .existsByTipoDocumentoAndNumeroDocumento(
                                "DNI",
                                "87654321"
                        )
        ).thenReturn(Mono.just(false));

        Paciente pacienteGuardado = Paciente.builder()
                .id(2L)
                .tipoDocumento("DNI")
                .numeroDocumento("87654321")
                .nombres("Maria Elena")
                .apellidos("Lopez Torres")
                .fechaNacimiento(LocalDate.of(1995, 8, 20))
                .telefono("987654321")
                .correo("maria.lopez@gmail.com")
                .direccion("Lima")
                .fechaRegistro(LocalDate.now())
                .estado("ACTIVO")
                .build();

        when(pacienteRepository.save(any(Paciente.class)))
                .thenReturn(Mono.just(pacienteGuardado));

        StepVerifier.create(
                pacienteService.registrar(pacienteRequest)
        )
                .assertNext(resultado -> {

                    assertEquals(2L, resultado.getId());
                    assertEquals("DNI", resultado.getTipoDocumento());
                    assertEquals("87654321", resultado.getNumeroDocumento());
                    assertEquals("Maria Elena", resultado.getNombres());
                    assertEquals("ACTIVO", resultado.getEstado());
                })
                .verifyComplete();

        verify(pacienteRepository)
                .save(any(Paciente.class));
    }

    @Test
    void registrar_DocumentoDuplicado_RetornaConflict() {

        when(pacienteRequest.getTipoDocumento())
                .thenReturn("DNI");

        when(pacienteRequest.getNumeroDocumento())
                .thenReturn("45879632");

        when(
                pacienteRepository
                        .existsByTipoDocumentoAndNumeroDocumento(
                                "DNI",
                                "45879632"
                        )
        ).thenReturn(Mono.just(true));

        StepVerifier.create(
                pacienteService.registrar(pacienteRequest)
        )
                .expectErrorSatisfies(error -> {

                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.CONFLICT,
                            excepcion.getStatusCode()
                    );

                    assertEquals(
                            "Ya existe un paciente con ese tipo y número de documento",
                            excepcion.getReason()
                    );
                })
                .verify();

        verify(
                pacienteRepository,
                never()
        ).save(any(Paciente.class));
    }
}