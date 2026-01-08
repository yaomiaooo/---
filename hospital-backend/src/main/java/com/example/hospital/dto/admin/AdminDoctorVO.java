package com.example.hospital.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminDoctorVO {
    private String doctorId;
    private String userId;

    private String userPhone;

    private String hospitalId;
    private String departmentId;

    private String doctorName;
    private String doctorGender;
    private String doctorIdcard;
    private String title;

    private String doctorPhone;
    private String doctorEmail;
    private String doctorIntro;
    private String avatarUrl;

    // 可取 User.createdAt（User 实体存在 createdAt :contentReference[oaicite:6]{index=6}）
    private LocalDateTime createdAt;
}
