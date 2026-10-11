package pe.edu.cibertec.notificaciones.consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import pe.edu.cibertec.notificaciones.bean.dto.CitaEvento;
import pe.edu.cibertec.notificaciones.config.RabbitMQConfig;

public class CitaEventoListenerTest {

    @Test
    void recibirEvento_MuestraNotificacionCorrectamente() {

        CitaEvento evento = new CitaEvento();

        evento.setCitaId(4L);
        evento.setCodigoCita("CITA-90390B4B");
        evento.setPacienteId(1L);
        evento.setEspecialidad("Pediatría");
        evento.setFechaHora(
                LocalDateTime.of(2026, 10, 23, 10, 0)
        );
        evento.setProfesionalCodigo("MED-003");
        evento.setEstado("CONFIRMADA");
        evento.setFechaEvento(
                LocalDateTime.of(2026, 10, 11, 8, 52, 37)
        );

        CitaEventoListener listener =
                new CitaEventoListener();

        PrintStream salidaOriginal = System.out;

        ByteArrayOutputStream salidaCapturada =
                new ByteArrayOutputStream();

        System.setOut(
                new PrintStream(salidaCapturada)
        );

        try {

            listener.recibirEvento(evento);

        } finally {

            System.setOut(salidaOriginal);
        }

        String resultado =
                salidaCapturada.toString();

        assertTrue(
                resultado.contains("NOTIFICACION DE CITA")
        );

        assertTrue(
                resultado.contains("Cita ID: 4")
        );

        assertTrue(
                resultado.contains("Codigo: CITA-90390B4B")
        );

        assertTrue(
                resultado.contains("Paciente ID: 1")
        );

        assertTrue(
                resultado.contains("Especialidad: Pediatría")
        );

        assertTrue(
                resultado.contains(
                        "Fecha y hora: 2026-10-23T10:00"
                )
        );

        assertTrue(
                resultado.contains(
                        "Profesional: MED-003"
                )
        );

        assertTrue(
                resultado.contains(
                        "Estado: CONFIRMADA"
                )
        );
    }


    @Test
    void recibirEvento_EstaConfiguradoConRabbitListener() throws Exception {

        Method metodo =
                CitaEventoListener.class.getMethod(
                        "recibirEvento",
                        CitaEvento.class
                );

        RabbitListener anotacion =
                metodo.getAnnotation(
                        RabbitListener.class
                );

        assertNotNull(anotacion);

        assertArrayEquals(
                new String[] {
                        RabbitMQConfig.QUEUE
                },
                anotacion.queues()
        );
    }
}