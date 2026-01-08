package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.dto.ReviewResponse;
import com.example.hospital.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    /**
     * 根据用户ID查询评价列表，关联查询医生信息
     * 使用 JOIN 查询一次性获取所有相关信息
     * 使用 DISTINCT 确保不返回重复记录
     */
    @Select("SELECT DISTINCT " +
            "r.review_id, " +
            "r.visit_id, " +
            "r.user_id, " +
            "r.hospital_id, " +
            "r.rating, " +
            "r.content, " +
            "r.review_time, " +
            "v.appointment_id, " +
            "s.doctor_id, " +
            "d.doctor_name " +
            "FROM review r " +
            "LEFT JOIN visit v ON r.visit_id = v.visit_id " +
            "LEFT JOIN appointment a ON v.appointment_id = a.appointment_id " +
            "LEFT JOIN schedule s ON a.schedule_id = s.schedule_id " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.review_time DESC")
    @Results({
        @Result(property = "reviewId", column = "review_id"),
        @Result(property = "visitId", column = "visit_id"),
        @Result(property = "appointmentId", column = "appointment_id"),
        @Result(property = "doctorId", column = "doctor_id"),
        @Result(property = "doctorName", column = "doctor_name"),
        @Result(property = "rating", column = "rating"),
        @Result(property = "content", column = "content"),
        @Result(property = "createdAt", column = "review_time")
    })
    List<ReviewResponse> selectReviewsWithDoctorByUserId(@Param("userId") String userId);

    /**
     * 根据医生ID查询评价列表，关联查询医生信息
     * 使用 JOIN 查询一次性获取所有相关信息
     * 使用 DISTINCT 确保不返回重复记录
     */
    @Select("SELECT DISTINCT " +
            "r.review_id, " +
            "r.visit_id, " +
            "r.user_id, " +
            "r.hospital_id, " +
            "r.rating, " +
            "r.content, " +
            "r.review_time, " +
            "v.appointment_id, " +
            "s.doctor_id, " +
            "d.doctor_name " +
            "FROM review r " +
            "LEFT JOIN visit v ON r.visit_id = v.visit_id " +
            "LEFT JOIN appointment a ON v.appointment_id = a.appointment_id " +
            "LEFT JOIN schedule s ON a.schedule_id = s.schedule_id " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "WHERE s.doctor_id = #{doctorId} " +
            "ORDER BY r.review_time DESC")
    @Results({
        @Result(property = "reviewId", column = "review_id"),
        @Result(property = "visitId", column = "visit_id"),
        @Result(property = "appointmentId", column = "appointment_id"),
        @Result(property = "doctorId", column = "doctor_id"),
        @Result(property = "doctorName", column = "doctor_name"),
        @Result(property = "rating", column = "rating"),
        @Result(property = "content", column = "content"),
        @Result(property = "createdAt", column = "review_time")
    })
    List<ReviewResponse> selectReviewsByDoctorId(@Param("doctorId") String doctorId);
}

