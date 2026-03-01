package com.hrm.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqChangePasswordDTO {

    private String currentPassword;
    private String newPassword;
}
