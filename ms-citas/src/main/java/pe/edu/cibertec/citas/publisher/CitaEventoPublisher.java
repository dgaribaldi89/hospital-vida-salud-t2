package pe.edu.cibertec.citas.publisher;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.citas.bean.entity.Cita;
import pe.edu.cibertec.citas.bean.event.CitaEvento;
import pe.edu.cibertec.citas.config.RabbitMQConfig;

@Component
public class CitaEventoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public CitaEventoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(Cita cita) {

        CitaEvento evento = CitaEvento.builder()
                .citaId(cita.getId())
                .codigoCita(cita.getCodigoCita())
                .pacienteId(cita.getPacienteId())
                .especialidad(cita.getEspecialidad())
                .fechaHora(cita.getFechaHora())
                .profesionalCodigo(cita.getProfesionalCodigo())
                .estado(cita.getEstado())
                .fechaEvento(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                evento
        );
    }
}