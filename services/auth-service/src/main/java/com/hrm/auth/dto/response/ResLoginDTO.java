package com.hrm.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResLoginDTO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserAccount user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserAccount {
        private String employeeId;
        private String fullName;
        private String email;
        private List<String> roles;
        private String avatarUrl;
    }
}
