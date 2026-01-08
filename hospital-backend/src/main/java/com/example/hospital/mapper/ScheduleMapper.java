package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
    /**
     * 根据医生ID查询排班信息
     * @param doctorId 医生ID
     * @return 排班列表
     */
    @Select("SELECT * FROM schedule WHERE doctor_id = #{doctorId} ORDER BY work_date, time_slot")
    List<Schedule> selectByDoctorId(String doctorId);

    /**
     * 根据日期范围和医生ID查询排班信息
     * @param doctorId 医生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    @Select("SELECT * FROM schedule WHERE doctor_id = #{doctorId} AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date, time_slot")
    List<Schedule> selectByDoctorIdAndDateRange(String doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期范围、医生ID和时间段查询排班信息
     * @param doctorId 医生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeSlot 时间段
     * @return 排班列表
     */
    @Select("SELECT * FROM schedule WHERE doctor_id = #{doctorId} AND work_date BETWEEN #{startDate} AND #{endDate} AND time_slot = #{timeSlot} ORDER BY work_date, time_slot")
    List<Schedule> selectByDoctorIdAndDateRangeAndTimeSlot(String doctorId, LocalDate startDate, LocalDate endDate, String timeSlot);
}

