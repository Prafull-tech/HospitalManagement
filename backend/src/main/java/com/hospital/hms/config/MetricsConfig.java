package com.hospital.hms.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("hms.auth.login")
                .tag("outcome", "success")
                .description("Successful login attempts")
                .register(registry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry registry) {
        return Counter.builder("hms.auth.login")
                .tag("outcome", "failure")
                .description("Failed login attempts")
                .register(registry);
    }

    @Bean
    public Counter paymentCounter(MeterRegistry registry) {
        return Counter.builder("hms.billing.payments")
                .description("Total payments recorded")
                .register(registry);
    }

    @Bean
    public Counter patientRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("hms.reception.registrations")
                .description("Patient registrations")
                .register(registry);
    }

    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("hms.api.response_time")
                .description("API response time")
                .register(registry);
    }
}
