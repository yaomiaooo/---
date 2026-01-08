package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    // 继承后即可使用 MyBatis-Plus 提供的所有自动 SQL 功能

    /**
     * 根据用户ID查询医生信息
     * @param userId 用户ID
     * @return 医生实体
     */
    @Select("SELECT * FROM doctor WHERE user_id = #{userId}")
    Doctor selectByUserId(String userId);

    /**
     * 根据手机号查询医生（用于唯一性检查）
     * @param doctorPhone 医生手机号
     * @return 医生实体
     */
    @Select("SELECT * FROM doctor WHERE doctor_phone = #{doctorPhone}")
    Doctor selectByDoctorPhone(String doctorPhone);

    /**
     * 根据邮箱查询医生（用于唯一性检查）
     * @param doctorEmail 医生邮箱
     * @return 医生实体
     */
    @Select("SELECT * FROM doctor WHERE doctor_email = #{doctorEmail}")
    Doctor selectByDoctorEmail(String doctorEmail);

    /**
     * 更新医生可修改字段（避免更新分片键）
     * @param doctorPhone 医生电话
     * @param doctorEmail 医生邮箱
     * @param doctorIntro 医生简介
     * @param doctorId 医生ID
     * @return 影响行数
     */
    @Update("UPDATE doctor SET doctor_phone = #{doctorPhone}, doctor_email = #{doctorEmail}, doctor_intro = #{doctorIntro} WHERE doctor_id = #{doctorId}")
    int updateDoctorProfile(String doctorPhone, String doctorEmail, String doctorIntro, String doctorId);
}