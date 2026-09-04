package com.ifoodclone.auth.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ifoodclone.auth.entity.User;
import com.ifoodclone.auth.service.JwtService;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public OpenTelemetry openTelemetry() {
        OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
        Tracer tracer = mock(Tracer.class);
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        Span span = mock(Span.class);
        Scope scope = mock(Scope.class);

        // Configure the chain of mocks
        when(openTelemetry.getTracer(anyString(), anyString())).thenReturn(tracer);
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(anyString(), any(Long.class))).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(anyString(), any(Boolean.class))).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(anyString(), any(Integer.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.makeCurrent()).thenReturn(scope);
        when(span.addEvent(anyString())).thenReturn(span);
        when(span.addEvent(anyString(), any(java.time.Instant.class))).thenReturn(span);
        when(span.recordException(any(Throwable.class))).thenReturn(span);
        when(span.setAttribute(anyString(), anyString())).thenReturn(span);
        when(span.setAttribute(anyString(), any(Long.class))).thenReturn(span);
        when(span.setAttribute(anyString(), any(Boolean.class))).thenReturn(span);
        when(span.setAttribute(anyString(), any(Integer.class))).thenReturn(span);
        when(span.setStatus(any(StatusCode.class), anyString())).thenReturn(span);
        when(span.setStatus(any(StatusCode.class))).thenReturn(span);

        return openTelemetry;
    }

    @Bean
    @Primary
    public Tracer tracer() {
        return Mockito.mock(Tracer.class);
    }

    // CorrelationIdInterceptor (registered for every request, including in @WebMvcTest
    // slices) depends on io.micrometer.tracing.Tracer - a different type from the
    // io.opentelemetry.api.trace.Tracer mocked above, and not provided by the web slice.
    @Bean
    @Primary
    public io.micrometer.tracing.Tracer micrometerTracer() {
        return Mockito.mock(io.micrometer.tracing.Tracer.class);
    }

    // JwtAuthenticationFilter is a @Component Filter, also auto-detected by @WebMvcTest
    // slices regardless of the controller under test - its constructor deps need mocks too.
    @Bean
    @Primary
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }

    // Pre-stubbed at bean-creation time, not left for each test's @BeforeEach: Spring
    // Security's @WithUserDetails resolves its SecurityContext via a
    // TestExecutionListener that runs before JUnit's @BeforeEach callbacks (documented
    // Spring Security caveat), so a mock stubbed only in @BeforeEach can still be
    // "unstubbed" (returns null) at the moment @WithUserDetails needs it -- this raced
    // reliably on GitHub Actions' runner but not always locally, hence the intermittent
    // "Unable to create SecurityContext" failures in AuthControllerTest's
    // @WithUserDetails("test@example.com") tests.
    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        UserDetailsService mockUserDetailsService = Mockito.mock(UserDetailsService.class);
        User defaultTestUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded-password")
                .role(User.UserRole.CUSTOMER)
                .active(true)
                .build();
        when(mockUserDetailsService.loadUserByUsername(anyString())).thenReturn(defaultTestUser);
        return mockUserDetailsService;
    }
}