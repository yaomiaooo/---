package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.VisitResponse;
import com.example.hospital.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 就诊记录控制器
 */
@RestController
@RequestMapping("/api/visits")
@UserLoginToken // 需要登录访问
public class VisitController {

    @Autowired
    private VisitService visitService;

    /**
     * 获取当前登录用户的所有就诊记录
     * @param request HttpServletRequest，用于获取用户ID
     * @return 就诊记录列表
     */
    @GetMapping
    public Result<List<VisitResponse>> getMyVisits(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("VisitController.getMyVisits - 从request获取的userId: " + userId);

        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法获取就诊记录");
            return Result.error("用户身份验证失败，请重新登录");
        }

        List<VisitResponse> visits = visitService.getVisitsByUserId(userId);
        return Result.success(visits);
    }
}

