package com.example.hospital.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.admin.*;
import com.example.hospital.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@UserLoginToken(roles = {"ADMIN"})
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<Page<AdminUserVO>> page(AdminUserQueryRequest req) {
        return Result.success(adminUserService.pageUsers(req));
    }

    @GetMapping("/{userId}")
    public Result<AdminUserVO> detail(@PathVariable String userId) {
        return Result.success(adminUserService.getById(userId));
    }

    @Log("新增用户")
    @PostMapping
    public Result<AdminUserVO> create(@RequestBody @Valid AdminUserCreateRequest req) {
        return Result.success(adminUserService.createUser(req));
    }

    @Log("修改用户信息")
    @PutMapping("/{userId}")
    public Result<AdminUserVO> update(@PathVariable String userId, @RequestBody @Valid AdminUserUpdateRequest req) {
        return Result.success(adminUserService.updateUser(userId, req));
    }

    @Log("重置用户密码")
    @PatchMapping("/{userId}/reset-password")
    public Result<Void> resetPassword(@PathVariable String userId) {
        adminUserService.resetPassword(userId);
        return Result.success(null);
    }

    @Log("删除用户")
    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable String userId) {
        adminUserService.deleteUser(userId);
        return Result.success(null);
    }
}

