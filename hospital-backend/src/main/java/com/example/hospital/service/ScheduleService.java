package com.example.hospital.service;

import com.example.hospital.dto.OutpatientScheduleResponse;
import com.example.hospital.dto.ScheduleResponse;
import com.example.hospital.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务接口
 */
public interface ScheduleService {

    /**
     * 根据医生ID和日期范围获取排班信息
     * @param doctorId 医生ID
     * @param hospitalId 院区ID（可选，用于分片）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 排班列表
     */
    List<ScheduleResponse> getSchedulesByDoctor(String doctorId, String hospitalId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取门诊排班信息（按科室分组）
     * @param hospitalId 院区ID（必填）
     * @param titleFilter 职称筛选（可选）："all"=全部, "normal"=普通门诊（医师、主任）, "expert"=专家门诊（专家）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 按科室分组的排班列表
     */
    List<OutpatientScheduleResponse> getOutpatientSchedules(String hospitalId, String titleFilter, LocalDate startDate, LocalDate endDate);

    /**
     * 根据医生ID获取排班信息
     * @param doctorId 医生ID
     * @return 排班列表
     */
    List<Schedule> getSchedulesByDoctorId(String doctorId);

    /**
     * 根据医生ID和日期范围获取排班信息
     * @param doctorId 医生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<Schedule> getSchedulesByDoctorIdAndDateRange(String doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据医生ID、日期范围和时间段获取排班信息
     * @param doctorId 医生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeSlot 时间段
     * @return 排班列表
     */
    List<Schedule> getSchedulesByDoctorIdAndDateRangeAndTimeSlot(String doctorId, LocalDate startDate, LocalDate endDate, String timeSlot);

    /**
     * 创建排班
     * @param schedule 排班信息
     * @return 创建的排班
     */
    Schedule createSchedule(Schedule schedule);

    /**
     * 更新排班
     * @param schedule 排班信息
     * @return 更新后的排班
     */
    Schedule updateSchedule(Schedule schedule);

    /**
     * 删除排班
     * @param scheduleId 排班ID
     */
    void deleteSchedule(String scheduleId);
}

