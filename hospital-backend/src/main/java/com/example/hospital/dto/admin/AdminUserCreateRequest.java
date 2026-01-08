package com.example.hospital.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserCreateRequest {
    @NotBlank(message = "手机号不能为空")
    private String userPhone;

    /** 不传则使用默认密码 */
    private String userPassword;

    /** user/doctor/admin；不传默认 user */
    private String role;
}
