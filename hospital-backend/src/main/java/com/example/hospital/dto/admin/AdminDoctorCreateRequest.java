package com.example.hospital.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDoctorCreateRequest {

    @NotBlank(message = "userPhone不能为空")
    private String userPhone;

    // 不填可默认 123456（由后端处理）
    private String userPassword;

    @NotBlank(message = "hospitalId不能为空")
    private String hospitalId;

    @NotBlank(message = "departmentId不能为空")
    private String departmentId;

    @NotBlank(message = "doctorName不能为空")
    private String doctorName;

    private String doctorGender;

    @NotBlank(message = "doctorIdcard不能为空")
    private String doctorIdcard;

    private String title;

    private String doctorPhone;
    private String doctorEmail;
    private String doctorIntro;
    private String avatarUrl;
}
