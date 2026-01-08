package com.example.hospital.dto;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.service.AuthService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author YJJ
 * @version 1.0
 */


/**
 * 登录请求的数据传输对象
 */
@Data
public class LoginRequest {
    private String userPhone;
    private String userPassword;
    private String role;

    /**
     * 认证控制器
     * 负责处理登录、注册等请求
     */
    @RestController
    @RequestMapping("/api/auth")
    public static class AuthController {

        @Autowired
        private AuthService authService;

        /**
         * 用户登录接口
         * @param loginRequest 包含手机号、密码和角色的请求体
         * @return 包含JWT Token的Result对象
         */
        @PassToken // 这个注解表示该接口不需要Token验证
        @PostMapping("/login")
        public Result<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
            String token = authService.login(loginRequest);
            // 将Token包装在一个Map中返回，方便前端解析
            return Result.success(Map.of("token", token));
        }

        /**
         * 用户注册接口
         * @param registerRequest 包含手机号和密码的注册请求体
         * @return 注册后的用户信息
         */
        @PassToken // 这个注解表示该接口不需要Token验证
        @PostMapping("/register")
        public Result<com.example.hospital.entity.User> register(@RequestBody com.example.hospital.dto.RegisterRequest registerRequest) {
            com.example.hospital.entity.User user = authService.register(registerRequest);
            return Result.success(user);
        }

    }
}

