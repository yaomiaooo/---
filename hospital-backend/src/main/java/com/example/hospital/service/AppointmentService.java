package com.example.hospital.service;

import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.dto.AppointmentResponse;
import com.example.hospital.dto.CreateAppointmentRequest;
import com.example.hospital.dto.MedicalRecordDTO;
import com.example.hospital.entity.Appointment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 获取当前用户的所有预约记录
     * 通过 user_patient 表查找与该用户关联的所有就诊人
     * 然后查询这些就诊人的所有预约记录
     * 
     * @param userId 用户ID
     * @return 预约记录列表
     */
    List<AppointmentResponse> getAppointmentsByUserId(String userId);

    /**
     * 创建预约
     * 
     * @param userId 用户ID
     * @param request 创建预约请求
     * @return 创建的预约记录
     */
    AppointmentResponse createAppointment(String userId, CreateAppointmentRequest request);

    /**
     * 取消预约
     * 将预约状态修改为 CANCELLED（已取消）
     * 
     * @param userId 用户ID
     * @param appointmentId 预约ID
     * @return 是否取消成功
     */
    boolean cancelAppointment(String userId, String appointmentId);

    /**
     * 获取医生病历档案
     * @param userId 用户ID (医生对应的用户ID)
     * @param month 查询月份 (YYYY-MM格式)
     * @param page 页码
     * @param pageSize 每页大小
     * @return 病历记录分页列表
     */
    Page<MedicalRecordDTO> getMedicalRecords(String userId, String month, int page, int pageSize);

    // ========== 医生端API方法 ==========

    /**
     * 获取医生今日的预约列表
     * @param doctorId 医生ID
     * @param today 今日日期
     * @return 预约列表
     */
    List<Appointment> getTodayAppointmentsByDoctorId(String doctorId, LocalDate today);

    /**
     * 根据预约ID获取完整的预约信息
     * @param appointmentId 预约ID
     * @return 预约详细信息列表
     */
    List<AppointmentDetailDTO> getAppointmentWithDetails(String appointmentId);

    /**
     * 更新预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 更新后的预约
     */
    Appointment updateAppointmentStatus(String appointmentId, String status);

    /**
     * 开始接诊
     * @param appointmentId 预约ID
     * @param visitTimeStr 就诊时间字符串（前端本地时间）
     * @return 是否成功
     */
    boolean startConsultation(String appointmentId, String visitTimeStr);

    /**
     * 完成接诊
     * @param appointmentId 预约ID
     * @param patientId 患者ID
     * @param diagnosis 诊断结果
     * @return 是否成功
     */
    boolean completeConsultation(String appointmentId, String patientId, String diagnosis);

    /**
     * 获取诊断结果
     * @param appointmentId 预约ID
     * @return 诊断结果
     */
    String getDiagnosis(String appointmentId);
}

