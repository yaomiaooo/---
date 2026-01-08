package com.example.hospital.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD) // 作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Documented
public @interface Log {
    /**
     * 操作描述，例如："新增用户"、"删除医生"、"修改排班"等
     */
    String value() default "";
}

