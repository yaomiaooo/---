package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-就诊人关联表
 * 注意：此表使用复合主键 (user_id, patient_id)
 * 因此不能使用 MyBatis-Plus 的 getById、removeById、updateById 等方法
 * 必须使用 QueryWrapper 进行条件查询和操作
 */
@Data
@TableName("user_patient")
public class UserPatient {
    private String userId;       // varchar(32), 联合主键的一部分
    private String patientId;    // varchar(32), 联合主键的一部分
    private String relationType; // '本人'/'父母'/'子女'/'配偶'/'其他'
}