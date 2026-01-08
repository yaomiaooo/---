package com.example.hospital.service;

import com.example.hospital.dto.VisitResponse;

import java.util.List;

/**
 * 就诊记录服务接口
 */
public interface VisitService {

    /**
     * 获取当前用户关联的所有就诊人的就诊记录
     * @param userId 用户ID
     * @return 就诊记录列表
     */
    List<VisitResponse> getVisitsByUserId(String userId);
}

