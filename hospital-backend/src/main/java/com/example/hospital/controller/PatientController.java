package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.AddPatientRequest;
import com.example.hospital.dto.PatientResponse;
import com.example.hospital.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * 就诊人控制器
 */
@RestController
@RequestMapping("/api/patients")
@UserLoginToken // 整个Controller都需要登录访问
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 获取当前登录用户的所有就诊人
     * @param request HttpServletRequest，用于获取用户ID
     * @return 就诊人列表
     */
    @GetMapping
    public Result<List<PatientResponse>> getMyPatients(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("PatientController.getMyPatients - 从request获取的userId: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法获取就诊人列表");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        List<PatientResponse> patients = patientService.getPatientsByUserId(userId);
        return Result.success(patients);
    }

    /**
     * 为当前登录用户添加就诊人
     * @param request HttpServletRequest，用于获取用户ID
     * @param addPatientRequest 添加就诊人的请求数据
     * @return 新添加的就诊人信息
     */
    @Log("用户添加就诊人")
    @PostMapping
    public Result<PatientResponse> addPatient(HttpServletRequest request, @RequestBody AddPatientRequest addPatientRequest) {
        String userId = (String) request.getAttribute("userId");
        PatientResponse newPatient = patientService.addPatientForUser(userId, addPatientRequest);
        return Result.success(newPatient);
    }

    /**
     * 更新就诊人信息
     * @param request HttpServletRequest，用于获取用户ID
     * @param patientId 就诊人ID
     * @param updateRequest 更新请求数据
     * @return 更新后的就诊人信息
     */
    @Log("用户修改就诊人信息")
    @PutMapping("/{patientId}")
    public Result<PatientResponse> updatePatient(HttpServletRequest request, 
                                                 @PathVariable String patientId,
                                                 @RequestBody AddPatientRequest updateRequest) {
        String userId = (String) request.getAttribute("userId");
        PatientResponse updatedPatient = patientService.updatePatient(userId, patientId, updateRequest);
        return Result.success(updatedPatient);
    }

    /**
     * 删除就诊人（删除用户与就诊人的关联关系）
     * @param request HttpServletRequest，用于获取用户ID
     * @param patientId 就诊人ID
     * @return 删除结果
     */
    @Log("用户删除就诊人")
    @DeleteMapping("/{patientId}")
    public Result<String> deletePatient(HttpServletRequest request, @PathVariable String patientId) {
        String userId = (String) request.getAttribute("userId");
        patientService.deletePatient(userId, patientId);
        return Result.success("删除成功");
    }
}

