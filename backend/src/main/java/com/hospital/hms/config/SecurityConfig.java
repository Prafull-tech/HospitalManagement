package com.hospital.hms.config;

import com.hospital.hms.common.logging.RequestCorrelationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security: role-based access for Reception module.
 * ADMIN, RECEPTIONIST, HELP_DESK. In-memory users for demo; replace with DB/LDAP in production.
 *
 * AUTH DISABLED: All requests permitted for development. Re-enable when all modules are ready
 * by uncommenting authenticated() rules and removing permitAll() for /reception/** and anyRequest().
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public RequestCorrelationFilter requestCorrelationFilter() {
        return new RequestCorrelationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(requestCorrelationFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/h2-console/**").permitAll()
                        // Hospital Bed Availability: read allowed without login; write/update/delete protected by @PreAuthorize
                        .requestMatchers("/api/hospitals/**").permitAll()
                        .requestMatchers("/api/admission-priority/**", "/api/ipd/transfers/**").authenticated()
                        // Shift-to-ward: only nursing roles; requires auth then @PreAuthorize NURSE/ADMIN
                        .requestMatchers("/ipd/*/shift-to-ward").authenticated()
                        // Only doctor can recommend admission; endpoint requires auth then @PreAuthorize DOCTOR
                        .requestMatchers("/visit/**").authenticated()
                        // Other modules: permit all until ready
                        .requestMatchers("/patients/**", "/reception/**", "/doctors/**", "/departments/**", "/opd/**", "/emergency/**", "/ipd/**", "/nursing/**", "/wards/**", "/beds/**", "/system/**").permitAll()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> {});
        // Allow H2 console frame in dev
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        UserDetails receptionist = User.builder()
                .username("receptionist")
                .password(encoder.encode("rec123"))
                .roles("RECEPTIONIST")
                .build();
        UserDetails helpdesk = User.builder()
                .username("helpdesk")
                .password(encoder.encode("help123"))
                .roles("HELP_DESK")
                .build();
        UserDetails ipdManager = User.builder()
                .username("ipdmanager")
                .password(encoder.encode("ipd123"))
                .roles("IPD_MANAGER")
                .build();
        UserDetails doctor = User.builder()
                .username("doctor")
                .password(encoder.encode("doc123"))
                .roles("DOCTOR")
                .build();
        UserDetails medicalSuperintendent = User.builder()
                .username("medsuper")
                .password(encoder.encode("ms123"))
                .roles("MEDICAL_SUPERINTENDENT")
                .build();
        UserDetails emergencyHead = User.builder()
                .username("emergencyhead")
                .password(encoder.encode("eh123"))
                .roles("EMERGENCY_HEAD")
                .build();
        UserDetails nurse = User.builder()
                .username("nurse")
                .password(encoder.encode("nurse123"))
                .roles("NURSE")
                .build();
        return new InMemoryUserDetailsManager(admin, receptionist, helpdesk, ipdManager, doctor,
                medicalSuperintendent, emergencyHead, nurse);
    }
}
