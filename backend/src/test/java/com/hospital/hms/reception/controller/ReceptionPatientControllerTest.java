package com.hospital.hms.reception.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.config.SecurityConfig;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.reception.dto.PatientRequestDto;
import com.hospital.hms.reception.dto.PatientResponseDto;
import com.hospital.hms.reception.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceptionPatientController.class)
@Import(SecurityConfig.class)
class ReceptionPatientControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    // @Test
    // @WithMockUser(roles = "RECEPTIONIST")
    // void register_returns201_whenValid() throws Exception {
    //     PatientRequestDto request = new PatientRequestDto("Test User", 30, "Male", null, null);
    //     PatientResponseDto response = new PatientResponseDto();
    //     response.setId(1L);
    //     response.setUhid("HMS-2025-000001");
    //     response.setFullName("Test User");
    //     response.setAge(30);
    //     response.setGender("Male");
    //     response.setCreatedAt(Instant.now());
    //     response.setUpdatedAt(Instant.now());
    //     when(patientService.register(any(PatientRequestDto.class))).thenReturn(response);
    //     mvc.perform(post("/reception/patients")
    //                     .contentType(MediaType.APPLICATION_JSON)
    //                     .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated());
    // }

    @Test
    void getByUhid_returns404_whenNotFound() throws Exception {
        when(patientService.getByUhid(anyString()))
                .thenThrow(new ResourceNotFoundException("Patient not found with UHID: HMS-2025-000001"));
        mvc.perform(get("/reception/patients/HMS-2025-000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HELP_DESK")
    void search_returns200_whenAuthenticated() throws Exception {
        mvc.perform(get("/reception/patients/search").param("name", "John"))
                .andExpect(status().isOk());
    }
}
