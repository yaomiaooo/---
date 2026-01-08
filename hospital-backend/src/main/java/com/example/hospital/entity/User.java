package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId
    private String userId;        // varchar(32), 主键
    private String userPhone;     // varchar(20), 唯一
    private String userPassword;  // varchar(60)
    private String role;          // 'user'/'doctor'/'admin'
    private LocalDateTime createdAt; // 默认当前时间

}