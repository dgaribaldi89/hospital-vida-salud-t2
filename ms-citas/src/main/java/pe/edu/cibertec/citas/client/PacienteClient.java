package pe.edu.cibertec.citas.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.citas.bean.dto.PacienteResponse;
import reactor.core.publisher.Mono;

@Component
public class PacienteClient {

    private final WebClient.Builder webClientBuilder;

    public PacienteClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<PacienteResponse> buscarPorId(Long pacienteId) {

        return webClientBuilder
                .build()
                .get()
                .uri(
                    "http://ms-pacientes/pacientes/{id}",
                    pacienteId
                )
                .retrieve()
                .onStatus(
                    status -> status.value() == 404,
                    response -> Mono.error(
                        new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El paciente indicado no existe"
                        )
                    )
                )
                .bodyToMono(PacienteResponse.class);
    }
}