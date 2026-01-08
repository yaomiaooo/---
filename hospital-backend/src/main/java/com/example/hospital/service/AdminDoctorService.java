package com.example.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.AdminDoctorCreateRequest;
import com.example.hospital.dto.admin.AdminDoctorQueryRequest;
import com.example.hospital.dto.admin.AdminDoctorUpdateRequest;
import com.example.hospital.dto.admin.AdminDoctorVO;

public interface AdminDoctorService {

    Page<AdminDoctorVO> pageDoctors(AdminDoctorQueryRequest req);

    void createDoctor(AdminDoctorCreateRequest req);

    /**
     * 更新医生：必须提供 hospitalId 做分片路由；但严禁更新 hospital_id（分片键）以及 user_id/doctor_id/department_id
     */
    void updateDoctor(String doctorId, String hospitalId, AdminDoctorUpdateRequest req);

    /**
     * 删除医生：建议提供 hospitalId 做分片路由
     */
    void deleteDoctor(String doctorId, String hospitalId);
}

