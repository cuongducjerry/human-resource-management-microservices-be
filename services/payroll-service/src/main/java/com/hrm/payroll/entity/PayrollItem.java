package com.hrm.payroll.entity;

import com.hrm.payroll.util.constant.PayrollItemType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "payroll_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID payrollId;

    @Enumerated(EnumType.STRING)
    private PayrollItemType type;

    private String description;

    private Double amount;
}
