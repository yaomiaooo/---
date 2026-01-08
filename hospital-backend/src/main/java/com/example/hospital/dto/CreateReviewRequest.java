package com.example.hospital.dto;

import lombok.Data;

/**
 * 创建评价请求DTO
 */
@Data
public class CreateReviewRequest {
    private String appointmentId;  // 预约ID（用于关联就诊记录）
    private Integer rating;         // 评分（1-5）
    private String content;         // 评价内容
}

