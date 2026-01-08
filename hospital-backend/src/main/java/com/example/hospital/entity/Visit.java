package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visit")
public class Visit {
    @TableId
    private String visitId;        // varchar(32), 主键
    private String appointmentId;  // 外键, 唯一
    private String hospitalId;     // 分片键 [cite: 43]
    private LocalDateTime visitTime; // datetime
    private String diagnosis;      // varchar(1000)
}