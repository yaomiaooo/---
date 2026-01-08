package com.example.hospital.service;

import com.example.hospital.entity.Department;

import java.util.List;

/**
 * 科室服务接口
 */
public interface DepartmentService {

    /**
     * 获取所有科室列表
     * @return 科室列表
     */
    List<Department> getAllDepartments();

    /**
     * 根据医院ID获取科室列表
     * @param hospitalId 医院ID
     * @return 科室列表
     */
    List<Department> getDepartmentsByHospitalId(String hospitalId);

    /**
     * 搜索科室（支持按科室名称或介绍模糊搜索）
     * @param keyword 搜索关键词
     * @param hospitalId 医院ID（可选，如果提供则只搜索该医院的科室）
     * @return 科室列表
     */
    List<Department> searchDepartments(String keyword, String hospitalId);

    /**
     * 根据科室ID获取科室详情
     * @param departmentId 科室ID
     * @return 科室实体
     */
    Department getDepartmentById(String departmentId);
}

