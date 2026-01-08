package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hospital.entity.Department;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科室服务实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> getAllDepartments() {
        return departmentMapper.selectList(null);
    }

    @Override
    public List<Department> getDepartmentsByHospitalId(String hospitalId) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hospital_id", hospitalId);
        return departmentMapper.selectList(queryWrapper);
    }

    @Override
    public List<Department> searchDepartments(String keyword, String hospitalId) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        
        // 如果提供了 hospitalId，先按医院ID过滤
        if (hospitalId != null && !hospitalId.isEmpty()) {
            queryWrapper.eq("hospital_id", hospitalId);
        }
        
        // 如果提供了关键词，进行模糊搜索（搜索科室名称或介绍）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("department_name", keyword.trim())
                .or()
                .like("department_intro", keyword.trim())
            );
        }
        
        return departmentMapper.selectList(queryWrapper);
    }

    @Override
    public Department getDepartmentById(String departmentId) {
        return departmentMapper.selectById(departmentId);
    }
}

