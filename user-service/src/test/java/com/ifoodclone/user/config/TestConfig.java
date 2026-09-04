package com.ifoodclone.user.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// CorrelationIdInterceptor (registered for every request, including in @WebMvcTest slices)
// needs a io.micrometer.tracing.Tracer bean the web slice doesn't provide on its own.
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public io.micrometer.tracing.Tracer micrometerTracer() {
        return Mockito.mock(io.micrometer.tracing.Tracer.class);
    }
}
