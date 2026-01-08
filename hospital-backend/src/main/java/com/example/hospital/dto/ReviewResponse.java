package com.example.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价响应DTO
 */
@Data
public class ReviewResponse {
    private String reviewId;          // 评价ID
    private String visitId;           // 就诊记录ID
    private String appointmentId;     // 预约ID
    private String doctorId;          // 医生ID
    private String doctorName;        // 医生姓名
    private Integer rating;           // 评分（1-5）
    private String content;           // 评价内容
    private LocalDateTime createdAt;  // 创建时间
}

