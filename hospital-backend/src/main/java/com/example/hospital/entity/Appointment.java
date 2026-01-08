package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId
    private String appointmentId; // 主键
    private String userId;        // 外键（User.user_id）
    private String patientId;     // 外键（Patient.patient_id）
    private String scheduleId;    // 外键（Schedule.schedule_id）
    private String hospitalId;    // 医院/院区ID [cite: 36]
    private String status;        // 'BOOKED'/'CANCELLED'/'COMPLETED'/'NO_SHOW'
    private LocalDateTime createdAt; // 默认当前时间

    // 以下字段用于JOIN查询结果，不对应数据库字段
    @TableField(exist = false)
    private String patientName;   // 患者姓名
    @TableField(exist = false)
    private String patientGender; // 患者性别
    @TableField(exist = false)
    private LocalDate patientBirthday; // 患者生日
    @TableField(exist = false)
    private String timeSlot;      // 时间段
    @TableField(exist = false)
    private String title;         // 医生职称
}