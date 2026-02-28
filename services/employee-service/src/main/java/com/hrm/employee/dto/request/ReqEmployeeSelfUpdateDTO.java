package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReqEmployeeSelfUpdateDTO {

    @NotBlank(message = "fullName is required")
    private String fullName;

    private String phone;

    private String address;

    private LocalDate dateOfBirth;

    private Gender gender;
}
