package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.common.annotation.Log;
import com.example.hospital.dto.CreateReviewRequest;
import com.example.hospital.dto.ReviewResponse;
import com.example.hospital.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/reviews")
@UserLoginToken // 需要登录访问
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 创建评价
     * @param request HttpServletRequest，用于获取用户ID
     * @param createRequest 创建评价请求
     * @return 创建的评价信息
     */
    @Log("用户创建评价")
    @PostMapping
    public Result<ReviewResponse> createReview(HttpServletRequest request, @RequestBody CreateReviewRequest createRequest) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("ReviewController.createReview - 从request获取的userId: " + userId);

        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法创建评价");
            return Result.error("用户身份验证失败，请重新登录");
        }

        try {
            ReviewResponse review = reviewService.createReview(userId, createRequest);
            return Result.success(review);
        } catch (Exception e) {
            System.err.println("创建评价异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据预约ID获取评价
     * @param appointmentId 预约ID
     * @return 评价信息
     */
    @GetMapping("/appointment/{appointmentId}")
    public Result<ReviewResponse> getReviewByAppointmentId(@PathVariable String appointmentId) {
        ReviewResponse review = reviewService.getReviewByAppointmentId(appointmentId);
        if (review == null) {
            return Result.error("未找到该预约的评价信息");
        }
        return Result.success(review);
    }

    /**
     * 获取当前登录用户的所有评价
     * @param request HttpServletRequest，用于获取用户ID
     * @return 评价列表
     */
    @GetMapping
    public Result<List<ReviewResponse>> getMyReviews(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("ReviewController.getMyReviews - 从request获取的userId: " + userId);

        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法获取评价列表");
            return Result.error("用户身份验证失败，请重新登录");
        }

        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
            return Result.success(reviews);
        } catch (Exception e) {
            System.err.println("获取评价列表异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据医生ID获取该医生的所有评价（公开接口，不需要登录）
     * @param doctorId 医生ID
     * @return 评价列表
     */
    @GetMapping("/doctor/{doctorId}")
    @com.example.hospital.common.PassToken // 公开接口，不需要登录
    public Result<List<ReviewResponse>> getReviewsByDoctorId(@PathVariable String doctorId) {
        System.out.println("ReviewController.getReviewsByDoctorId - doctorId: " + doctorId);

        if (doctorId == null || doctorId.isEmpty()) {
            return Result.error("医生ID不能为空");
        }

        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByDoctorId(doctorId);
            return Result.success(reviews);
        } catch (Exception e) {
            System.err.println("获取医生评价列表异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

