package com.example.hospital.dto.admin;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员 - 预约订单返回 VO
 * 说明：字段给得“宽松一些”，前端要显示什么列都能直接拿
 */
@Data
public class AdminAppointmentVO {

    private String appointmentId;
    private String status;
    private LocalDateTime createdAt;

    private String hospitalId;
    private String hospitalName;

    private String userId;
    private String userPhone;

    private String patientId;
    private String patientName;

    private String scheduleId;
    private LocalDate workDate;
    private String timeSlot;

    private String doctorId;
    private String doctorName;

    private String departmentId;
    private String departmentName;
}
