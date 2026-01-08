package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.dto.AppointmentResponse;
import com.example.hospital.dto.CreateAppointmentRequest;
import com.example.hospital.dto.MedicalRecordDTO;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.service.AppointmentService;
import com.example.hospital.service.DoctorService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/appointments")
@UserLoginToken // 需要登录访问
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 获取当前登录用户的所有预约记录
     * @param request HttpServletRequest，用于获取用户ID
     * @return 预约记录列表
     */
    @GetMapping
    public Result<List<AppointmentResponse>> getMyAppointments(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("AppointmentController.getMyAppointments - 从request获取的userId: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法获取预约记录");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    /**
     * 创建预约
     * @param request HttpServletRequest，用于获取用户ID
     * @param createRequest 创建预约请求
     * @return 创建的预约记录
     */
    @Log("用户创建预约")
    @PostMapping
    public Result<AppointmentResponse> createAppointment(HttpServletRequest request, @RequestBody CreateAppointmentRequest createRequest) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("AppointmentController.createAppointment - 从request获取的userId: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法创建预约");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        try {
            AppointmentResponse appointment = appointmentService.createAppointment(userId, createRequest);
            return Result.success(appointment);
        } catch (Exception e) {
            System.err.println("创建预约异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约
     * @param request HttpServletRequest，用于获取用户ID
     * @param appointmentId 预约ID
     * @return 操作结果
     */
    @Log("用户取消预约")
    @PutMapping("/{appointmentId}/cancel")
    public Result<String> cancelAppointment(HttpServletRequest request, @PathVariable String appointmentId) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("AppointmentController.cancelAppointment - 从request获取的userId: " + userId + ", appointmentId: " + appointmentId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法取消预约");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        try {
            boolean success = appointmentService.cancelAppointment(userId, appointmentId);
            if (success) {
                return Result.success("预约取消成功");
            } else {
                return Result.error("取消预约失败，请重试");
            }
        } catch (Exception e) {
            System.err.println("取消预约异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取医生病历档案（基于visit表）
     * @param request HttpServletRequest
     * @param month 查询月份 (YYYY-MM格式)
     * @param page 页码
     * @param pageSize 每页大小
     * @return 病历记录列表
     */
    @UserLoginToken
    @GetMapping("/visit-records")
    public Result<Page<MedicalRecordDTO>> getMedicalRecords(HttpServletRequest request,
                                                           @RequestParam(required = false) String month,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        String userId = (String) request.getAttribute("userId");
        
        if (userId == null || userId.isEmpty()) {
            return Result.error("用户未登录或token无效");
        }

        try {
            Page<MedicalRecordDTO> result = appointmentService.getMedicalRecords(userId, month, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            System.err.println("获取病历档案失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取病历档案失败：" + e.getMessage());
        }
    }

    // ========== 医生端API接口 ==========

    /**
     * 获取当前登录医生今日的预约列表
     * @param request HttpServletRequest
     * @return 今日预约列表
     */
    @UserLoginToken
    @GetMapping("/today")
    public Result<List<Appointment>> getTodayAppointments(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }

        LocalDate today = LocalDate.now();
        List<Appointment> appointments = appointmentService.getTodayAppointmentsByDoctorId(doctor.getDoctorId(), today);
        return Result.success(appointments);
    }

    /**
     * 获取预约详细信息
     * @param appointmentId 预约ID
     * @return 预约详细信息列表
     */
    @UserLoginToken
    @GetMapping("/{appointmentId}/details")
    public Result<List<AppointmentDetailDTO>> getAppointmentDetails(@PathVariable String appointmentId) {
        List<AppointmentDetailDTO> appointments = appointmentService.getAppointmentWithDetails(appointmentId);
        if (appointments == null || appointments.isEmpty()) {
            return Result.error("预约不存在");
        }
        return Result.success(appointments);
    }

    /**
     * 开始接诊
     * @param request HttpServletRequest
     * @param appointmentId 预约ID
     * @param requestBody 请求体，包含visitTime
     * @return 操作结果
     */
    @Log("医生开始接诊")
    @UserLoginToken
    @PostMapping("/{appointmentId}/start")
    public Result<String> startConsultation(HttpServletRequest request,
                                          @PathVariable String appointmentId,
                                          @RequestBody Map<String, String> requestBody) {
        String visitTimeStr = requestBody.get("visitTime");
        boolean success = appointmentService.startConsultation(appointmentId, visitTimeStr);
        if (success) {
            return Result.success("开始接诊");
        } else {
            return Result.error("无法开始接诊，预约状态不正确");
        }
    }

    /**
     * 完成接诊
     * @param request HttpServletRequest
     * @param appointmentId 预约ID
     * @param requestBody 请求体，包含patientId和diagnosis
     * @return 操作结果
     */
    @Log("医生完成接诊")
    @UserLoginToken
    @PostMapping("/{appointmentId}/complete")
    public Result<String> completeConsultation(HttpServletRequest request,
                                             @PathVariable String appointmentId,
                                             @RequestBody Map<String, String> requestBody) {
        String patientId = requestBody.get("patientId");
        String diagnosis = requestBody.get("diagnosis");

        boolean success = appointmentService.completeConsultation(appointmentId, patientId, diagnosis);
        if (success) {
            return Result.success("诊断完成");
        } else {
            return Result.error("无法完成诊断，预约状态不正确");
        }
    }

    /**
     * 获取诊断结果
     * @param request HttpServletRequest
     * @param appointmentId 预约ID
     * @return 诊断结果
     */
    @UserLoginToken
    @GetMapping("/{appointmentId}/diagnosis")
    public Result<String> getDiagnosis(HttpServletRequest request, @PathVariable String appointmentId) {
        String diagnosis = appointmentService.getDiagnosis(appointmentId);
        if (diagnosis != null) {
            return Result.success(diagnosis);
        } else {
            return Result.error("未找到诊断记录");
        }
    }
}

