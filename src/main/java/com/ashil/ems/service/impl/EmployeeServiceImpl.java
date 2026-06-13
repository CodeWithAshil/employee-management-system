package com.ashil.ems.service.impl;

import com.ashil.ems.dto.EmployeeRequest;
import com.ashil.ems.dto.EmployeeResponse;
import com.ashil.ems.entity.Department;
import com.ashil.ems.entity.Employee;
import com.ashil.ems.entity.Role;
import com.ashil.ems.exception.DuplicateResourceException;
import com.ashil.ems.exception.ResourceNotFoundException;
import com.ashil.ems.repository.DepartmentRepository;
import com.ashil.ems.repository.EmployeeRepository;
import com.ashil.ems.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        log.info("Creating employee with email {}", request.getEmail());
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee already exists with email: " + request.getEmail());
        }
        Role role = findRole(request.getRoleId());
        Department department = findDepartment(request.getDepartmentId());
        Employee saved = employeeRepository.save(EmployeeMapper.toEntity(request, role, department));
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
        Role role = findRole(request.getRoleId());
        Department department = findDepartment(request.getDepartmentId());

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setSalary(request.getSalary());
        employee.setJoiningDate(request.getJoiningDate());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        employee.setRole(role);
        employee.setDepartment(department);
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

    private Role findRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }
}
