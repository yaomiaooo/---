package com.example.hospital.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Schedule;
import com.example.hospital.mapper.AppointmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 半连接技术示例
 * 
 * 半连接（Semi-Join）是一种优化技术，用于避免在分库分表场景中的全表扫描。
 * 
 * 原理：
 * 1. 先在一个表中查询出符合条件的ID列表
 * 2. 然后在另一个表中使用 IN 查询，只查询这些ID对应的记录
 * 
 * 优势：
 * - 避免跨分片的 JOIN 操作（分库分表中 JOIN 性能很差）
 * - 减少数据传输量
 * - 提高查询性能
 * 
 * 应用场景：
 * - 按科室查询预约：department -> doctor -> schedule -> appointment
 * - 按医生查询预约：doctor -> schedule -> appointment
 * - 按排班查询预约：schedule -> appointment
 */
@Service
@RequiredArgsConstructor
public class SemiJoinExample {

    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;
    private final ScheduleMapper scheduleMapper;

    /**
     * 示例1：按科室查询预约（半连接技术）
     * 
     * 传统方式（不推荐）：
     * SELECT a.* FROM appointment a
     * JOIN schedule s ON a.schedule_id = s.schedule_id
     * JOIN doctor d ON s.doctor_id = d.doctor_id
     * WHERE d.department_id = 'xxx'
     * 
     * 问题：在分库分表中，JOIN 操作会触发全表扫描，性能很差
     * 
     * 半连接方式（推荐）：
     * 1. 先查询 department -> doctor，获取 doctorIds
     * 2. 再查询 doctor -> schedule，获取 scheduleIds
     * 3. 最后查询 schedule -> appointment，使用 schedule_id IN (...)
     */
    public List<Appointment> getAppointmentsByDepartment(String hospitalId, String departmentId) {
        // 步骤1：根据 departmentId 查询 doctorIds（半连接的第一步）
        List<Doctor> doctors = doctorMapper.selectList(
            new QueryWrapper<Doctor>()
                .eq("hospital_id", hospitalId)  // 必须带分片键，避免广播查询
                .eq("department_id", departmentId)
                .select("doctor_id")  // 只查询需要的字段
        );
        
        if (doctors.isEmpty()) {
            return new ArrayList<>();  // 如果没有医生，直接返回空列表
        }
        
        // 提取 doctorIds
        Set<String> doctorIds = new HashSet<>();
        for (Doctor doctor : doctors) {
            if (doctor.getDoctorId() != null) {
                doctorIds.add(doctor.getDoctorId());
            }
        }
        
        // 步骤2：根据 doctorIds 查询 scheduleIds（半连接的第二步）
        List<Schedule> schedules = scheduleMapper.selectList(
            new QueryWrapper<Schedule>()
                .eq("hospital_id", hospitalId)  // 必须带分片键
                .in("doctor_id", doctorIds)
                .select("schedule_id")
        );
        
        if (schedules.isEmpty()) {
            return new ArrayList<>();  // 如果没有排班，直接返回空列表
        }
        
        // 提取 scheduleIds
        Set<String> scheduleIds = new HashSet<>();
        for (Schedule schedule : schedules) {
            if (schedule.getScheduleId() != null) {
                scheduleIds.add(schedule.getScheduleId());
            }
        }
        
        // 步骤3：根据 scheduleIds 查询 appointments（半连接的第三步）
        // 使用 IN 查询，避免 JOIN
        return appointmentMapper.selectList(
            new QueryWrapper<Appointment>()
                .eq("hospital_id", hospitalId)  // 必须带分片键
                .in("schedule_id", scheduleIds)
        );
    }

    /**
     * 示例2：按医生查询预约（半连接技术）
     * 
     * 流程：doctor -> schedule -> appointment
     */
    public List<Appointment> getAppointmentsByDoctor(String hospitalId, String doctorId) {
        // 步骤1：根据 doctorId 查询 scheduleIds
        List<Schedule> schedules = scheduleMapper.selectList(
            new QueryWrapper<Schedule>()
                .eq("hospital_id", hospitalId)
                .eq("doctor_id", doctorId)
                .select("schedule_id")
        );
        
        if (schedules.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取 scheduleIds
        Set<String> scheduleIds = new HashSet<>();
        for (Schedule schedule : schedules) {
            if (schedule.getScheduleId() != null) {
                scheduleIds.add(schedule.getScheduleId());
            }
        }
        
        // 步骤2：根据 scheduleIds 查询 appointments
        return appointmentMapper.selectList(
            new QueryWrapper<Appointment>()
                .eq("hospital_id", hospitalId)
                .in("schedule_id", scheduleIds)
        );
    }

    /**
     * 示例3：按排班查询预约（直接查询，无需半连接）
     * 
     * 如果查询条件已经是最终表的关联键，可以直接查询，无需半连接
     */
    public List<Appointment> getAppointmentsBySchedule(String hospitalId, String scheduleId) {
        return appointmentMapper.selectList(
            new QueryWrapper<Appointment>()
                .eq("hospital_id", hospitalId)
                .eq("schedule_id", scheduleId)
        );
    }

    /**
     * 示例4：批量 IN 查询优化
     * 
     * 当 IN 列表很大时（超过 800 个），需要分批查询，避免 SQL 过长
     */
    public List<Appointment> getAppointmentsByScheduleIdsBatch(String hospitalId, Set<String> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> scheduleIdList = new ArrayList<>(scheduleIds);
        
        // 如果数量较少，直接查询
        if (scheduleIdList.size() <= 800) {
            return appointmentMapper.selectList(
                new QueryWrapper<Appointment>()
                    .eq("hospital_id", hospitalId)
                    .in("schedule_id", scheduleIdList)
            );
        }
        
        // 如果数量很大，分批查询
        List<Appointment> allAppointments = new ArrayList<>();
        int batchSize = 800;
        int i = 0;
        
        while (i < scheduleIdList.size()) {
            int end = Math.min(i + batchSize, scheduleIdList.size());
            List<String> batch = scheduleIdList.subList(i, end);
            
            List<Appointment> batchAppointments = appointmentMapper.selectList(
                new QueryWrapper<Appointment>()
                    .eq("hospital_id", hospitalId)
                    .in("schedule_id", batch)
            );
            
            allAppointments.addAll(batchAppointments);
            i = end;
        }
        
        return allAppointments;
    }
}

