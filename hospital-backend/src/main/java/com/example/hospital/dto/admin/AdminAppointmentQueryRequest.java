package com.example.hospital.dto.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 管理员 - 预约订单分页查询
 *
 * - keyword：模糊匹配（手机号 / 就诊人姓名 / 订单号）
 * - status：BOOKED/CANCELLED/COMPLETED/NO_SHOW
 * - hospitalId：按院区过滤（建议传 1/2；后端兼容 A/B）
 * - startTime/endTime：按订单创建时间 created_at 过滤（可选）
 */
@Data
public class AdminAppointmentQueryRequest {

    private long page = 1;
    private long size = 10;

    private String keyword;
    private String status;

    /** 建议传 "1"/"2"，后端兼容 "A"/"B" */
    private String hospitalId;

    /** created_at >= startTime */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** created_at <= endTime */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 精确筛某个排班（优先级最高） */
    private String scheduleId;

    /** 精确筛某个医生的订单（通过 schedule 半连接） */
    private String doctorId;

    /** 精确筛某个科室的订单（department -> doctor -> schedule 半连接） */
    private String departmentId;
}
