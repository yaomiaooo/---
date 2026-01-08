package com.example.hospital.service;

import com.example.hospital.dto.LoginRequest;
import com.example.hospital.dto.RegisterRequest;
import com.example.hospital.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求体
     * @return JWT Token
     */
    String login(LoginRequest loginRequest);

    /**
     * 用户注册
     * @param registerRequest 注册请求体
     * @return 注册后的用户信息
     */
    User register(RegisterRequest registerRequest);

}

