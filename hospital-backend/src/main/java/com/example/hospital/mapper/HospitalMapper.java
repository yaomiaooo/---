package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Hospital;
import org.apache.ibatis.annotations.Mapper;

/**
 * Hospital Mapper 接口
 */
@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {
}

