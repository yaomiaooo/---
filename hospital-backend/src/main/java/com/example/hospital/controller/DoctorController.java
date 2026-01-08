package com.example.hospital.controller;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.dto.DoctorResponse;
import com.example.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生控制器
 */
@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {

    @Autowired
    private DoctorService doctorService;
    
    // 添加构造函数用于调试
    public DoctorController() {
        System.out.println("DoctorController 被实例化了！");
    }

    /**
     * 获取医生列表（支持按院区、科室、关键词搜索）
     * @param hospitalId 院区ID（可选）
     * @param departmentId 科室ID（可选）
     * @param keyword 搜索关键词（医生姓名，可选）
     * @return 医生列表
     */
    @PassToken
    @GetMapping
    public Result<List<DoctorResponse>> getDoctors(
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String keyword) {
        
        System.out.println("前端正在请求医生列表... hospitalId: " + hospitalId + ", departmentId: " + departmentId + ", keyword: " + keyword);
        
        List<DoctorResponse> doctors = doctorService.getDoctors(hospitalId, departmentId, keyword);
        return Result.success(doctors);
    }

    /**
     * 根据医生ID获取医生详情
     * @param doctorId 医生ID
     * @return 医生信息
     */
    @PassToken
    @GetMapping("/{doctorId}")
    public Result<DoctorResponse> getDoctorById(@PathVariable String doctorId) {
        System.out.println("前端正在请求医生详情... doctorId: " + doctorId);
        DoctorResponse doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        return Result.success(doctor);
    }
}

