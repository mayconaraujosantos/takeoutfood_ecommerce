package com.ifoodclone.review.config;

import java.io.IOException;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import com.ifoodclone.review.config.GatewayUserContext.UserContext;

@Configuration
public class RestTemplateConfig {

    // @LoadBalanced resolves logical Eureka service names (e.g. "http://restaurant-service")
    // to an actual instance -- same pattern as order-service's RestTemplateConfig.
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.additionalInterceptors(new UserContextForwardingInterceptor()).build();
    }

    // restaurant-service trusts the same X-User-* headers the API Gateway injects, but
    // this call bypasses the gateway entirely, so this service forwards the current
    // request's identity itself.
    static class UserContextForwardingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            Long userId = UserContext.getUserId();
            if (userId != null) {
                request.getHeaders().add("X-User-Id", String.valueOf(userId));
                request.getHeaders().add("X-User-Email", UserContext.getUserEmail());
                request.getHeaders().add("X-User-Roles", UserContext.getUserRoles());
                request.getHeaders().add("X-Authenticated", "true");
            }
            return execution.execute(request, body);
        }
    }
}
