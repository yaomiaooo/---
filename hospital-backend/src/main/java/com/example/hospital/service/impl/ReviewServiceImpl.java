package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.CreateReviewRequest;
import com.example.hospital.dto.ReviewResponse;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Review;
import com.example.hospital.entity.Schedule;
import com.example.hospital.entity.Visit;
import com.example.hospital.mapper.AppointmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.ReviewMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.mapper.VisitMapper;
import com.example.hospital.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private VisitMapper visitMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * 生成唯一的评价ID（以R开头，长度不超过32）
     */
    private String generateUniqueReviewId() {
        String reviewId;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = String.format("%04d", (int)(Math.random() * 10000));
            reviewId = "R" + timestamp + random;

            Review existing = reviewMapper.selectById(reviewId);
            if (existing == null) {
                return reviewId;
            }

            attempts++;
            if (attempts >= maxAttempts) {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                reviewId = "R" + uuid.substring(0, 31);
                existing = reviewMapper.selectById(reviewId);
                if (existing == null) {
                    return reviewId;
                }
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "生成评价ID失败，请稍后重试");
            }
        } while (true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResponse createReview(String userId, CreateReviewRequest request) {
        // 1. 验证预约记录是否存在
        Appointment appointment = appointmentMapper.selectById(request.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "预约记录不存在");
        }

        // 2. 查找对应的就诊记录
        Visit visit = visitMapper.selectOne(
                new LambdaQueryWrapper<Visit>()
                        .eq(Visit::getAppointmentId, request.getAppointmentId())
        );

        if (visit == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该预约尚未完成就诊，无法评价");
        }

        // 3. 检查是否已经评价过
        Review existingReview = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getVisitId, visit.getVisitId())
        );

        if (existingReview != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该就诊记录已评价，请勿重复评价");
        }

        // 4. 验证评分范围
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "评分必须在1-5之间");
        }

        // 5. 创建评价记录
        Review review = new Review();
        // 获取医生ID
        String doctorId = null;
        if (appointment.getScheduleId() != null) {
            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            if (schedule != null) {
                doctorId = schedule.getDoctorId();
            }
        }

        review.setReviewId(generateUniqueReviewId());
        review.setVisitId(visit.getVisitId());
        // appointmentId 不在数据库表中，不需要设置
        review.setUserId(userId);
        review.setDoctorId(doctorId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setReviewTime(LocalDateTime.now());
        // ✅ 核心修复：设置分片键 hospitalId（从 visit 对象中获取）
        // 这是 ShardingSphere 分片路由的关键，必须设置，否则会报 "Sharding value can't be null" 错误
        review.setHospitalId(visit.getHospitalId());

        int insertResult = reviewMapper.insert(review);
        if (insertResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建评价失败，请重试");
        }

        // 6. 组装返回数据
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setVisitId(review.getVisitId());
        response.setAppointmentId(request.getAppointmentId()); // 从请求中获取
        response.setDoctorId(review.getDoctorId());
        response.setRating(review.getRating());
        response.setContent(review.getContent());
        response.setCreatedAt(review.getReviewTime()); // 从实体类的 reviewTime 字段获取

        return response;
    }

    @Override
    public ReviewResponse getReviewByAppointmentId(String appointmentId) {
        // 查找对应的就诊记录
        Visit visit = visitMapper.selectOne(
                new LambdaQueryWrapper<Visit>()
                        .eq(Visit::getAppointmentId, appointmentId)
        );

        if (visit == null) {
            return null;
        }

        // 查找评价
        Review review = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getVisitId, visit.getVisitId())
        );

        if (review == null) {
            return null;
        }

        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setVisitId(review.getVisitId());
        response.setAppointmentId(appointmentId); // 从参数中获取
        response.setDoctorId(review.getDoctorId());
        response.setRating(review.getRating());
        response.setContent(review.getContent());
        response.setCreatedAt(review.getReviewTime()); // 从实体类的 reviewTime 字段获取

        return response;
    }

    @Override
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        // ✅ 使用 JOIN 查询一次性获取所有相关信息（包括医生姓名）
        // 这样可以避免多次查询和关联，提高性能并确保数据完整性
        System.out.println("ReviewServiceImpl.getReviewsByUserId - 查询用户ID: " + userId);
        List<ReviewResponse> reviews = reviewMapper.selectReviewsWithDoctorByUserId(userId);
        
        System.out.println("ReviewServiceImpl.getReviewsByUserId - 查询到的评价数量: " + (reviews != null ? reviews.size() : 0));
        
        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyList();
        }

        // ✅ 额外去重：按 reviewId 去重，确保每个评价只出现一次
        // 使用 LinkedHashMap 保持顺序
        Map<String, ReviewResponse> uniqueReviews = new LinkedHashMap<>();
        for (ReviewResponse review : reviews) {
            if (review.getReviewId() != null && !uniqueReviews.containsKey(review.getReviewId())) {
                uniqueReviews.put(review.getReviewId(), review);
                System.out.println("ReviewServiceImpl.getReviewsByUserId - 添加评价: " + review.getReviewId() + ", 医生: " + review.getDoctorName());
            } else {
                System.out.println("ReviewServiceImpl.getReviewsByUserId - 跳过重复评价: " + review.getReviewId());
            }
        }

        System.out.println("ReviewServiceImpl.getReviewsByUserId - 去重后的评价数量: " + uniqueReviews.size());

        // 返回去重后的评价列表
        return new ArrayList<>(uniqueReviews.values());
    }

    @Override
    public List<ReviewResponse> getReviewsByDoctorId(String doctorId) {
        // ✅ 使用 JOIN 查询一次性获取该医生的所有评价（包括医生姓名）
        System.out.println("ReviewServiceImpl.getReviewsByDoctorId - 查询医生ID: " + doctorId);
        List<ReviewResponse> reviews = reviewMapper.selectReviewsByDoctorId(doctorId);
        
        System.out.println("ReviewServiceImpl.getReviewsByDoctorId - 查询到的评价数量: " + (reviews != null ? reviews.size() : 0));
        
        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyList();
        }

        // ✅ 额外去重：按 reviewId 去重，确保每个评价只出现一次
        Map<String, ReviewResponse> uniqueReviews = new LinkedHashMap<>();
        for (ReviewResponse review : reviews) {
            if (review.getReviewId() != null && !uniqueReviews.containsKey(review.getReviewId())) {
                uniqueReviews.put(review.getReviewId(), review);
                System.out.println("ReviewServiceImpl.getReviewsByDoctorId - 添加评价: " + review.getReviewId() + ", 医生: " + review.getDoctorName());
            } else {
                System.out.println("ReviewServiceImpl.getReviewsByDoctorId - 跳过重复评价: " + review.getReviewId());
            }
        }

        System.out.println("ReviewServiceImpl.getReviewsByDoctorId - 去重后的评价数量: " + uniqueReviews.size());

        // 返回去重后的评价列表
        return new ArrayList<>(uniqueReviews.values());
    }

}

