package com.example.hospital.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;
import com.example.hospital.service.AdminScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/schedules")
@UserLoginToken(roles = {"ADMIN"})
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    public AdminScheduleController(AdminScheduleService adminScheduleService) {
        this.adminScheduleService = adminScheduleService;
    }

    // 下拉：院区
    @GetMapping("/hospitals")
    public Result<List<Hospital>> listHospitals() {
        return Result.success(adminScheduleService.listHospitals());
    }

    // 下拉：科室（按院区）
    @GetMapping("/departments")
    public Result<List<Department>> listDepartments(@RequestParam String hospitalId) {
        return Result.success(adminScheduleService.listDepartments(hospitalId));
    }

    // 下拉：医生（按院区，可选科室）
    @GetMapping("/doctors")
    public Result<List<Doctor>> listDoctors(@RequestParam String hospitalId,
                                            @RequestParam(required = false) String departmentId) {
        return Result.success(adminScheduleService.listDoctors(hospitalId, departmentId));
    }

    // 分页查询排班
    @GetMapping
    public Result<IPage<AdminScheduleVO>> page(AdminScheduleQueryRequest req) {
        return Result.success(adminScheduleService.pageSchedules(req));
    }

    // 新增排班
    @Log("新增排班")
    @PostMapping
    public Result<Boolean> create(@RequestBody AdminScheduleCreateRequest req) {
        adminScheduleService.createSchedule(req);
        return Result.success(true);
    }

    // 更新排班（重要：不允许更新 hospitalId）
    @Log("修改排班信息")
    @PutMapping("/{scheduleId}")
    public Result<Boolean> update(@PathVariable String scheduleId,
                                  @RequestBody AdminScheduleUpdateRequest req) {
        adminScheduleService.updateSchedule(scheduleId, req);
        return Result.success(true);
    }

    // 删除排班
    @Log("删除排班")
    @DeleteMapping("/{scheduleId}")
    public Result<Boolean> delete(@PathVariable String scheduleId) {
        adminScheduleService.deleteSchedule(scheduleId);
        return Result.success(true);
    }
}

