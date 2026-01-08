package com.example.hospital.dto;

import lombok.Data;
import java.util.List;

/**
 * 门诊排班响应DTO（按科室分组）
 */
@Data
public class OutpatientScheduleResponse {
    private String departmentId;        // 科室ID
    private String departmentName;      // 科室名称
    private List<DoctorScheduleInfo> doctors; // 该科室的医生排班信息列表
}

