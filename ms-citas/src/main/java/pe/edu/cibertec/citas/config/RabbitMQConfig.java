package pe.edu.cibertec.citas.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE =
            "hospital.citas.exchange";

    public static final String ROUTING_KEY =
            "cita.evento";

    @Bean
    DirectExchange citasExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}