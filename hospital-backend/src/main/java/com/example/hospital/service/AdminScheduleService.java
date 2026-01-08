package com.example.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;

import java.util.List;

public interface AdminScheduleService {

    IPage<AdminScheduleVO> pageSchedules(AdminScheduleQueryRequest req);

    void createSchedule(AdminScheduleCreateRequest req);

    void updateSchedule(String scheduleId, AdminScheduleUpdateRequest req);

    void deleteSchedule(String scheduleId);

    // 下拉数据
    List<Hospital> listHospitals();

    List<Department> listDepartments(String hospitalId);

    List<Doctor> listDoctors(String hospitalId, String departmentId);
}

