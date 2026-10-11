package pe.edu.cibertec.pacientes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pe.edu.cibertec.pacientes.bean.dto.PacienteRequest;
import pe.edu.cibertec.pacientes.bean.entity.Paciente;
import pe.edu.cibertec.pacientes.usecase.PacienteService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public Flux<Paciente> listarTodos() {
        return pacienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Paciente>> buscarPorId(
            @PathVariable Long id) {

        return pacienteService
                .buscarPorId(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/documento/{tipo}/{numero}")
    public Mono<ResponseEntity<Paciente>> buscarPorDocumento(
            @PathVariable String tipo,
            @PathVariable String numero) {

        return pacienteService
                .buscarPorDocumento(tipo, numero)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Paciente>> registrar(
            @Valid @RequestBody PacienteRequest request) {

        return pacienteService
                .registrar(request)
                .map(paciente ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(paciente)
                );
    }
}