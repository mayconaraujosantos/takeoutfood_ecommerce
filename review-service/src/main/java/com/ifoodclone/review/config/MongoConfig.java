package com.ifoodclone.review.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

// Kept separate from the main @SpringBootApplication class: putting @EnableMongoAuditing
// there gets processed even inside @WebMvcTest slices (which use that class as the
// context's config source but load no real Mongo connection), same rationale as
// restaurant-service's JpaConfig. A plain @Configuration class here isn't picked up by
// @WebMvcTest's restricted component scan.
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
