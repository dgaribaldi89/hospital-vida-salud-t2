package pe.edu.cibertec.citas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pe.edu.cibertec.citas.bean.dto.CitaRequest;
import pe.edu.cibertec.citas.bean.dto.EstadoCitaRequest;
import pe.edu.cibertec.citas.bean.entity.Cita;
import pe.edu.cibertec.citas.usecase.CitaService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public Flux<Cita> listarTodos() {
        return citaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cita>> buscarPorId(
            @PathVariable Long id) {

        return citaService
                .buscarPorId(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/paciente/{pacienteId}")
    public Flux<Cita> listarPorPaciente(
            @PathVariable Long pacienteId) {

        return citaService.listarPorPaciente(pacienteId);
    }

    @PostMapping
    public Mono<ResponseEntity<Cita>> registrar(
            @Valid @RequestBody CitaRequest request) {

        return citaService
                .registrar(request)
                .map(cita ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(cita)
                );
    }

    @PatchMapping("/{id}/estado")
    public Mono<ResponseEntity<Cita>> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoCitaRequest request) {

        return citaService
                .cambiarEstado(id, request)
                .map(ResponseEntity::ok);
    }
}