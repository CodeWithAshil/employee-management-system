package com.ashil.ems.service.impl;

import com.ashil.ems.dto.EmployeeRequest;
import com.ashil.ems.dto.EmployeeResponse;
import com.ashil.ems.entity.Employee;
import com.ashil.ems.exception.DuplicateResourceException;
import com.ashil.ems.exception.ResourceNotFoundException;
import com.ashil.ems.repository.EmployeeRepository;
import com.ashil.ems.service.EmployeeService;
import com.ashil.ems.util.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        log.info("Creating employee with email {}", request.getEmail());
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee already exists with email: " + request.getEmail());
        }
        Employee saved = employeeRepository.save(EmployeeMapper.toEntity(request));
        return EmployeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> getById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        log.info("Updating employee {}", id);
        Employee employee = findEmployee(id);
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting employee {}", id);
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }
}
