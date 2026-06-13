package com.ashil.ems.util;

import com.ashil.ems.dto.EmployeeRequest;
import com.ashil.ems.dto.EmployeeResponse;
import com.ashil.ems.entity.Employee;

/**
 * Maps between Employee entity and its DTOs.
 */
public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static Employee toEntity(EmployeeRequest request) {
        return Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .build();
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .build();
    }
}
