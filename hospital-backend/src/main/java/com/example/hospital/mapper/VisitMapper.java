package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Visit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VisitMapper extends BaseMapper<Visit> {

    /**
     * 更新诊断结果，避免更新分片键visit_time
     * @param diagnosis 新的诊断结果
     * @param visitId visit记录ID
     * @return 影响行数
     */
    @Update("UPDATE visit SET diagnosis = #{diagnosis} WHERE visit_id = #{visitId}")
    int updateDiagnosis(@Param("diagnosis") String diagnosis, @Param("visitId") String visitId);
}

