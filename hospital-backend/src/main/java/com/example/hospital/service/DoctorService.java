package com.example.hospital.service;

import com.example.hospital.dto.ChangePasswordRequest;
import com.example.hospital.dto.DoctorProfileResponse;
import com.example.hospital.dto.DoctorProfileUpdateRequest;
import com.example.hospital.dto.DoctorResponse;
import com.example.hospital.entity.Doctor;

import java.util.List;

/**
 * 医生服务接口
 */
public interface DoctorService {

    /**
     * 根据院区和科室获取医生列表
     * @param hospitalId 院区ID（可选）
     * @param departmentId 科室ID（可选）
     * @param keyword 搜索关键词（医生姓名，可选）
     * @return 医生列表
     */
    List<DoctorResponse> getDoctors(String hospitalId, String departmentId, String keyword);

    /**
     * 根据医生ID获取医生详情
     * @param doctorId 医生ID
     * @return 医生信息
     */
    DoctorResponse getDoctorById(String doctorId);

    /**
     * 根据用户ID获取医生信息
     * @param userId 用户ID
     * @return 医生实体
     */
    Doctor getDoctorByUserId(String userId);

    /**
     * 根据用户ID获取医生完整资料（包含医院和科室信息）
     * @param userId 用户ID
     * @return 医生资料响应对象
     */
    DoctorProfileResponse getDoctorProfile(String userId);

    /**
     * 更新医生个人资料
     * @param userId 用户ID
     * @param request 更新请求体
     * @return 更新后的医生实体
     */
    Doctor updateDoctorProfile(String userId, DoctorProfileUpdateRequest request);

    /**
     * 修改医生密码
     * @param userId 用户ID
     * @param request 密码修改请求
     * @return 是否修改成功
     */
    boolean changePassword(String userId, ChangePasswordRequest request);
}

