package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.ChangePasswordRequest;
import com.example.hospital.dto.DoctorProfileResponse;
import com.example.hospital.dto.DoctorProfileUpdateRequest;
import com.example.hospital.dto.DoctorResponse;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Schedule;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.DepartmentService;
import com.example.hospital.service.DoctorService;
import com.example.hospital.service.HospitalService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生服务实现类
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public List<DoctorResponse> getDoctors(String hospitalId, String departmentId, String keyword) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按院区筛选
        if (hospitalId != null && !hospitalId.trim().isEmpty()) {
            queryWrapper.eq(Doctor::getHospitalId, hospitalId);
        }
        
        // 按科室筛选（通过医生的 departmentId 字段）
        if (departmentId != null && !departmentId.trim().isEmpty()) {
            queryWrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        
        // 按关键词搜索（医生姓名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Doctor::getDoctorName, keyword.trim());
        }
        
        // 2. 查询医生列表
        List<Doctor> doctors = doctorMapper.selectList(queryWrapper);
        
        if (doctors.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 3. 收集所有医生的科室ID，批量查询科室信息
        List<String> departmentIds = doctors.stream()
                .map(Doctor::getDepartmentId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询科室信息
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Department> departmentMap;
        if (!departmentIds.isEmpty()) {
            List<Department> departments = departmentMapper.selectBatchIds(departmentIds);
            departmentMap = departments.stream()
                    .collect(Collectors.toMap(Department::getDepartmentId, d -> d));
        } else {
            departmentMap = Collections.emptyMap();
        }
        
        // 4. 组装返回数据
        // 使用 final 确保在 Lambda 中可以安全访问 doctors
        final List<Doctor> finalDoctors = doctors;
        List<DoctorResponse> doctorResponses = finalDoctors.stream()
                .map(doctor -> {
                    DoctorResponse response = new DoctorResponse();
                    BeanUtils.copyProperties(doctor, response);
                    response.setDoctorId(doctor.getDoctorId());
                    response.setDoctorName(doctor.getDoctorName());
                    response.setTitle(doctor.getTitle());
                    response.setDoctorGender(doctor.getDoctorGender());
                    response.setDoctorPhone(doctor.getDoctorPhone());
                    response.setDoctorEmail(doctor.getDoctorEmail());
                    response.setDoctorIntro(doctor.getDoctorIntro());
                    response.setAvatarUrl(doctor.getAvatarUrl());
                    response.setHospitalId(doctor.getHospitalId());
                    response.setDepartmentId(doctor.getDepartmentId());
                    
                    // 设置科室名称
                    if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
                        Department department = departmentMap.get(doctor.getDepartmentId());
                        if (department != null) {
                            response.setDepartmentName(department.getDepartmentName());
                        }
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
        
        return doctorResponses;
    }

    @Override
    public DoctorResponse getDoctorById(String doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return null;
        }
        
        DoctorResponse response = new DoctorResponse();
        BeanUtils.copyProperties(doctor, response);
        response.setDoctorId(doctor.getDoctorId());
        response.setDoctorName(doctor.getDoctorName());
        response.setTitle(doctor.getTitle());
        response.setDoctorGender(doctor.getDoctorGender());
        response.setDoctorPhone(doctor.getDoctorPhone());
        response.setDoctorEmail(doctor.getDoctorEmail());
        response.setDoctorIntro(doctor.getDoctorIntro());
        response.setAvatarUrl(doctor.getAvatarUrl());
        response.setHospitalId(doctor.getHospitalId());
        response.setDepartmentId(doctor.getDepartmentId());
        
        // 查询医生所属科室
        if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            if (department != null) {
                response.setDepartmentName(department.getDepartmentName());
            }
        }
        
        return response;
    }

    @Override
    public Doctor getDoctorByUserId(String userId) {
        Doctor doctor = doctorMapper.selectByUserId(userId);
        return doctor;
    }

    @Override
    public DoctorProfileResponse getDoctorProfile(String userId) {
        Doctor doctor = getDoctorByUserId(userId);

        DoctorProfileResponse response = new DoctorProfileResponse();
        response.setDoctorId(doctor.getDoctorId());
        response.setDoctorName(doctor.getDoctorName());
        response.setDoctorGender(doctor.getDoctorGender());
        response.setDoctorPhone(doctor.getDoctorPhone());
        response.setDoctorEmail(doctor.getDoctorEmail());
        response.setTitle(doctor.getTitle());
        response.setDoctorIntro(doctor.getDoctorIntro());
        response.setHospitalId(doctor.getHospitalId());
        response.setDepartmentId(doctor.getDepartmentId());

        // 获取医院名称
        if (doctor.getHospitalId() != null && !doctor.getHospitalId().isEmpty()) {
            try {
                Hospital hospital = hospitalService.getHospitalById(doctor.getHospitalId());
                response.setHospitalName(hospital != null ? hospital.getHospitalName() : "");
            } catch (Exception e) {
                response.setHospitalName("");
            }
        } else {
            response.setHospitalName("");
        }

        // 获取科室名称
        if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().isEmpty()) {
            try {
                Department department = departmentService.getDepartmentById(doctor.getDepartmentId());
                response.setDepartmentName(department != null ? department.getDepartmentName() : "");
            } catch (Exception e) {
                response.setDepartmentName("");
            }
        } else {
            response.setDepartmentName("");
        }

        // 擅长领域暂时为空，后续可根据业务需求扩展
        response.setSpecialty("");

        return response;
    }

    @Override
    public Doctor updateDoctorProfile(String userId, DoctorProfileUpdateRequest request) {
        Doctor doctor = getDoctorByUserId(userId);

        String doctorPhone = doctor.getDoctorPhone();
        String doctorEmail = doctor.getDoctorEmail();
        String doctorIntro = doctor.getDoctorIntro();

        if (request.getDoctorPhone() != null && !request.getDoctorPhone().trim().isEmpty()) {
            Doctor existingDoctor = doctorMapper.selectByDoctorPhone(request.getDoctorPhone().trim());
            if (existingDoctor != null && !existingDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他医生使用");
            }
            doctorPhone = request.getDoctorPhone().trim();
        }

        if (request.getDoctorEmail() != null && !request.getDoctorEmail().trim().isEmpty()) {
            Doctor existingDoctor = doctorMapper.selectByDoctorEmail(request.getDoctorEmail().trim());
            if (existingDoctor != null && !existingDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该邮箱已被其他医生使用");
            }
            doctorEmail = request.getDoctorEmail().trim();
        }

        if (request.getDoctorIntro() != null && !request.getDoctorIntro().trim().isEmpty()) {
            doctorIntro = request.getDoctorIntro().trim();
        }

        doctorMapper.updateDoctorProfile(doctorPhone, doctorEmail, doctorIntro, doctor.getDoctorId());

        return doctorMapper.selectByUserId(userId);
    }

    @Override
    public boolean changePassword(String userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        if (!user.getUserPassword().equals(request.getOldPassword())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "当前密码错误");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码至少6位");
        }

        user.setUserPassword(request.getNewPassword());
        int result = userMapper.updateById(user);

        return result > 0;
    }
}

