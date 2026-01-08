package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("department")
public class Department {
    @TableId
    private String departmentId;   // varchar(32), 主键
    private String hospitalId;     // 外键
    private String departmentName; // varchar(100)
    private String departmentIntro;// varchar(500)
}