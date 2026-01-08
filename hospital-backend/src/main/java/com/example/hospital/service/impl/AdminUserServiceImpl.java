package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.AdminUserCreateRequest;
import com.example.hospital.dto.admin.AdminUserQueryRequest;
import com.example.hospital.dto.admin.AdminUserUpdateRequest;
import com.example.hospital.dto.admin.AdminUserVO;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;

    /** 默认密码：不传 userPassword 时使用 */
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public Page<AdminUserVO> pageUsers(AdminUserQueryRequest req) {
        Page<User> page = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();

        // keyword：按手机号模糊
        if (StringUtils.hasText(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            qw.like(User::getUserPhone, kw);
        }

        // role：精确匹配（user/doctor/admin）
        if (StringUtils.hasText(req.getRole())) {
            qw.eq(User::getRole, req.getRole().trim());
        }

        // 按创建时间倒序
        qw.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(page, qw);

        Page<AdminUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public AdminUserVO getById(String userId) {
        User db = userMapper.selectById(userId);
        if (db == null) throw new RuntimeException("用户不存在");
        return toVO(db);
    }

    @Override
    @Transactional
    public AdminUserVO createUser(AdminUserCreateRequest req) {
        // 手机号唯一校验
        LambdaQueryWrapper<User> check = new LambdaQueryWrapper<User>()
                .eq(User::getUserPhone, req.getUserPhone());
        if (userMapper.selectCount(check) > 0) {
            throw new RuntimeException("手机号已存在");
        }

        User u = new User();

        // 生成 userId
        u.setUserId(UUID.randomUUID().toString().replace("-", ""));

        u.setUserPhone(req.getUserPhone());

        // role 默认 user
        String role = StringUtils.hasText(req.getRole()) ? req.getRole().trim() : "user";
        u.setRole(role);

        // password 默认 DEFAULT_PASSWORD（使用明文存储，与现有系统保持一致）
        String pwd = StringUtils.hasText(req.getUserPassword()) ? req.getUserPassword() : DEFAULT_PASSWORD;
        u.setUserPassword(pwd);

        // createdAt
        u.setCreatedAt(LocalDateTime.now());

        userMapper.insert(u);
        return toVO(u);
    }

    @Override
    @Transactional
    public AdminUserVO updateUser(String userId, AdminUserUpdateRequest req) {
        User db = userMapper.selectById(userId);
        if (db == null) throw new RuntimeException("用户不存在");

        // 手机号唯一校验（排除自己）
        LambdaQueryWrapper<User> check = new LambdaQueryWrapper<User>()
                .eq(User::getUserPhone, req.getUserPhone())
                .ne(User::getUserId, userId);
        if (userMapper.selectCount(check) > 0) {
            throw new RuntimeException("手机号已存在");
        }

        db.setUserPhone(req.getUserPhone());
        if (StringUtils.hasText(req.getRole())) {
            db.setRole(req.getRole().trim());
        }

        userMapper.updateById(db);
        return toVO(db);
    }

    @Override
    @Transactional
    public void resetPassword(String userId) {
        User db = userMapper.selectById(userId);
        if (db == null) throw new RuntimeException("用户不存在");

        // 使用明文默认密码（与现有系统保持一致）
        db.setUserPassword(DEFAULT_PASSWORD);
        userMapper.updateById(db);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User db = userMapper.selectById(userId);
        if (db == null) {
            throw new RuntimeException("用户不存在");
        }

        // 防呆：避免把管理员账号删了
        if ("admin".equalsIgnoreCase(db.getRole())) {
            throw new RuntimeException("不允许删除管理员账号");
        }

        try {
            int rows = userMapper.deleteById(userId);
            if (rows == 0) {
                throw new RuntimeException("删除失败：记录不存在或已被删除");
            }
        } catch (DataIntegrityViolationException e) {
            // 典型：外键约束（预约/就诊/评价等引用了该用户）
            throw new RuntimeException("删除失败：该用户存在关联数据（预约记录/就诊记录/评价等），请先删除关联数据或改为逻辑删除", e);
        }
    }

    private AdminUserVO toVO(User u) {
        AdminUserVO vo = new AdminUserVO();
        vo.setUserId(u.getUserId());
        vo.setUserPhone(u.getUserPhone());
        vo.setRole(u.getRole());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}

