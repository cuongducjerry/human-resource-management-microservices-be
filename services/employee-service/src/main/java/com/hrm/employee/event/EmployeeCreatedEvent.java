package com.hrm.employee.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeCreatedEvent {
    private final UUID employeeId;
}
