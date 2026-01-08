package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("patient")
public class Patient {
    @TableId
    private String patientId;       // varchar(32), 主键
    private String patientIdcard;   // varchar(18), 唯一
    private String patientName;     // varchar(50)
    private String patientGender;   // varchar(1), '男'/'女'
    private LocalDate patientBirthday; // date
    private String patientPhone;    // varchar(20)
}