package com.hrm.authservice.dto;

import lombok.Data;

@Data
public class ReqLogin {
    private String username;
    private String password;
}
