package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Patient Mapper 接口
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    /**
     * 根据手机号查询患者
     * @param patientPhone 手机号
     * @return 患者实体
     */
    @Select("SELECT * FROM patient WHERE patient_phone = #{patientPhone}")
    Patient selectByPatientPhone(String patientPhone);
}

