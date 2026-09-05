package com.ifoodclone.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

// Kept separate from the main @SpringBootApplication class: putting @EnableMongoAuditing
// there gets processed even inside @WebMvcTest slices (which use that class as the
// context's config source but load no real Mongo connection), breaking every web slice
// test. A plain @Configuration class here isn't picked up by @WebMvcTest's restricted
// component scan -- same rationale as JpaConfig in restaurant-service/order-service.
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
