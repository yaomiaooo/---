package com.example.hospital.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 医生排班信息
 */
@Data
public class DoctorScheduleInfo {
    private String doctorId;            // 医生ID
    private String doctorName;          // 医生姓名
    private String title;               // 职称
    private Map<String, List<TimeSlotSchedule>> scheduleMap; // 日期 -> 时间段排班列表
}

