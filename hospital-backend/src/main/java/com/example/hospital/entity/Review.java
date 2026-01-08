package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId
    private String reviewId;      // varchar(32), 主键
    private String visitId;        // 外键（Visit.visit_id）
    @TableField(exist = false, select = false)    // 标记该字段不存在于数据库表中，且查询时不选择
    private String appointmentId;  // 外键（Appointment.appointment_id），用于关联预约记录（通过visit表获取）
    private String userId;         // 用户ID
    private String hospitalId;     // 分片键（对应所属院区），必须设置，否则 ShardingSphere 会报错
    @TableField(exist = false, select = false)    // 标记该字段不存在于数据库表中，且查询时不选择
    private String doctorId;       // 医生ID（通过关联查询获取，不存储在review表中）
    private Integer rating;        // 评分（1-5）
    private String content;        // 评价内容
    private LocalDateTime reviewTime; // 创建时间（对应数据库的 review_time 列）
}

