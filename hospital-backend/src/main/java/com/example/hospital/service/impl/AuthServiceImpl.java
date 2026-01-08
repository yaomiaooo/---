package com.example.hospital.service.impl;

import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.LoginRequest;
import com.example.hospital.dto.RegisterRequest;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.AuthService;
import com.example.hospital.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String login(LoginRequest loginRequest) {
        // 1. 根据手机号查询用户
        User user = userMapper.selectByUserPhone(loginRequest.getUserPhone());

        // 2. 校验用户是否存在
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 3. 校验密码 (注意：这里是明文比较，生产环境应使用加密密码)
        if (!user.getUserPassword().equals(loginRequest.getUserPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "密码错误");
        }

        // 4. 校验角色
        if (!user.getRole().equalsIgnoreCase(loginRequest.getRole())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "角色选择错误");
        }

        // 5. 登录成功，生成JWT Token
        return jwtUtils.generateToken(user.getUserId(), user.getUserPhone(), user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest registerRequest) {
        // 1. 验证请求参数
        if (registerRequest.getUserPhone() == null || registerRequest.getUserPhone().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "手机号不能为空");
        }
        if (registerRequest.getUserPassword() == null || registerRequest.getUserPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码不能为空");
        }

        String userPhone = registerRequest.getUserPhone().trim();
        String userPassword = registerRequest.getUserPassword().trim();

        // 2. 验证手机号格式（11位数字，以1开头）
        if (!userPhone.matches("^1\\d{10}$")) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "手机号格式不正确（应为11位数字，以1开头）");
        }

        // 3. 验证密码长度（至少6位）
        if (userPassword.length() < 6) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码长度至少为6位");
        }

        // 4. 检查手机号是否已被注册
        User existingUser = userMapper.selectByUserPhone(userPhone);
        if (existingUser != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被注册");
        }

        // 5. 生成唯一的用户ID（以U开头，VARCHAR(32)，最多32位）
        String userId = generateUniqueUserId();

        // 6. 创建新用户
        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setUserPhone(userPhone);
        newUser.setUserPassword(userPassword); // 注意：当前系统使用明文存储，生产环境应使用BCrypt加密
        newUser.setRole("user"); // 只能创建普通用户，不能创建医生和管理员
        newUser.setCreatedAt(LocalDateTime.now());

        // 7. 保存到数据库
        int insertResult = userMapper.insert(newUser);
        if (insertResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "注册失败，请重试");
        }

        System.out.println("用户注册成功 - userId: " + userId + ", userPhone: " + userPhone);

        // 8. 返回用户信息（密码脱敏）
        newUser.setUserPassword(null);
        return newUser;
    }

    /**
     * 生成唯一的用户ID（以U开头，长度不超过32）
     * 格式：U + 时间戳（yyyyMMddHHmmss） + 随机数（4位）
     * 总长度：1 + 14 + 4 = 19 位（远小于32位限制）
     *
     * @return 唯一的用户ID
     */
    private String generateUniqueUserId() {
        String userId;
        int maxAttempts = 10; // 最多尝试10次
        int attempts = 0;

        do {
            // 生成ID：U + 时间戳（14位） + 随机数（4位）
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = String.format("%04d", (int)(Math.random() * 10000));
            userId = "U" + timestamp + random;

            // 检查ID是否已存在
            User existing = userMapper.selectById(userId);
            if (existing == null) {
                return userId; // ID唯一，返回
            }

            attempts++;
            if (attempts >= maxAttempts) {
                // 如果尝试10次都重复，使用UUID（去掉连字符）
                String uuid = UUID.randomUUID().toString().replace("-", "");
                // 取前31位，加上U前缀，总共32位
                userId = "U" + uuid.substring(0, 31);
                // 再次检查唯一性
                existing = userMapper.selectById(userId);
                if (existing == null) {
                    return userId;
                }
                // 如果还是重复，抛出异常
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "生成用户ID失败，请稍后重试");
            }
        } while (true);
    }
}

