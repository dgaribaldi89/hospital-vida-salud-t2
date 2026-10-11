package pe.edu.cibertec.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE =
            "hospital.citas.exchange";

    public static final String QUEUE =
            "hospital.notificaciones.queue";

    public static final String ROUTING_KEY =
            "cita.evento";

    @Bean
    Queue notificacionesQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    DirectExchange citasExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    Binding notificacionesBinding(
            Queue notificacionesQueue,
            DirectExchange citasExchange) {

        return BindingBuilder
                .bind(notificacionesQueue)
                .to(citasExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}