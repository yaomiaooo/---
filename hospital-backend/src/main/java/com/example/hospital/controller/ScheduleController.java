package com.example.hospital.controller;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.OutpatientScheduleResponse;
import com.example.hospital.dto.ScheduleResponse;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Schedule;
import com.example.hospital.service.DoctorService;
import com.example.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

/**
 * 排班控制器
 */
@RestController
@RequestMapping("/api/schedules")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 根据医生ID获取排班信息
     * @param doctorId 医生ID（必填）
     * @param hospitalId 院区ID（可选，用于分片）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 排班列表
     */
    @PassToken
    @GetMapping
    public Result<List<ScheduleResponse>> getSchedules(
            @RequestParam(required = true) String doctorId,
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        System.out.println("前端正在请求排班信息... doctorId: " + doctorId + ", hospitalId: " + hospitalId + 
                         ", startDate: " + startDate + ", endDate: " + endDate);
        
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDoctor(doctorId, hospitalId, startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 获取门诊排班信息（按科室分组）
     * @param hospitalId 院区ID（必填）
     * @param titleFilter 职称筛选（可选）："all"=全部, "normal"=普通门诊（医师、主任）, "expert"=专家门诊（专家）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 按科室分组的排班列表
     */
    @PassToken
    @GetMapping("/outpatient")
    public Result<List<OutpatientScheduleResponse>> getOutpatientSchedules(
            @RequestParam(required = true) String hospitalId,
            @RequestParam(required = false, defaultValue = "all") String titleFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        System.out.println("前端正在请求门诊排班信息... hospitalId: " + hospitalId + ", titleFilter: " + titleFilter + 
                         ", startDate: " + startDate + ", endDate: " + endDate);
        
        List<OutpatientScheduleResponse> schedules = scheduleService.getOutpatientSchedules(hospitalId, titleFilter, startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 获取当前登录医生的排班信息
     * @param request HttpServletRequest
     * @return 排班列表
     */
    @UserLoginToken
    @GetMapping("/my-schedules")
    public Result<List<Schedule>> getMySchedules(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }
        List<Schedule> schedules = scheduleService.getSchedulesByDoctorId(doctor.getDoctorId());
        return Result.success(schedules);
    }

    /**
     * 根据日期范围获取当前登录医生的排班信息
     * @param request HttpServletRequest
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeSlot 时间段（可选）
     * @return 排班列表
     */
    @UserLoginToken
    @GetMapping("/my-schedules/range")
    public Result<List<Schedule>> getMySchedulesByDateRange(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String timeSlot) {

        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }

        List<Schedule> schedules;
        if (timeSlot != null && !timeSlot.trim().isEmpty()) {
            schedules = scheduleService.getSchedulesByDoctorIdAndDateRangeAndTimeSlot(doctor.getDoctorId(), startDate, endDate, timeSlot);
        } else {
            schedules = scheduleService.getSchedulesByDoctorIdAndDateRange(doctor.getDoctorId(), startDate, endDate);
        }

        return Result.success(schedules);
    }

    /**
     * 创建排班
     * @param request HttpServletRequest
     * @param schedule 排班信息
     * @return 创建的排班
     */
    @UserLoginToken
    @PostMapping
    public Result<Schedule> createSchedule(HttpServletRequest request, @RequestBody Schedule schedule) {
        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }

        // 设置医生ID和医院ID
        schedule.setDoctorId(doctor.getDoctorId());
        schedule.setHospitalId(doctor.getHospitalId());

        Schedule createdSchedule = scheduleService.createSchedule(schedule);
        return Result.success(createdSchedule);
    }

    /**
     * 更新排班
     * @param request HttpServletRequest
     * @param scheduleId 排班ID
     * @param schedule 排班信息
     * @return 更新后的排班
     */
    @UserLoginToken
    @PutMapping("/{scheduleId}")
    public Result<Schedule> updateSchedule(HttpServletRequest request, @PathVariable String scheduleId, @RequestBody Schedule schedule) {
        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }

        // 验证排班属于当前医生
        List<Schedule> doctorSchedules = scheduleService.getSchedulesByDoctorId(doctor.getDoctorId());
        Schedule existingSchedule = doctorSchedules.stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);

        if (existingSchedule == null) {
            return Result.error("排班不存在或无权限访问");
        }

        schedule.setScheduleId(scheduleId);
        schedule.setDoctorId(doctor.getDoctorId()); // 确保医生ID不变
        schedule.setHospitalId(existingSchedule.getHospitalId()); // 确保医院ID不变（分片键）

        Schedule updatedSchedule = scheduleService.updateSchedule(schedule);
        return Result.success(updatedSchedule);
    }

    /**
     * 删除排班
     * @param request HttpServletRequest
     * @param scheduleId 排班ID
     * @return 删除结果
     */
    @UserLoginToken
    @DeleteMapping("/{scheduleId}")
    public Result<String> deleteSchedule(HttpServletRequest request, @PathVariable String scheduleId) {
        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }

        // 验证排班属于当前医生
        List<Schedule> doctorSchedules = scheduleService.getSchedulesByDoctorId(doctor.getDoctorId());
        Schedule existingSchedule = doctorSchedules.stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);

        if (existingSchedule == null) {
            return Result.error("排班不存在或无权限访问");
        }

        // 检查是否已有预约
        if (existingSchedule.getBookedCount() != null && existingSchedule.getBookedCount() > 0) {
            return Result.error("该排班已有预约，无法删除");
        }

        scheduleService.deleteSchedule(scheduleId);
        return Result.success("删除成功");
    }
}

