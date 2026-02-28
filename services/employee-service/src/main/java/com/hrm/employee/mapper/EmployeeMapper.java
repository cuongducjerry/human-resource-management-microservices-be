package com.hrm.employee.mapper;

import com.hrm.employee.dto.response.ResCreateEmployeeDTO;
import com.hrm.employee.dto.response.ResEmployeeDTO;
import com.hrm.employee.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public ResEmployeeDTO convertToResEmployeeDTO(Employee employee) {

        ResEmployeeDTO dto = new ResEmployeeDTO();

        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setGender(employee.getGender());
        dto.setStatus(employee.getStatus());
        dto.setDepartmentId(employee.getDepartmentId());
        dto.setPositionId(employee.getPositionId());
        dto.setHireDate(employee.getHireDate());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());

        return dto;
    }

    public ResCreateEmployeeDTO convertToResCreateEmployeeDTO(Employee employee) {

        ResCreateEmployeeDTO dto = new ResCreateEmployeeDTO();

        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setDepartmentId(employee.getDepartmentId());
        dto.setPositionId(employee.getPositionId());
        dto.setKeycloakUserId(employee.getKeycloakUserId());

        return dto;
    }
}