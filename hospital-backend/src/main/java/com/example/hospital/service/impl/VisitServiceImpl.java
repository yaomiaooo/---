package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hospital.dto.ReviewResponse;
import com.example.hospital.dto.VisitResponse;
import com.example.hospital.entity.*;
import com.example.hospital.mapper.*;
import com.example.hospital.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 就诊记录服务实现类
 */
@Service
public class VisitServiceImpl implements VisitService {

    @Autowired
    private VisitMapper visitMapper;

    @Autowired
    private UserPatientMapper userPatientMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public List<VisitResponse> getVisitsByUserId(String userId) {
        // 1. 查找该用户关联的所有就诊人
        List<UserPatient> userPatientLinks = userPatientMapper.selectList(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
        );

        if (userPatientLinks.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取所有就诊人ID
        List<String> userPatientIds = userPatientLinks.stream()
                .map(UserPatient::getPatientId)
                .collect(Collectors.toList());

        // 3. 查询这些就诊人的所有预约记录
        List<Appointment> appointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .in(Appointment::getPatientId, userPatientIds)
                        .orderByDesc(Appointment::getCreatedAt)
        );

        if (appointments.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 提取所有预约ID
        List<String> appointmentIds = appointments.stream()
                .map(Appointment::getAppointmentId)
                .collect(Collectors.toList());

        // 5. 查询这些预约对应的就诊记录
        List<Visit> visits = visitMapper.selectList(
                new LambdaQueryWrapper<Visit>()
                        .in(Visit::getAppointmentId, appointmentIds)
                        .orderByDesc(Visit::getVisitTime)
        );

        if (visits.isEmpty()) {
            return Collections.emptyList();
        }

        // 6. 批量查询相关数据
        // 6.1 查询所有就诊人信息
        List<String> visitPatientIds = appointments.stream()
                .map(Appointment::getPatientId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Patient> patientMap = patientMapper.selectBatchIds(visitPatientIds).stream()
                .collect(Collectors.toMap(Patient::getPatientId, p -> p));

        // 6.2 查询所有排班信息
        List<String> scheduleIds = appointments.stream()
                .map(Appointment::getScheduleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Schedule> scheduleMap = scheduleIds.isEmpty() ? Collections.emptyMap() :
                scheduleMapper.selectBatchIds(scheduleIds).stream()
                        .collect(Collectors.toMap(Schedule::getScheduleId, s -> s));

        // 6.3 查询所有医生信息
        List<String> doctorIds = scheduleMap.values().stream()
                .map(Schedule::getDoctorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Doctor> doctorMap = doctorIds.isEmpty() ? Collections.emptyMap() :
                doctorMapper.selectBatchIds(doctorIds).stream()
                        .collect(Collectors.toMap(Doctor::getDoctorId, d -> d));

        // 6.4 查询所有院区信息
        List<String> hospitalIds = visits.stream()
                .map(Visit::getHospitalId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Hospital> hospitalMap = hospitalIds.isEmpty() ? Collections.emptyMap() :
                hospitalMapper.selectBatchIds(hospitalIds).stream()
                        .collect(Collectors.toMap(Hospital::getHospitalId, h -> h));

        // 6.5 查询所有科室信息
        List<String> departmentIds = doctorMap.values().stream()
                .map(Doctor::getDepartmentId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, Department> departmentMap = departmentIds.isEmpty() ? Collections.emptyMap() :
                departmentMapper.selectBatchIds(departmentIds).stream()
                        .collect(Collectors.toMap(Department::getDepartmentId, d -> d));

        // 6.6 查询所有评价信息
        List<String> visitIds = visits.stream()
                .map(Visit::getVisitId)
                .collect(Collectors.toList());

        Map<String, Review> reviewMap = visitIds.isEmpty() ? Collections.emptyMap() :
                reviewMapper.selectList(
                        new LambdaQueryWrapper<Review>()
                                .in(Review::getVisitId, visitIds)
                ).stream()
                        .collect(Collectors.toMap(Review::getVisitId, r -> r));

        // 7. 组装返回数据
        return visits.stream().map(visit -> {
            VisitResponse response = new VisitResponse();
            response.setVisitId(visit.getVisitId());
            response.setAppointmentId(visit.getAppointmentId());
            response.setVisitTime(visit.getVisitTime());
            response.setDiagnosis(visit.getDiagnosis());

            // 查找对应的预约记录
            Appointment appointment = appointments.stream()
                    .filter(a -> a.getAppointmentId().equals(visit.getAppointmentId()))
                    .findFirst()
                    .orElse(null);

            if (appointment != null) {
                // 设置就诊人信息
                Patient patient = patientMap.get(appointment.getPatientId());
                if (patient != null) {
                    response.setPatientId(patient.getPatientId());
                    response.setPatientName(patient.getPatientName());
                }

                // 设置排班信息
                Schedule schedule = scheduleMap.get(appointment.getScheduleId());
                if (schedule != null) {
                    response.setTimeSlot(schedule.getTimeSlot());

                    // 设置医生信息
                    Doctor doctor = doctorMap.get(schedule.getDoctorId());
                    if (doctor != null) {
                        response.setDoctorId(doctor.getDoctorId());
                        response.setDoctorName(doctor.getDoctorName());
                        response.setDoctorTitle(doctor.getTitle());

                        // 设置科室信息
                        if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
                            Department department = departmentMap.get(doctor.getDepartmentId());
                            if (department != null) {
                                response.setDepartmentName(department.getDepartmentName());
                            }
                        }
                    }
                }

                // 设置院区信息
                response.setHospitalId(appointment.getHospitalId());
                Hospital hospital = hospitalMap.get(appointment.getHospitalId());
                if (hospital != null) {
                    response.setHospitalName(hospital.getHospitalName());
                }
            }

            // 设置评价信息
            Review review = reviewMap.get(visit.getVisitId());
            if (review != null) {
                ReviewResponse reviewResponse = new ReviewResponse();
                reviewResponse.setReviewId(review.getReviewId());
                reviewResponse.setVisitId(review.getVisitId());
                reviewResponse.setAppointmentId(visit.getAppointmentId()); // 从visit对象中获取
                reviewResponse.setDoctorId(review.getDoctorId());
                reviewResponse.setRating(review.getRating());
                reviewResponse.setContent(review.getContent());
                reviewResponse.setCreatedAt(review.getReviewTime()); // 从实体类的 reviewTime 字段获取
                response.setReview(reviewResponse);
            }

            return response;
        }).collect(Collectors.toList());
    }
}

