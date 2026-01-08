package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.ChangePasswordRequest;
import com.example.hospital.dto.DoctorProfileResponse;
import com.example.hospital.dto.DoctorProfileUpdateRequest;
import com.example.hospital.entity.Doctor;
import com.example.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 医生个人资料控制器
 */
@RestController
@RequestMapping("/api/doctor")
public class DoctorProfileController {

    @Autowired
    private DoctorService doctorService;

    /**
     * 获取当前登录医生的个人资料
     * @param request HttpServletRequest，用于获取拦截器中存入的用户ID
     * @return 医生完整资料（包含医院和科室信息）
     */
    @UserLoginToken
    @GetMapping("/profile")
    public Result<DoctorProfileResponse> getDoctorProfile(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        DoctorProfileResponse doctorProfile = doctorService.getDoctorProfile(userId);
        return Result.success(doctorProfile);
    }

    /**
     * 更新当前登录医生的个人资料
     * @param request HttpServletRequest
     * @param updateRequest 包含更新信息的数据体
     * @return 更新后的医生完整资料
     */
    @Log("医生修改个人信息")
    @UserLoginToken
    @PutMapping("/profile")
    public Result<DoctorProfileResponse> updateDoctorProfile(HttpServletRequest request, @RequestBody DoctorProfileUpdateRequest updateRequest) {
        String userId = (String) request.getAttribute("userId");
        Doctor updatedDoctor = doctorService.updateDoctorProfile(userId, updateRequest);
        DoctorProfileResponse doctorProfile = doctorService.getDoctorProfile(userId);
        return Result.success(doctorProfile);
    }

    /**
     * 修改当前登录医生的密码
     * @param request HttpServletRequest
     * @param changePasswordRequest 包含旧密码和新密码
     * @return 修改结果
     */
    @Log("医生修改密码")
    @UserLoginToken
    @PostMapping("/change-password")
    public Result<String> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest changePasswordRequest) {
        String userId = (String) request.getAttribute("userId");

        if (userId == null || userId.isEmpty()) {
            return Result.error("用户身份验证失败，请重新登录");
        }

        try {
            boolean success = doctorService.changePassword(userId, changePasswordRequest);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("修改密码失败，请重试");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

