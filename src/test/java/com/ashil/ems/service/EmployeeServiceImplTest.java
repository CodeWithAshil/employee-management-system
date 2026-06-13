package com.ashil.ems.service;

import com.ashil.ems.dto.EmployeeRequest;
import com.ashil.ems.dto.EmployeeResponse;
import com.ashil.ems.entity.Employee;
import com.ashil.ems.exception.DuplicateResourceException;
import com.ashil.ems.exception.ResourceNotFoundException;
import com.ashil.ems.repository.EmployeeRepository;
import com.ashil.ems.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequest request;
    private Employee employee;

    @BeforeEach
    void setUp() {
        request = EmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .department("Engineering")
                .designation("Developer")
                .build();

        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .department("Engineering")
                .designation("Developer")
                .build();
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = employeeService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("John");
    }

    @Test
    void create_shouldThrowWhenEmailExists() {
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(request.getEmail());

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnPresentOptionalWhenFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Optional<EmployeeResponse> result = employeeService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void getById_shouldReturnEmptyOptionalWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(employeeService.getById(99L)).isEmpty();
    }

    @Test
    void getAll_shouldReturnMappedList() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeResponse> result = employeeService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void update_shouldModifyAndReturnResponse() {
        EmployeeRequest updateRequest = EmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .department("HR")
                .designation("Manager")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.update(1L, updateRequest);

        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveWhenFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.delete(1L);

        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).delete(any());
    }
}
