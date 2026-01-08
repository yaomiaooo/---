package com.example.hospital.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.admin.AdminAppointmentBatchStatusUpdateRequest;
import com.example.hospital.dto.admin.AdminAppointmentQueryRequest;
import com.example.hospital.dto.admin.AdminAppointmentStatusUpdateRequest;
import com.example.hospital.dto.admin.AdminAppointmentVO;
import com.example.hospital.service.AdminAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
@UserLoginToken(roles = {"ADMIN"})
public class AdminAppointmentController {

    private final AdminAppointmentService adminAppointmentService;

    /** 分页查询预约订单 */
    @GetMapping
    public Result<Page<AdminAppointmentVO>> page(AdminAppointmentQueryRequest req) {
        return Result.success(adminAppointmentService.pageAppointments(req));
    }

    /** 订单详情 */
    @GetMapping("/{appointmentId}")
    public Result<AdminAppointmentVO> detail(@PathVariable String appointmentId) {
        return Result.success(adminAppointmentService.getDetail(appointmentId));
    }

    /** 更新单个订单状态 */
    @Log("更新预约订单状态")
    @PatchMapping("/{appointmentId}/status")
    public Result<Void> updateStatus(
            @PathVariable String appointmentId,
            @RequestBody @Valid AdminAppointmentStatusUpdateRequest req
    ) {
        adminAppointmentService.updateStatus(appointmentId, req.getStatus());
        return Result.success(null);
    }

    /** 批量更新订单状态 */
    @Log("批量更新预约订单状态")
    @PatchMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@RequestBody @Valid AdminAppointmentBatchStatusUpdateRequest req) {
        adminAppointmentService.batchUpdateStatus(req.getAppointmentIds(), req.getStatus());
        return Result.success(null);
    }
}

