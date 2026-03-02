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

        dto.setOrganizationId(employee.getOrganizationId());
        dto.setPositionId(employee.getPositionId());
        dto.setManagerId(employee.getManagerId());
        dto.setAvatarUrl(employee.getAvatarUrl());

        dto.setHireDate(employee.getHireDate());

        // Lifecycle fields
        dto.setProbationEndDate(employee.getProbationEndDate());
        dto.setConfirmedDate(employee.getConfirmedDate());
        dto.setTerminationDate(employee.getTerminationDate());
        dto.setTerminationReason(employee.getTerminationReason());

        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        dto.setCreatedBy(employee.getCreatedBy());
        dto.setUpdatedBy(employee.getUpdatedBy());

        return dto;
    }

    public ResCreateEmployeeDTO convertToResCreateEmployeeDTO(Employee employee) {

        ResCreateEmployeeDTO dto = new ResCreateEmployeeDTO();

        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());


        dto.setOrganizationId(employee.getOrganizationId());
        dto.setPositionId(employee.getPositionId());
        dto.setManagerId(employee.getManagerId());

        dto.setKeycloakUserId(employee.getKeycloakUserId());

        return dto;
    }
}