package com.hrm.auth.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReqCreateKeycloakUserDTO {

    private String username;
    private String email;
    private String password;
    private List<String> roles;
}
