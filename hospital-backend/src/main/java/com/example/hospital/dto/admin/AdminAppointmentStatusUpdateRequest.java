package com.example.hospital.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理员 - 更新单个订单状态 */
@Data
public class AdminAppointmentStatusUpdateRequest {

    @NotBlank(message = "status不能为空")
    private String status; // BOOKED/CANCELLED/COMPLETED/NO_SHOW
}
