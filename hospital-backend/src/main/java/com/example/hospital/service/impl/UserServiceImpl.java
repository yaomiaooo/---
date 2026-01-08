package com.example.hospital.service.impl;

import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.ChangePasswordRequest;
import com.example.hospital.dto.UserProfileUpdateRequest;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.PatientMapper;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public User getUserById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }
        // 密码脱敏
        user.setUserPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务管理，确保数据一致性
    public User updateUserProfile(String userId, UserProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 更新手机号 (暂不处理realName)
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            String newPhone = request.getPhone().trim();
            String oldPhone = user.getUserPhone();
            
            // 如果手机号没有变化，直接返回
            if (newPhone.equals(oldPhone)) {
                System.out.println("手机号未变化，无需更新。userId: " + userId);
                user.setUserPassword(null);
                return user;
            }
            
            System.out.println("准备更新用户手机号。userId: " + userId + ", 旧手机号: " + oldPhone + ", 新手机号: " + newPhone);
            
            // 检查新手机号是否已被其他用户注册
            User existingUser = userMapper.selectByUserPhone(newPhone);
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他用户注册");
            }
            
            // 检查新手机号是否已被患者使用
            Patient existingPatient = patientMapper.selectByPatientPhone(newPhone);
            if (existingPatient != null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被患者使用");
            }
            
            // 更新手机号
            user.setUserPhone(newPhone);
        }

        // 执行数据库更新操作
        int updateResult = userMapper.updateById(user);
        System.out.println("更新用户信息结果。userId: " + userId + ", 影响行数: " + updateResult);
        
        if (updateResult <= 0) {
            System.err.println("警告：更新用户信息失败，影响行数为0。userId: " + userId);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新用户信息失败");
        }
        
        // 验证更新是否成功（可选，用于调试）
        User updatedUser = userMapper.selectById(userId);
        if (updatedUser != null) {
            System.out.println("更新后的用户手机号: " + updatedUser.getUserPhone());
        }
        
        // 返回更新后的用户信息，密码脱敏
        user.setUserPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(String userId, ChangePasswordRequest request) {
        // 1. 验证请求参数
        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请输入旧密码");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请输入新密码");
        }

        String oldPassword = request.getOldPassword().trim();
        String newPassword = request.getNewPassword().trim();

        // 2. 验证新密码长度（建议至少6位）
        if (newPassword.length() < 6) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码长度至少为6位");
        }

        // 3. 验证新密码不能与旧密码相同
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码不能与旧密码相同");
        }

        // 4. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 5. 验证旧密码是否正确
        // 注意：当前系统使用明文密码存储，生产环境应使用BCrypt等加密方式
        if (!user.getUserPassword().equals(oldPassword)) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "旧密码错误");
        }

        // 6. 更新密码
        // 注意：当前系统使用明文存储，生产环境应使用BCryptPasswordEncoder加密
        user.setUserPassword(newPassword);
        int updateResult = userMapper.updateById(user);

        if (updateResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "修改密码失败，请重试");
        }

        System.out.println("密码修改成功 - userId: " + userId);
        return true;
    }
}

