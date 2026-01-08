package com.example.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Hospital;

import java.util.List;

public interface AdminDepartmentService {

    IPage<AdminDepartmentVO> pageDepartments(AdminDepartmentQueryRequest req);

    boolean createDepartment(AdminDepartmentCreateRequest req);

    boolean updateDepartment(AdminDepartmentUpdateRequest req);

    boolean deleteDepartment(String departmentId);

    List<Hospital> listHospitals();
}

