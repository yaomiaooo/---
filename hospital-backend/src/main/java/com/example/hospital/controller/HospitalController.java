package com.example.hospital.controller;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.entity.Hospital;
import com.example.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * 医院/院区控制器
 */
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 获取所有医院/院区列表
     * @return 医院列表
     */
    @PassToken // 无需登录即可访问
    @GetMapping
    public Result<List<Hospital>> getAllHospitals() {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        return Result.success(hospitals);
    }

    /**
     * 根据ID获取医院详情
     * @param id 医院ID
     * @return 医院实体
     */
    @PassToken // 无需登录即可访问
    @GetMapping("/{id}")
    public Result<Hospital> getHospitalById(@PathVariable String id) {
        Hospital hospital = hospitalService.getHospitalById(id);
        return Result.success(hospital);
    }

    /**
     * 根据ID获取医院介绍（用于院区详情页）
     * @param id 医院ID (1=朝晖, 2=屏峰)
     * @return 医院实体，包含介绍信息
     */
    @PassToken // 无需登录即可访问
    @GetMapping("/intro/{id}")
    public Result<Hospital> getHospitalIntroById(@PathVariable String id) {
        Hospital hospital = hospitalService.getHospitalById(id);
        return Result.success(hospital);
    }

    // 未来可以添加获取单个医院详情、获取某医院下所有科室等接口
}

