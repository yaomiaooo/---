package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hospital")
public class Hospital {
    @TableId
    private String hospitalId;      // varchar(32), 主键
    private String hospitalName;    // varchar(100)
    private String hospitalAddress; // varchar(255)
    private String hospitalIntro;   // varchar(500)
    private String hospitalPhone;   // varchar(20)
}