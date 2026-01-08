package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.entity.AuditLog;
import com.example.hospital.mapper.AuditLogMapper;
import com.example.hospital.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Override
    public IPage<AuditLog> pageLogs(int page, int size, String roleType, String keyword) {
        LambdaQueryWrapper<AuditLog> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(roleType)) {
            qw.eq(AuditLog::getRoleType, roleType);
        }
        if (StringUtils.hasText(keyword)) {
            qw.like(AuditLog::getAction, keyword);
        }

        qw.orderByDesc(AuditLog::getActionTime);

        return auditLogMapper.selectPage(
                new Page<>(page, size),
                qw
        );
    }
}

