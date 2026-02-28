package com.hrm.employee.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ReqCreateKeycloakUserDTO {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
