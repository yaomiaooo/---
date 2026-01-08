package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.ASSIGN_ID)
    private String logId;           // varchar(32), 主键
    private String operatorUserId;  // 操作人用户ID（外键，关联user表）
    private String hospitalId;      // 院区ID（可选，用于业务查询和统计，audit_log表是广播表，不需要分片键）
    private String roleType;        // 角色类型：'user'/'doctor'/'admin'
    private String action;          // 操作描述：varchar(255)，例如"用户创建预约"、"管理员删除排班"等
    private LocalDateTime actionTime; // 操作时间：默认当前时间
}