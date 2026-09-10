package com.ifoodclone.payment.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Not @LoadBalanced -- the payment gateway (Node-RED) isn't a Eureka client, it's a
    // fixed docker-compose hostname:port, unlike this service's peers in order-service's
    // RestTemplateConfig.
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
