package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hospital.dto.DoctorScheduleInfo;
import com.example.hospital.dto.OutpatientScheduleResponse;
import com.example.hospital.dto.ScheduleResponse;
import com.example.hospital.dto.TimeSlotSchedule;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Schedule;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.service.ScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 排班服务实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public List<ScheduleResponse> getSchedulesByDoctor(String doctorId, String hospitalId, LocalDate startDate, LocalDate endDate) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按医生ID筛选
        if (doctorId != null && !doctorId.trim().isEmpty()) {
            queryWrapper.eq(Schedule::getDoctorId, doctorId);
        }
        
        // 按院区ID筛选（用于分片）
        if (hospitalId != null && !hospitalId.trim().isEmpty()) {
            queryWrapper.eq(Schedule::getHospitalId, hospitalId);
        }
        
        // 按日期范围筛选
        if (startDate != null) {
            queryWrapper.ge(Schedule::getWorkDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(Schedule::getWorkDate, endDate);
        }
        
        // 只查询未来的排班（workDate >= 今天）
        queryWrapper.ge(Schedule::getWorkDate, LocalDate.now());
        
        // 按日期排序
        queryWrapper.orderByAsc(Schedule::getWorkDate);
        
        // 2. 查询排班列表
        List<Schedule> schedules = scheduleMapper.selectList(queryWrapper);
        
        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 3. 转换为响应DTO
        return schedules.stream()
                .map(schedule -> {
                    ScheduleResponse response = new ScheduleResponse();
                    BeanUtils.copyProperties(schedule, response);
                    response.setScheduleId(schedule.getScheduleId());
                    response.setDoctorId(schedule.getDoctorId());
                    response.setHospitalId(schedule.getHospitalId());
                    response.setWorkDate(schedule.getWorkDate());
                    response.setTimeSlot(schedule.getTimeSlot());
                    response.setTotalQuota(schedule.getTotalQuota());
                    response.setBookedCount(schedule.getBookedCount());
                    
                    // 计算可预约数
                    int availableCount = (schedule.getTotalQuota() != null ? schedule.getTotalQuota() : 0) 
                                       - (schedule.getBookedCount() != null ? schedule.getBookedCount() : 0);
                    response.setAvailableCount(Math.max(0, availableCount));
                    response.setIsAvailable(availableCount > 0);
                    
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<OutpatientScheduleResponse> getOutpatientSchedules(String hospitalId, String titleFilter, LocalDate startDate, LocalDate endDate) {
        if (hospitalId == null || hospitalId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 先查询该院区的所有科室（无论有没有医生）
        LambdaQueryWrapper<com.example.hospital.entity.Department> deptQueryWrapper = new LambdaQueryWrapper<>();
        deptQueryWrapper.eq(com.example.hospital.entity.Department::getHospitalId, hospitalId);
        List<com.example.hospital.entity.Department> allDepartments = departmentMapper.selectList(deptQueryWrapper);

        if (allDepartments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查询该院区的所有医生（根据职称筛选）
        LambdaQueryWrapper<Doctor> doctorQueryWrapper = new LambdaQueryWrapper<>();
        doctorQueryWrapper.eq(Doctor::getHospitalId, hospitalId);

        // 根据职称筛选
        if (titleFilter != null && !titleFilter.trim().isEmpty()) {
            if ("normal".equalsIgnoreCase(titleFilter)) {
                // 普通门诊：医师、主任
                doctorQueryWrapper.and(wrapper -> wrapper
                        .like(Doctor::getTitle, "医师")
                        .or()
                        .like(Doctor::getTitle, "主任")
                );
            } else if ("expert".equalsIgnoreCase(titleFilter)) {
                // 专家门诊：专家
                doctorQueryWrapper.like(Doctor::getTitle, "专家");
            }
            // "all" 或其他值：不筛选，显示全部
        }

        List<Doctor> doctors = doctorMapper.selectList(doctorQueryWrapper);

        // 3. 提取所有医生ID（如果有医生的话）
        List<String> doctorIds = doctors.stream()
                .map(Doctor::getDoctorId)
                .collect(Collectors.toList());

        // 4. 查询这些医生的排班信息（如果有医生的话）
        List<Schedule> schedules = Collections.emptyList();
        if (!doctorIds.isEmpty()) {
            LambdaQueryWrapper<Schedule> scheduleQueryWrapper = new LambdaQueryWrapper<>();
            scheduleQueryWrapper.eq(Schedule::getHospitalId, hospitalId)
                    .in(Schedule::getDoctorId, doctorIds);

            // 设置日期范围（默认未来7天）
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            if (endDate == null) {
                endDate = startDate.plusDays(6); // 默认7天
            }
            scheduleQueryWrapper.ge(Schedule::getWorkDate, startDate)
                    .le(Schedule::getWorkDate, endDate)
                    .orderByAsc(Schedule::getWorkDate)
                    .orderByAsc(Schedule::getTimeSlot);

            schedules = scheduleMapper.selectList(scheduleQueryWrapper);
        }

        // 5. 按科室分组医生
        Map<String, List<Doctor>> departmentDoctorMap = doctors.stream()
                .filter(d -> d.getDepartmentId() != null && !d.getDepartmentId().trim().isEmpty())
                .collect(Collectors.groupingBy(Doctor::getDepartmentId));

        // 6. 按医生分组排班
        Map<String, List<Schedule>> doctorScheduleMap = schedules.stream()
                .collect(Collectors.groupingBy(Schedule::getDoctorId));

        // 7. 组装返回数据 - 遍历所有科室，确保所有科室都显示
        List<OutpatientScheduleResponse> result = new ArrayList<>();
        for (com.example.hospital.entity.Department department : allDepartments) {
            String deptId = department.getDepartmentId();
            
            OutpatientScheduleResponse deptResponse = new OutpatientScheduleResponse();
            deptResponse.setDepartmentId(deptId);
            deptResponse.setDepartmentName(department.getDepartmentName());

            // 获取该科室的医生（如果有）
            List<Doctor> deptDoctors = departmentDoctorMap.getOrDefault(deptId, Collections.emptyList());

            // 组装该科室的医生排班信息
            List<DoctorScheduleInfo> doctorScheduleList = new ArrayList<>();
            for (Doctor doctor : deptDoctors) {
                List<Schedule> doctorSchedules = doctorScheduleMap.getOrDefault(doctor.getDoctorId(), Collections.emptyList());
                if (doctorSchedules.isEmpty()) {
                    continue; // 如果没有排班，跳过该医生
                }

                DoctorScheduleInfo doctorInfo = new DoctorScheduleInfo();
                doctorInfo.setDoctorId(doctor.getDoctorId());
                doctorInfo.setDoctorName(doctor.getDoctorName());
                doctorInfo.setTitle(doctor.getTitle());

                // 按日期分组排班
                Map<String, List<TimeSlotSchedule>> dateScheduleMap = new LinkedHashMap<>();
                for (Schedule schedule : doctorSchedules) {
                    String dateKey = schedule.getWorkDate().format(DateTimeFormatter.ofPattern("MM.dd"));
                    dateScheduleMap.computeIfAbsent(dateKey, k -> new ArrayList<>());

                    TimeSlotSchedule timeSlotSchedule = new TimeSlotSchedule();
                    timeSlotSchedule.setTimeSlot(schedule.getTimeSlot());
                    
                    // 判断时段（am/pm）
                    // 时间段格式如 "08:00-10:00" 或 "13:30-17:00"
                    String timeSlot = schedule.getTimeSlot();
                    if (timeSlot != null && timeSlot.contains("-")) {
                        String startTime = timeSlot.split("-")[0].trim();
                        // 上午：08:00, 09:00, 10:00, 11:00, 12:00
                        // 下午：13:00 及以后
                        if (startTime.startsWith("08:") || startTime.startsWith("09:") || 
                            startTime.startsWith("10:") || startTime.startsWith("11:") || 
                            startTime.startsWith("12:")) {
                            timeSlotSchedule.setPeriod("am");
                        } else {
                            timeSlotSchedule.setPeriod("pm");
                        }
                    } else {
                        // 默认根据时间判断
                        timeSlotSchedule.setPeriod("pm");
                    }

                    int availableCount = (schedule.getTotalQuota() != null ? schedule.getTotalQuota() : 0)
                                       - (schedule.getBookedCount() != null ? schedule.getBookedCount() : 0);
                    timeSlotSchedule.setAvailableCount(Math.max(0, availableCount));
                    timeSlotSchedule.setIsAvailable(availableCount > 0);

                    dateScheduleMap.get(dateKey).add(timeSlotSchedule);
                }

                doctorInfo.setScheduleMap(dateScheduleMap);
                doctorScheduleList.add(doctorInfo);
            }

            // ✅ 关键修改：无论是否有医生，都添加到结果中
            deptResponse.setDoctors(doctorScheduleList); // 如果没有医生，列表为空
            result.add(deptResponse);
        }

        return result;
    }

    @Override
    public List<Schedule> getSchedulesByDoctorId(String doctorId) {
        return scheduleMapper.selectByDoctorId(doctorId);
    }

    @Override
    public List<Schedule> getSchedulesByDoctorIdAndDateRange(String doctorId, LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectByDoctorIdAndDateRange(doctorId, startDate, endDate);
    }

    @Override
    public List<Schedule> getSchedulesByDoctorIdAndDateRangeAndTimeSlot(String doctorId, LocalDate startDate, LocalDate endDate, String timeSlot) {
        return scheduleMapper.selectByDoctorIdAndDateRangeAndTimeSlot(doctorId, startDate, endDate, timeSlot);
    }

    @Override
    public Schedule createSchedule(Schedule schedule) {
        // 生成UUID作为主键
        schedule.setScheduleId(UUID.randomUUID().toString().replace("-", ""));

        // 如果没有设置bookedCount，默认为0
        if (schedule.getBookedCount() == null) {
            schedule.setBookedCount(0);
        }

        scheduleMapper.insert(schedule);
        return schedule;
    }

    @Override
    public Schedule updateSchedule(Schedule schedule) {
        scheduleMapper.updateById(schedule);
        return schedule;
    }

    @Override
    public void deleteSchedule(String scheduleId) {
        scheduleMapper.deleteById(scheduleId);
    }
}

