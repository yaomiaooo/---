package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("schedule")
public class Schedule {
    @TableId
    private String scheduleId;   // varchar(32), 主键
    private String doctorId;     // 外键
    private String hospitalId;   // 分片键（对应所属院区） [cite: 30, 33]
    private LocalDate workDate;  // Date
    private String timeSlot;     // '08:00-10:00'等
    private Integer totalQuota;  // int
    private Integer bookedCount; // int
}