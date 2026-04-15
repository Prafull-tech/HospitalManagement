package com.hospital.hms.auth;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        AppUser admin = new AppUser();
        admin.setUsername("testadmin");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setFullName("Test Admin");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
    }

    @Test
    @DisplayName("POST /auth/login - valid credentials returns token")
    void loginWithValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testadmin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.expiresAt", notNullValue()))
                .andExpect(jsonPath("$.sessionExpiresAt", notNullValue()))
                .andExpect(jsonPath("$.username").value("testadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login - invalid password returns 401")
    void loginWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testadmin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/me - without token returns 401")
    void meWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/refresh - with valid refresh token returns new tokens")
    void refreshWithValidToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testadmin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken = loginJson.get("refreshToken").asText();
        String sessionExpiresAt = loginJson.get("sessionExpiresAt").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.sessionExpiresAt").value(sessionExpiresAt));
    }

    @Test
    @DisplayName("POST /auth/refresh - with invalid refresh token returns 401")
    void refreshWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-token\"}"))
                .andExpect(status().isUnauthorized());
    }

        @Test
        @DisplayName("POST /auth/logout - invalidates existing access token")
        void logoutInvalidatesExistingAccessToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testadmin\",\"password\":\"Admin123!\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("token").asText();

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isUnauthorized());
        }
}
