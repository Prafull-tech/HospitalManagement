package com.hospital.hms.enquiry.controller;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.enquiry.dto.EnquiryRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryResponseDto;
import com.hospital.hms.enquiry.entity.EnquiryCategory;
import com.hospital.hms.enquiry.entity.EnquiryPriority;
import com.hospital.hms.enquiry.service.EnquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.auth.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnquiryController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnquiryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnquiryService enquiryService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        when(enquiryService.getById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Enquiry not found: 99"));

        mvc.perform(get("/enquiries/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201WhenValid() throws Exception {
        EnquiryRequestDto request = new EnquiryRequestDto();
        request.setCategory(EnquiryCategory.GENERAL_ENQUIRY);
        request.setPriority(EnquiryPriority.MEDIUM);
        request.setSubject("Visiting hours");
        request.setDescription("Need visiting hours for ward.");
        request.setEnquirerName("Test User");
        request.setPhone("9999999999");

        EnquiryResponseDto response = new EnquiryResponseDto();
        response.setId(1L);
        response.setEnquiryNo("ENQ-20260411-1001");
        response.setSubject(request.getSubject());

        when(enquiryService.create(any(EnquiryRequestDto.class))).thenReturn(response);

        mvc.perform(post("/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
