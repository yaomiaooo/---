package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.dto.admin.AdminDepartmentVO;
import com.example.hospital.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Department Mapper 接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    @Select("""
            SELECT
                department_id AS departmentId,
                department_name AS departmentName
            FROM department
            WHERE hospital_id = #{hospitalId}
            ORDER BY department_name ASC
            """)
    List<AdminDepartmentVO> selectOptionsByHospitalId(@Param("hospitalId") String hospitalId);
}

