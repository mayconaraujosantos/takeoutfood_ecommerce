package com.ifoodclone.delivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Kept separate from the main @SpringBootApplication class: putting @EnableJpaAuditing
// there gets processed even inside @WebMvcTest slices (which use that class as the
// context's config source but load no real JPA/datasource), breaking every web slice
// test with "JPA metamodel must not be empty". A plain @Configuration class here isn't
// picked up by @WebMvcTest's restricted component scan.
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
