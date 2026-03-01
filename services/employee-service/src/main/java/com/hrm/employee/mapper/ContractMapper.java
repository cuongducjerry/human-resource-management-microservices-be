package com.hrm.employee.mapper;

import com.hrm.employee.dto.response.ResContractDTO;
import com.hrm.employee.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ResContractDTO convertToResContractDTO(Contract c) {
        return ResContractDTO.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .type(c.getType())
                .status(c.getStatus())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .salary(c.getSalary())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }

}
