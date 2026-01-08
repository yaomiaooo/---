package com.example.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.*;

public interface AdminUserService {

    Page<AdminUserVO> pageUsers(AdminUserQueryRequest req);

    AdminUserVO createUser(AdminUserCreateRequest req);

    AdminUserVO updateUser(String userId, AdminUserUpdateRequest req);

    void resetPassword(String userId);

    AdminUserVO getById(String userId);

    void deleteUser(String userId);
}

