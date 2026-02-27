package com.hrm.employee.service;

import com.hrm.employee.client.AuthClient;
import com.hrm.employee.dto.request.ReqCreateEmployee;
import com.hrm.employee.dto.request.ReqCreateKeycloakUserDTO;
import com.hrm.employee.dto.response.ResCreateEmployee;
import com.hrm.employee.entity.Employee;
import com.hrm.employee.repository.EmployeeRepository;

import com.hrm.employee.util.constant.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuthClient authClient;

    @Transactional
    public ResCreateEmployee createEmployee(ReqCreateEmployee req) {

        String keycloakUserId = null;

        try {
            // 1. Create user on Keycloak
            ReqCreateKeycloakUserDTO userReq = new ReqCreateKeycloakUserDTO();
            userReq.setUsername(req.getEmail());
            userReq.setEmail(req.getEmail());
            userReq.setPassword("123456");
            userReq.setRoles(req.getRoles());

            keycloakUserId = authClient.createUser(userReq);

            // 2. Save employee
            Employee employee = new Employee();
            employee.setKeycloakUserId(keycloakUserId);
            employee.setEmployeeCode(generateEmployeeCode());
            employee.setFullName(req.getFullName());
            employee.setEmail(req.getEmail());
            employee.setDepartmentId(req.getDepartmentId());
            employee.setPositionId(req.getPositionId());
            employee.setStatus(EmployeeStatus.ACTIVE);

            employeeRepository.save(employee);

            // 3. Return response
            return ResCreateEmployee.builder()
                    .id(employee.getId())
                    .employeeCode(employee.getEmployeeCode())
                    .fullName(employee.getFullName())
                    .email(employee.getEmail())
                    .departmentId(employee.getDepartmentId())
                    .positionId(employee.getPositionId())
                    .keycloakUserId(employee.getKeycloakUserId())
                    .build();

        } catch (Exception e) {

            if (keycloakUserId != null) {
                authClient.deleteUser(keycloakUserId);
            }

            throw e;
        }
    }

    private String generateEmployeeCode() {
        long count = employeeRepository.count() + 1;
        return "EMP" + String.format("%04d", count);
    }
}
