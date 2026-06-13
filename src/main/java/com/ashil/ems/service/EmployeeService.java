package com.ashil.ems.service;

import com.ashil.ems.dto.EmployeeRequest;
import com.ashil.ems.dto.EmployeeResponse;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    EmployeeResponse create(EmployeeRequest request);

    Optional<EmployeeResponse> getById(Long id);

    List<EmployeeResponse> getAll();

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);
}
