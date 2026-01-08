package com.example.hospital.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** 管理员 - 批量更新订单状态 */
@Data
public class AdminAppointmentBatchStatusUpdateRequest {

    @NotEmpty(message = "appointmentIds不能为空")
    private List<String> appointmentIds;

    @NotBlank(message = "status不能为空")
    private String status; // BOOKED/CANCELLED/COMPLETED/NO_SHOW
}
