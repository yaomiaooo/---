package com.example.hospital.aspect;

import com.example.hospital.common.annotation.Log;
import com.example.hospital.common.DateTimeUtil;
import com.example.hospital.entity.AuditLog;
import com.example.hospital.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 操作日志切面
 * 自动拦截标记了 @Log 注解的方法，记录操作日志
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final AuditLogMapper auditLogMapper;

    /**
     * 定义切点：所有标记了 @Log 注解的方法
     */
    @Pointcut("@annotation(com.example.hospital.common.annotation.Log)")
    public void logPointCut() {}

    /**
     * 环绕通知：在方法执行前后记录日志
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        System.out.println("========== AOP 切面被触发 ==========");
        System.out.println("拦截的方法: " + point.getSignature().toShortString());
        
        long beginTime = System.currentTimeMillis();
        
        // 1. 执行目标方法（真正的业务逻辑）
        Object result = point.proceed();
        
        // 2. 执行完后，计算耗时
        long time = System.currentTimeMillis() - beginTime;
        System.out.println("方法执行耗时: " + time + "ms");

        // 3. 保存日志（同步执行，确保日志能正确保存）
        saveSysLog(point, time);
        
        System.out.println("========== AOP 切面执行完成 ==========");

        return result;
    }

    /**
     * 保存操作日志
     * 注意：audit_log 表是广播表，不需要分片键，所有分片都有相同的数据
     */
    private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            AuditLog auditLog = new AuditLog();

            // 获取注解上的操作描述
            Log logAnnotation = method.getAnnotation(Log.class);
            if (logAnnotation != null && !logAnnotation.value().isEmpty()) {
                auditLog.setAction(logAnnotation.value());
            } else {
                // 如果没有设置描述，使用方法名
                auditLog.setAction(method.getName());
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 从 Request 中获取拦截器存进去的用户信息
                String userId = (String) request.getAttribute("userId");
                String role = (String) request.getAttribute("role");
                String username = (String) request.getAttribute("username");
                
                // 设置操作人信息
                auditLog.setOperatorUserId(userId != null ? userId : "anonymous");
                auditLog.setRoleType(role != null ? role.toLowerCase() : "anonymous");
                
                // audit_log 表是广播表，不需要 hospitalId 作为分片键
                // 但可以保留 hospitalId 字段用于业务查询和统计（可选）
                String hospitalId = extractHospitalId(joinPoint, request);
                if (hospitalId != null && !hospitalId.isEmpty()) {
                    auditLog.setHospitalId(hospitalId);
                }
            } else {
                // 如果没有请求上下文，设置默认值
                auditLog.setOperatorUserId("system");
                auditLog.setRoleType("system");
            }

            // 设置日志ID（使用 UUID，确保唯一性）
            auditLog.setLogId(UUID.randomUUID().toString().replace("-", ""));
            
            // 设置操作时间（使用统一的时间工具类，确保时区一致）
            auditLog.setActionTime(DateTimeUtil.now());

            // 调试日志：打印即将保存的日志信息
            System.out.println("=== 操作日志 ===");
            System.out.println("日志ID: " + auditLog.getLogId());
            System.out.println("操作: " + auditLog.getAction());
            System.out.println("操作人: " + auditLog.getOperatorUserId());
            System.out.println("角色: " + auditLog.getRoleType());
            System.out.println("院区ID: " + (auditLog.getHospitalId() != null ? auditLog.getHospitalId() : "无"));
            System.out.println("操作时间: " + auditLog.getActionTime());
            System.out.println("================");

            // 保存日志到数据库（同步执行，确保日志能正确保存）
            // 由于 audit_log 是广播表，不需要分片键，可以直接插入
            int rows = auditLogMapper.insert(auditLog);
            if (rows > 0) {
                System.out.println("✅ 操作日志保存成功，影响行数: " + rows);
            } else {
                System.err.println("⚠️ 操作日志保存失败，影响行数为 0");
            }
            
        } catch (Exception e) {
            // 日志保存失败不应该影响业务，只打印错误
            // 这样可以确保即使日志系统出现问题，业务功能仍然正常
            System.err.println("❌ 保存操作日志失败: " + e.getMessage());
            System.err.println("异常类型: " + e.getClass().getName());
            e.printStackTrace();
        }
    }

    /**
     * 从请求参数中提取 hospitalId（用于分片路由）
     * 优先从请求参数中获取，其次从请求体（JSON）中获取
     */
    private String extractHospitalId(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        // 1. 从 URL 参数中获取
        String hospitalId = request.getParameter("hospitalId");
        if (hospitalId != null && !hospitalId.isEmpty()) {
            return hospitalId;
        }

        // 2. 从方法参数中获取
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        
        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                if ("hospitalId".equals(paramNames[i]) && args[i] != null) {
                    return args[i].toString();
                }
            }
        }

        // 3. 从请求体中尝试获取（如果是对象参数）
        for (Object arg : args) {
            if (arg != null) {
                try {
                    // 使用反射获取 hospitalId 字段
                    java.lang.reflect.Field field = arg.getClass().getDeclaredField("hospitalId");
                    field.setAccessible(true);
                    Object value = field.get(arg);
                    if (value != null) {
                        return value.toString();
                    }
                } catch (Exception e) {
                    // 忽略反射异常，继续尝试其他方式
                }
            }
        }

        return null;
    }
}

