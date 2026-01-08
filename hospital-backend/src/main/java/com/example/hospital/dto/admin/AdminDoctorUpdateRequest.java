package com.example.hospital.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDoctorUpdateRequest {

    @NotBlank(message = "userPhone不能为空")
    private String userPhone;

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
