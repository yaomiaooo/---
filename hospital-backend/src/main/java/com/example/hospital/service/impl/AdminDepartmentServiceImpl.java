package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Hospital;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.HospitalMapper;
import com.example.hospital.service.AdminDepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDepartmentServiceImpl implements AdminDepartmentService {

    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;

    public AdminDepartmentServiceImpl(
            DepartmentMapper departmentMapper,
            HospitalMapper hospitalMapper
    ) {
        this.departmentMapper = departmentMapper;
        this.hospitalMapper = hospitalMapper;
    }

    @Override
    public IPage<AdminDepartmentVO> pageDepartments(AdminDepartmentQueryRequest req) {
        Page<Department> page = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<Department> qw = new LambdaQueryWrapper<>();
        if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
            qw.like(Department::getDepartmentName, req.getKeyword());
        }

        IPage<Department> entityPage = departmentMapper.selectPage(page, qw);

        return entityPage.convert(dep -> {
            AdminDepartmentVO vo = new AdminDepartmentVO();
            vo.setDepartmentId(dep.getDepartmentId());
            vo.setHospitalId(dep.getHospitalId());
            vo.setDepartmentName(dep.getDepartmentName());
            vo.setDepartmentIntro(dep.getDepartmentIntro());
            return vo;
        });
    }

    @Override
    public boolean createDepartment(AdminDepartmentCreateRequest req) {
        Department dep = new Department();
        dep.setDepartmentId(req.getDepartmentId());
        dep.setHospitalId(req.getHospitalId());
        dep.setDepartmentName(req.getDepartmentName());
        dep.setDepartmentIntro(req.getDepartmentIntro());

        departmentMapper.insert(dep);
        return true;
    }

    @Override
    public boolean updateDepartment(AdminDepartmentUpdateRequest req) {
        Department exist = departmentMapper.selectById(req.getDepartmentId());
        if (exist == null) {
            throw new RuntimeException("科室不存在");
        }

        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Department> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();

        uw.eq(Department::getDepartmentId, req.getDepartmentId());
        uw.eq(Department::getHospitalId, exist.getHospitalId());

        uw.set(Department::getDepartmentName, req.getDepartmentName());
        uw.set(Department::getDepartmentIntro, req.getDepartmentIntro());

        int rows = departmentMapper.update(null, uw);
        if (rows == 0) {
            throw new RuntimeException("更新失败（可能是分片路由或数据不存在）");
        }
        return true;
    }

    @Override
    public boolean deleteDepartment(String departmentId) {
        departmentMapper.deleteById(departmentId);
        return true;
    }

    @Override
    public List<Hospital> listHospitals() {
        return hospitalMapper.selectList(null);
    }
}

