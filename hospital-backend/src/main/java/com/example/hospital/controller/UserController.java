package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.ChangePasswordRequest;
import com.example.hospital.dto.UserProfileUpdateRequest;
import com.example.hospital.entity.User;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户的个人资料
     * @param request HttpServletRequest，用于获取拦截器中存入的用户ID
     * @return 用户信息
     */
    @UserLoginToken // 需要Token验证
    @GetMapping("/profile")
    public Result<User> getUserProfile(HttpServletRequest request) {
        // 从request中获取由拦截器注入的用户ID
        String userId = (String) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 更新当前登录用户的个人资料
     * @param request HttpServletRequest
     * @param updateRequest 包含更新信息的数据体
     * @return 更新后的用户信息
     */
    @Log("用户修改个人信息")
    @UserLoginToken // 需要Token验证
    @PutMapping("/profile")
    public Result<User> updateUserProfile(HttpServletRequest request, @RequestBody UserProfileUpdateRequest updateRequest) {
        String userId = (String) request.getAttribute("userId");
        User updatedUser = userService.updateUserProfile(userId, updateRequest);
        return Result.success(updatedUser);
    }

    /**
     * 修改用户密码
     * @param request HttpServletRequest，用于获取用户ID
     * @param changePasswordRequest 修改密码请求体（包含旧密码和新密码）
     * @return 操作结果
     */
    @Log("用户修改密码")
    @UserLoginToken // 需要Token验证
    @PutMapping("/password")
    public Result<String> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest changePasswordRequest) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("UserController.changePassword - 从request获取的userId: " + userId);

        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法修改密码");
            return Result.error("用户身份验证失败，请重新登录");
        }

        try {
            boolean success = userService.changePassword(userId, changePasswordRequest);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("修改密码失败，请重试");
            }
        } catch (Exception e) {
            System.err.println("修改密码异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

}

