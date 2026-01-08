package com.example.hospital.config;

import com.example.hospital.common.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有/api/路径的请求
                .excludePathPatterns(
                        "/api/auth/**",        // 放行认证相关接口
                        "/api/hospital/departments", // 放行科室列表接口
                        "/api/hospital/departments/**", // 放行科室详情接口（包含所有子路径）
                        "/api/hospital/list",  // 放行医院列表接口
                        "/api/hospitals/**",   // 放行医院相关接口
                        "/api/doctors",        // 放行医生列表接口（公开查询）
                        "/api/doctors/**",     // 放行医生详情接口（公开查询）
                        "/api/schedules",      // 放行排班查询接口（公开查询，需要doctorId参数）
                        "/api/schedules/outpatient", // 放行门诊排班接口（公开查询）
                        "/api/reviews/doctor/**", // 放行根据医生ID获取评价接口（公开接口）
                        "/error"               // 放行错误页面
                );
    }

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
