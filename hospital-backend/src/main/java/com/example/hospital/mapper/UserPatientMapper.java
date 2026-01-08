package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.UserPatient;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserPatient Mapper 接口
 */
@Mapper
public interface UserPatientMapper extends BaseMapper<UserPatient> {
}

