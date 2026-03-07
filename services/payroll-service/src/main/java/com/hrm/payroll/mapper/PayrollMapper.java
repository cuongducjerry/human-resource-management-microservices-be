package com.hrm.payroll.mapper;

import com.hrm.payroll.dto.response.ResPayrollDTO;
import com.hrm.payroll.entity.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    public ResPayrollDTO toDTO(Payroll payroll) {

        if (payroll == null) {
            return null;
        }

        return ResPayrollDTO.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployeeId())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .baseSalary(payroll.getBaseSalary())
                .allowance(payroll.getAllowance())
                .overtimePay(payroll.getOvertimePay())
                .deduction(payroll.getDeduction())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus())
                .createdAt(payroll.getCreatedAt())
                .updatedAt(payroll.getUpdatedAt())
                .createdBy(payroll.getCreatedBy())
                .updatedBy(payroll.getUpdatedBy())
                .build();
    }

}
