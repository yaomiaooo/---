package com.example.hospital.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.admin.*;
import com.example.hospital.service.AdminDoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
@UserLoginToken(roles = {"ADMIN"})
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    /** 分页查询医生 */
    @GetMapping
    public Result<Page<AdminDoctorVO>> page(AdminDoctorQueryRequest req) {
        return Result.success(adminDoctorService.pageDoctors(req));
    }

    /** 新增医生 */
    @Log("新增医生")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid AdminDoctorCreateRequest req) {
        adminDoctorService.createDoctor(req);
        return Result.success(null);
    }

    /**
     * 更新医生（不触碰分片键）：hospitalId 必填用于路由，但不会写入更新字段
     * PUT /api/admin/doctors/{doctorId}?hospitalId=1
     */
    @Log("修改医生信息")
    @PutMapping("/{doctorId}")
    public Result<Void> update(
            @PathVariable String doctorId,
            @RequestParam String hospitalId,
            @RequestBody @Valid AdminDoctorUpdateRequest req
    ) {
        adminDoctorService.updateDoctor(doctorId, hospitalId, req);
        return Result.success(null);
    }

    /** 删除医生：DELETE /api/admin/doctors/{doctorId}?hospitalId=1 */
    @Log("删除医生")
    @DeleteMapping("/{doctorId}")
    public Result<Void> delete(
            @PathVariable String doctorId,
            @RequestParam String hospitalId
    ) {
        adminDoctorService.deleteDoctor(doctorId, hospitalId);
        return Result.success(null);
    }
}

