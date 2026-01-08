package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doctor")
public class Doctor {
    @TableId
    private String doctorId;     // 主键
    private String userId;       // 外键（User.user_id）
    private String hospitalId;   // 分片键（对应表中定义的所属院区） [cite: 27]
    private String departmentId; // 外键（Department.department_id），医生所属科室
    private String doctorName;   // 姓名
    private String doctorGender; // 取值限定：'男'/'女'
    private String doctorIdcard; // 非空且唯一
    private String title;        // 职称
    private String doctorPhone;  // 非空且唯一
    private String doctorEmail;  // 可空且唯一
    private String doctorIntro;  // 简介
    private String avatarUrl;    // 头像URL
}