package pe.edu.cibertec.pacientes.usecase;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.pacientes.bean.dto.PacienteRequest;
import pe.edu.cibertec.pacientes.bean.entity.Paciente;
import pe.edu.cibertec.pacientes.persistence.PacienteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public Flux<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public Mono<Paciente> buscarPorId(Long id) {

        return pacienteRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Paciente no encontrado"
                                )
                        )
                );
    }

    public Mono<Paciente> buscarPorDocumento(
            String tipoDocumento,
            String numeroDocumento) {

        return pacienteRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        tipoDocumento,
                        numeroDocumento
                )
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Paciente no encontrado"
                                )
                        )
                );
    }

    public Mono<Paciente> registrar(PacienteRequest request) {

        return pacienteRepository
                .existsByTipoDocumentoAndNumeroDocumento(
                        request.getTipoDocumento(),
                        request.getNumeroDocumento()
                )
                .flatMap(existe -> {

                    if (existe) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Ya existe un paciente con ese tipo y número de documento"
                                )
                        );
                    }

                    Paciente paciente = Paciente.builder()
                            .tipoDocumento(request.getTipoDocumento())
                            .numeroDocumento(request.getNumeroDocumento())
                            .nombres(request.getNombres())
                            .apellidos(request.getApellidos())
                            .fechaNacimiento(request.getFechaNacimiento())
                            .telefono(request.getTelefono())
                            .correo(request.getCorreo())
                            .direccion(request.getDireccion())
                            .fechaRegistro(LocalDate.now())
                            .estado("ACTIVO")
                            .build();

                    return pacienteRepository.save(paciente);
                });
    }
}