package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
    /**
     * 根据预约ID查询基本预约信息（只选择数据库中存在的字段）
     * @param appointmentId 预约ID
     * @return 预约信息
     */
    @Select("SELECT appointment_id, user_id, patient_id, schedule_id, hospital_id, status, created_at FROM appointment WHERE appointment_id = #{appointmentId}")
    @Results({
        @Result(property = "appointmentId", column = "appointment_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "patientId", column = "patient_id"),
        @Result(property = "scheduleId", column = "schedule_id"),
        @Result(property = "hospitalId", column = "hospital_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at")
    })
    Appointment selectBasicById(String appointmentId);

    /**
     * 根据医生ID查询今日预约列表（包含患者基本信息）
     * 先查询schedule表获取hospital_id，然后JOIN查询
     * @param doctorId 医生ID
     * @param today 今日日期
     * @return 预约列表
     */
    @Select("SELECT a.appointment_id, a.user_id, a.patient_id, a.schedule_id, a.hospital_id, a.status, a.created_at, " +
            "s.time_slot, d.title " +
            "FROM appointment a " +
            "JOIN schedule s ON a.schedule_id = s.schedule_id " +
            "JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "WHERE s.doctor_id = #{doctorId} AND s.work_date = #{today} " +
            "ORDER BY s.time_slot, a.created_at")
    @Results({
        @Result(property = "appointmentId", column = "appointment_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "patientId", column = "patient_id"),
        @Result(property = "scheduleId", column = "schedule_id"),
        @Result(property = "hospitalId", column = "hospital_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "timeSlot", column = "time_slot"),
        @Result(property = "title", column = "title")
    })
    List<Appointment> selectTodayAppointmentsByDoctorId(String doctorId, LocalDate today);

    /**
     * 根据预约ID查询预约（用于分片环境，只返回有效状态的记录）
     * @param appointmentId 预约ID
     * @return 预约列表
     */
    @Select("SELECT * FROM appointment WHERE appointment_id = #{appointmentId} AND status IN ('BOOKED', 'COMPLETED')")
    List<Appointment> selectByAppointmentId(String appointmentId);

    /**
     * 更新预约状态（避免更新分片键）
     * @param status 新状态
     * @param appointmentId 预约ID
     * @return 影响行数
     */
    @Update("UPDATE appointment SET status = #{status} WHERE appointment_id = #{appointmentId}")
    int updateAppointmentStatus(String status, String appointmentId);

    /**
     * 根据预约ID查询完整的预约信息（包含患者、医生、排班信息）
     * @param appointmentId 预约ID
     * @return 预约详细信息
     */
    @Select("SELECT a.appointment_id, a.user_id, a.patient_id, a.schedule_id, a.hospital_id, a.status, a.created_at, " +
            "s.time_slot, d.doctor_name, d.title, d.doctor_phone, d.doctor_email, d.doctor_intro " +
            "FROM appointment a " +
            "JOIN schedule s ON a.schedule_id = s.schedule_id " +
            "JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "WHERE a.appointment_id = #{appointmentId}")
    @Results({
        @Result(property = "appointmentId", column = "appointment_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "patientId", column = "patient_id"),
        @Result(property = "scheduleId", column = "schedule_id"),
        @Result(property = "hospitalId", column = "hospital_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "patientPhone", column = "patient_phone"),
        @Result(property = "timeSlot", column = "time_slot"),
        @Result(property = "doctorName", column = "doctor_name"),
        @Result(property = "title", column = "title"),
        @Result(property = "doctorPhone", column = "doctor_phone"),
        @Result(property = "doctorEmail", column = "doctor_email"),
        @Result(property = "doctorIntro", column = "doctor_intro")
    })
    List<AppointmentDetailDTO> selectAppointmentWithDetails(String appointmentId);
}

