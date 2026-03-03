package be.intecbrussel.shoppinglist.configuration;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Serialize LocalDate / LocalDateTime as ISO-8601 strings ("2026-07-15")
     * instead of JSON arrays ([2026, 7, 15]).
     *
     * spring.jackson.serialization.write-dates-as-timestamps=false no longer
     * works in Spring Boot 4.x — configure programmatically instead.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonDateCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}