package com.example.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.hospital.entity.AuditLog;

public interface AdminAuditLogService {

    IPage<AuditLog> pageLogs(
            int page,
            int size,
            String roleType,
            String keyword
    );
}

