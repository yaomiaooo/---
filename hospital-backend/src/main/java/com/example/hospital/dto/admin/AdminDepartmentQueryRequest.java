package com.example.hospital.dto.admin;

import lombok.Data;

/**
 * 管理员-科室分页查询条件
 */
@Data
public class AdminDepartmentQueryRequest {

    /**
     * 关键字（匹配科室名称）
     */
    private String keyword;

    /**
     * 院区/医院ID（可选；如果你前端不再需要院区筛选，可不传）
     */
    private String hospitalId;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;
}
