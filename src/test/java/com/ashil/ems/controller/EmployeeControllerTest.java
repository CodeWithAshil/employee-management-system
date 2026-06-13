package com.ashil.ems.controller;

import com.ashil.ems.dto.EmployeeRequest;
import com.ashil.ems.dto.EmployeeResponse;
import com.ashil.ems.entity.EmployeeStatus;
import com.ashil.ems.entity.RoleName;
import com.ashil.ems.security.SecurityConfig;
import com.ashil.ems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
@WithMockUser
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private EmployeeResponse sampleResponse() {
        return EmployeeResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+12345678901")
                .salary(new BigDecimal("75000.00"))
                .joiningDate(LocalDate.of(2024, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .roleName(RoleName.EMPLOYEE)
                .departmentName("Engineering")
                .departmentCode("ENG")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private EmployeeRequest validRequest() {
        return EmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+12345678901")
                .salary(new BigDecimal("75000.00"))
                .joiningDate(LocalDate.of(2024, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .roleId(1L)
                .departmentId(1L)
                .build();
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roleName").value("EMPLOYEE"))
                .andExpect(jsonPath("$.departmentCode").value("ENG"));
    }

    @Test
    void create_shouldReturn400WhenInvalid() throws Exception {
        EmployeeRequest invalid = EmployeeRequest.builder()
                .firstName("")
                .lastName("Doe")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        when(employeeService.getById(1L)).thenReturn(Optional.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/employees/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getById_shouldReturn404WhenMissing() throws Exception {
        when(employeeService.getById(eq(99L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/employees/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(employeeService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
