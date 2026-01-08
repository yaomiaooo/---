package com.example.hospital;

import org.mybatis.spring.annotation.MapperScan; // 必须手动加上这一行
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.example.hospital.mapper")
@EnableAsync // 启用异步支持，用于异步保存日志
public class HospitalBackendApplication {
    public static void main(String[] args) {
        // 设置 JVM 默认时区为 Asia/Shanghai (GMT+8)，确保所有时间操作都使用正确的时区
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.out.println("系统时区已设置为: " + TimeZone.getDefault().getID());
        
        SpringApplication.run(HospitalBackendApplication.class, args);
    }
}