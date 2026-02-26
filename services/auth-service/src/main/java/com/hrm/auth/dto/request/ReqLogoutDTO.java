package com.hrm.auth.dto.request;

import lombok.Data;

@Data
public class ReqLogoutDTO {
    private String refreshToken;
}
