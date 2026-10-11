package pe.edu.cibertec.notificaciones.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.notificaciones.bean.dto.CitaEvento;
import pe.edu.cibertec.notificaciones.config.RabbitMQConfig;

@Component
public class CitaEventoListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void recibirEvento(CitaEvento evento) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("       NOTIFICACION DE CITA");
        System.out.println("========================================");
        System.out.println("Cita ID: " + evento.getCitaId());
        System.out.println("Codigo: " + evento.getCodigoCita());
        System.out.println("Paciente ID: " + evento.getPacienteId());
        System.out.println("Especialidad: " + evento.getEspecialidad());
        System.out.println("Fecha y hora: " + evento.getFechaHora());
        System.out.println("Profesional: " + evento.getProfesionalCodigo());
        System.out.println("Estado: " + evento.getEstado());
        System.out.println("Fecha del evento: " + evento.getFechaEvento());
        System.out.println("========================================");
        System.out.println();
    }
}