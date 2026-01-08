package com.example.hospital.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    @NotBlank(message = "手机号不能为空")
    private String userPhone;

    /** user/doctor/admin */
    private String role;
}
