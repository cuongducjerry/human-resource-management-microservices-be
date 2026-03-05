package com.hrm.auth.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReqCreateKeycloakUserDTO {

    private UUID employeeId;

    private String username;
    private String email;
    private String password;

    private String firstName;
    private String lastName;

    private List<String> roles;
}