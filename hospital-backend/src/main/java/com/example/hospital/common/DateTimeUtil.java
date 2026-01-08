package com.example.hospital.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 日期时间工具类
 * 统一处理时区，确保所有时间操作都使用 Asia/Shanghai 时区（GMT+8）
 */
public class DateTimeUtil {

    /**
     * 系统默认时区：Asia/Shanghai (GMT+8)
     */
    private static final ZoneId SYSTEM_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 获取当前时间（Asia/Shanghai 时区）
     * 
     * @return 当前时间的 LocalDateTime（Asia/Shanghai 时区）
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(SYSTEM_ZONE);
    }

    /**
     * 获取当前时间的秒数（时间戳）
     * 
     * @return 当前时间的秒数（从 1970-01-01 00:00:00 UTC 开始）
     */
    public static long currentTimeSeconds() {
        return LocalDateTime.now(SYSTEM_ZONE)
                .atZone(SYSTEM_ZONE)
                .toEpochSecond();
    }
}

