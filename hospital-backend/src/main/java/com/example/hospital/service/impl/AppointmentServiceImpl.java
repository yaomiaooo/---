package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.hospital.common.BusinessException;
import com.example.hospital.common.DateTimeUtil;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.dto.AppointmentResponse;
import com.example.hospital.dto.CreateAppointmentRequest;
import com.example.hospital.dto.MedicalRecordDTO;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Visit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.Schedule;
import com.example.hospital.entity.UserPatient;
import com.example.hospital.mapper.AppointmentMapper;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.HospitalMapper;
import com.example.hospital.mapper.PatientMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.mapper.UserPatientMapper;
import com.example.hospital.mapper.VisitMapper;
import com.example.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private UserPatientMapper userPatientMapper;

    @Autowired
    private VisitMapper visitMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 生成唯一的预约ID（以A开头，长度不超过32）
     * 格式：A + 时间戳（yyyyMMddHHmmss） + 随机数（4位）
     * 总长度：1 + 14 + 4 = 19 位（远小于32位限制）
     * 
     * @return 唯一的预约ID
     */
    private String generateUniqueAppointmentId() {
        String appointmentId;
        int maxAttempts = 10; // 最多尝试10次
        int attempts = 0;
        
        do {
            // 生成ID：A + 时间戳（14位） + 随机数（4位）
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = String.format("%04d", (int)(Math.random() * 10000));
            appointmentId = "A" + timestamp + random;
            
            // 检查ID是否已存在
            Appointment existing = appointmentMapper.selectById(appointmentId);
            if (existing == null) {
                return appointmentId; // ID唯一，返回
            }
            
            attempts++;
            if (attempts >= maxAttempts) {
                // 如果尝试10次都重复，使用UUID（去掉连字符）
                String uuid = UUID.randomUUID().toString().replace("-", "");
                // 取前31位，加上A前缀，总共32位
                appointmentId = "A" + uuid.substring(0, 31);
                // 再次检查唯一性
                existing = appointmentMapper.selectById(appointmentId);
                if (existing == null) {
                    return appointmentId;
                }
                // 如果还是重复，抛出异常
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "生成预约ID失败，请稍后重试");
            }
        } while (true);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public AppointmentResponse createAppointment(String userId, CreateAppointmentRequest request) {
        // 1. 验证就诊人是否属于当前用户
        UserPatient userPatientLink = userPatientMapper.selectOne(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
                        .eq(UserPatient::getPatientId, request.getPatientId())
        );

        if (userPatientLink == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该就诊人不属于当前用户，无法预约");
        }

        // 2. 验证排班是否存在且可用
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "排班信息不存在");
        }

        // 检查是否还有可用名额：totalQuota > bookedCount
        if (schedule.getTotalQuota() == null || schedule.getBookedCount() == null 
                || schedule.getTotalQuota() <= schedule.getBookedCount()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该排班时段已满，无法预约");
        }

        // 3. 检查该就诊人是否已有相同排班的预约（避免重复预约）
        Appointment existingAppointment = appointmentMapper.selectOne(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getPatientId, request.getPatientId())
                        .eq(Appointment::getScheduleId, request.getScheduleId())
                        .in(Appointment::getStatus, "BOOKED") // 只检查未就诊的预约
        );

        if (existingAppointment != null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该就诊人已预约该时段，请勿重复预约");
        }

        // 4. 验证院区ID是否匹配
        if (request.getHospitalId() == null || request.getHospitalId().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "院区ID不能为空");
        }

        // 5. 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(generateUniqueAppointmentId());
        appointment.setUserId(userId);
        appointment.setPatientId(request.getPatientId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setHospitalId(request.getHospitalId());
        appointment.setStatus("BOOKED"); // 初始状态为已预约
        // 使用统一的时间工具类，确保时区一致（Asia/Shanghai GMT+8）
        LocalDateTime now = DateTimeUtil.now();
        appointment.setCreatedAt(now);
        System.out.println("创建预约时间 - 本地时间: " + now + ", 时区: Asia/Shanghai (GMT+8)");

        int insertResult = appointmentMapper.insert(appointment);
        if (insertResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建预约失败，请重试");
        }

        // 6. 更新排班的已预约数量（bookedCount + 1）
        // 使用乐观锁更新，确保并发安全
        LambdaUpdateWrapper<Schedule> scheduleUpdateWrapper = new LambdaUpdateWrapper<>();
        scheduleUpdateWrapper.eq(Schedule::getScheduleId, request.getScheduleId())
                .setSql("booked_count = booked_count + 1");  // 使用 SQL 原子操作，避免并发问题
        int scheduleUpdateResult = scheduleMapper.update(null, scheduleUpdateWrapper);
        if (scheduleUpdateResult <= 0) {
            // 如果更新失败，回滚预约（事务会自动回滚）
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新号源数量失败，请重试");
        }

        System.out.println("预约创建成功 - appointmentId: " + appointment.getAppointmentId() + ", userId: " + userId + ", patientId: " + request.getPatientId() + ", scheduleId: " + request.getScheduleId());

        // 7. 组装返回数据（需要查询相关数据）
        return buildAppointmentResponse(appointment);
    }

    /**
     * 构建 AppointmentResponse 对象
     */
    private AppointmentResponse buildAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(appointment.getAppointmentId());
        response.setUserId(appointment.getUserId());
        response.setPatientId(appointment.getPatientId());
        response.setScheduleId(appointment.getScheduleId());
        response.setHospitalId(appointment.getHospitalId());
        response.setStatus(mapStatusToDisplay(appointment.getStatus()));
        response.setCreatedAt(appointment.getCreatedAt());

        // 查询就诊人信息
        Patient patient = patientMapper.selectById(appointment.getPatientId());
        if (patient != null) {
            response.setPatientName(patient.getPatientName());
        }

        // 查询排班信息
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            response.setVisitDate(schedule.getWorkDate() != null
                    ? schedule.getWorkDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : null);
            response.setTimeSlot(schedule.getTimeSlot());

            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
            if (doctor != null) {
                response.setDoctorId(doctor.getDoctorId());
                response.setDoctorName(doctor.getDoctorName());
                response.setDoctorTitle(doctor.getTitle());

                // 查询科室信息
                if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    if (department != null) {
                        response.setDepartmentName(department.getDepartmentName());
                    }
                }
            }
        }

        // 查询院区信息
        Hospital hospital = hospitalMapper.selectById(appointment.getHospitalId());
        if (hospital != null) {
            response.setHospitalName(hospital.getHospitalName());
        }

        // 计算挂号费
        if (response.getDoctorTitle() != null) {
            response.setPrice(calculatePriceByTitle(response.getDoctorTitle()));
        }

        return response;
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByUserId(String userId) {
        // 1. 查找该用户关联的所有就诊人
        List<UserPatient> userPatientLinks = userPatientMapper.selectList(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
        );

        if (userPatientLinks.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取所有就诊人ID
        List<String> userPatientIds = userPatientLinks.stream()
                .map(UserPatient::getPatientId)
                .collect(Collectors.toList());

        System.out.println("用户关联的就诊人ID列表: " + userPatientIds);

        // 3. 查询这些就诊人的所有预约记录
        List<Appointment> appointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .in(Appointment::getPatientId, userPatientIds)
                        .orderByDesc(Appointment::getCreatedAt)
        );

        System.out.println("查询到的预约记录数量: " + appointments.size());

        if (appointments.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 批量查询相关数据
        // 4.1 从预约记录中提取所有就诊人ID（可能包含不在 user_patient 中的，但应该都在）
        List<String> appointmentPatientIds = appointments.stream()
                .map(Appointment::getPatientId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        System.out.println("预约记录中的就诊人ID列表: " + appointmentPatientIds);
        
        // 查询所有就诊人信息（使用预约记录中的就诊人ID）
        List<Patient> patients = patientMapper.selectBatchIds(appointmentPatientIds);
        System.out.println("查询到的就诊人数量: " + patients.size());
        
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Patient> patientMap = patients.stream()
                .collect(Collectors.toMap(Patient::getPatientId, p -> p));
        
        // 打印就诊人映射信息用于调试
        patientMap.forEach((id, patient) -> {
            System.out.println("就诊人映射 - ID: " + id + ", 姓名: " + patient.getPatientName());
        });

        // 4.2 查询所有排班信息
        List<String> scheduleIds = appointments.stream()
                .map(Appointment::getScheduleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Schedule> scheduleMap;
        if (!scheduleIds.isEmpty()) {
            List<Schedule> schedules = scheduleMapper.selectBatchIds(scheduleIds);
            scheduleMap = schedules.stream()
                    .collect(Collectors.toMap(Schedule::getScheduleId, s -> s));
        } else {
            scheduleMap = Collections.emptyMap();
        }

        // 4.3 查询所有医生信息
        List<String> doctorIds = scheduleMap.values().stream()
                .map(Schedule::getDoctorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Doctor> doctorMap;
        if (!doctorIds.isEmpty()) {
            List<Doctor> doctors = doctorMapper.selectBatchIds(doctorIds);
            doctorMap = doctors.stream()
                    .collect(Collectors.toMap(Doctor::getDoctorId, d -> d));
        } else {
            doctorMap = Collections.emptyMap();
        }

        // 4.4 查询所有科室信息（通过医生的departmentId和hospitalId）
        List<String> departmentIds = doctorMap.values().stream()
                .map(Doctor::getDepartmentId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Department> departmentMap;
        if (!departmentIds.isEmpty()) {
            List<Department> departments = departmentMapper.selectBatchIds(departmentIds);
            departmentMap = departments.stream()
                    .collect(Collectors.toMap(Department::getDepartmentId, d -> d));
        } else {
            departmentMap = Collections.emptyMap();
        }

        // 4.5 查询所有院区信息
        List<String> hospitalIds = appointments.stream()
                .map(Appointment::getHospitalId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Hospital> hospitalMap;
        if (!hospitalIds.isEmpty()) {
            List<Hospital> hospitals = hospitalMapper.selectBatchIds(hospitalIds);
            hospitalMap = hospitals.stream()
                    .collect(Collectors.toMap(Hospital::getHospitalId, h -> h));
        } else {
            hospitalMap = Collections.emptyMap();
        }

        // 5. 检查并更新过期的预约记录
        // 在组装返回数据之前，先检查是否有过期的预约需要更新状态
        checkAndUpdateExpiredAppointments(appointments, scheduleMap);

        // 6. 组装返回数据
        // 使用 final 确保在 Lambda 中可以安全访问 appointments
        final List<Appointment> finalAppointments = appointments;
        return finalAppointments.stream()
                .map(appointment -> {
                    AppointmentResponse response = new AppointmentResponse();
                    response.setAppointmentId(appointment.getAppointmentId());
                    response.setPatientId(appointment.getPatientId());
                    response.setScheduleId(appointment.getScheduleId());
                    response.setHospitalId(appointment.getHospitalId());
                    // 将数据库状态转换为前端显示的状态
                    response.setStatus(mapStatusToDisplay(appointment.getStatus()));
                    response.setCreatedAt(appointment.getCreatedAt());

                    // 设置就诊人信息
                    Patient patient = patientMap.get(appointment.getPatientId());
                    if (patient != null) {
                        response.setPatientName(patient.getPatientName());
                        System.out.println("设置就诊人信息 - appointmentId: " + appointment.getAppointmentId() + ", patientId: " + appointment.getPatientId() + ", patientName: " + patient.getPatientName());
                    } else {
                        System.err.println("警告：未找到就诊人信息 - appointmentId: " + appointment.getAppointmentId() + ", patientId: " + appointment.getPatientId());
                        response.setPatientName("未知就诊人");
                    }

                    // 设置排班信息
                    Schedule schedule = scheduleMap.get(appointment.getScheduleId());
                    String departmentName = ""; // 初始化科室名称
                    if (schedule != null) {
                        response.setVisitDate(schedule.getWorkDate() != null 
                                ? schedule.getWorkDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                : null);
                        response.setTimeSlot(schedule.getTimeSlot());
                        
                        // 设置医生信息
                        Doctor doctor = doctorMap.get(schedule.getDoctorId());
                        if (doctor != null) {
                            response.setDoctorId(doctor.getDoctorId());
                            response.setDoctorName(doctor.getDoctorName());
                            response.setDoctorTitle(doctor.getTitle());
                            
                            // 通过医生的departmentId和hospitalId查询科室名称
                            if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
                                Department department = departmentMap.get(doctor.getDepartmentId());
                                if (department != null) {
                                    // 验证科室是否属于同一个院区（确保数据一致性）
                                    if (doctor.getHospitalId() != null && doctor.getHospitalId().equals(department.getHospitalId())) {
                                        departmentName = department.getDepartmentName();
                                    } else {
                                        System.err.println("警告：医生院区与科室院区不匹配 - doctorId: " + doctor.getDoctorId() + 
                                                         ", doctorHospitalId: " + doctor.getHospitalId() + 
                                                         ", departmentHospitalId: " + department.getHospitalId());
                                    }
                                }
                            }
                            
                            // 如果无法通过departmentId获取，尝试从医生职称或简介中提取（作为备用方案）
                            if ((departmentName == null || departmentName.isEmpty())) {
                                departmentName = extractDepartmentFromTitle(doctor.getTitle());
                                if (departmentName == null || departmentName.isEmpty()) {
                                    departmentName = extractDepartmentFromIntro(doctor.getDoctorIntro());
                                }
                            }
                        }
                    }
                    
                    response.setDepartmentName(departmentName != null ? departmentName : "");

                    // 设置院区信息
                    Hospital hospital = hospitalMap.get(appointment.getHospitalId());
                    if (hospital != null) {
                        response.setHospitalName(hospital.getHospitalName());
                    }

                    // 诊断结果暂时为空
                    response.setDiagnosis("");

                    // 计算挂号费（根据医生职称）
                    Double price = calculatePriceByTitle(response.getDoctorTitle());
                    response.setPrice(price);

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从医生职称中提取科室名称
     * 例如："心血管内科专家" -> "心血管内科"
     *      "神经外科主任" -> "神经外科"
     */
    private String extractDepartmentFromTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        // 常见的科室名称列表
        String[] departments = {
            "心血管内科", "神经外科", "呼吸内科", "消化内科", "骨科", "妇产科", "儿科",
            "眼科", "耳鼻喉科", "中医科", "全科医疗部", "口腔中心", "皮肤科",
            "心理咨询室", "预防保健科", "运动医学科", "急诊内科", "医学影像科",
            "健康体检中心", "药剂科", "急诊医学科", "全科", "口腔科", "急诊科"
        };
        
        for (String dept : departments) {
            if (title.contains(dept)) {
                return dept;
            }
        }
        
        return "";
    }

    /**
     * 从医生简介中提取科室名称
     * 例如："心血管内科专家，从医30年" -> "心血管内科"
     */
    private String extractDepartmentFromIntro(String intro) {
        if (intro == null || intro.trim().isEmpty()) {
            return "";
        }
        
        // 常见的科室名称列表
        String[] departments = {
            "心血管内科", "神经外科", "呼吸内科", "消化内科", "骨科", "妇产科", "儿科",
            "眼科", "耳鼻喉科", "中医科", "全科医疗部", "口腔中心", "皮肤科",
            "心理咨询室", "预防保健科", "运动医学科", "急诊内科", "医学影像科",
            "健康体检中心", "药剂科", "急诊医学科", "全科", "口腔科", "急诊科"
        };
        
        for (String dept : departments) {
            if (intro.contains(dept)) {
                return dept;
            }
        }
        
        return "";
    }

    /**
     * 根据医生职称计算挂号费
     * 医师：30元
     * 主任：50元
     * 专家：100元
     * 其他：30元
     */
    private Double calculatePriceByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return 30.0; // 默认30元
        }
        
        String titleLower = title.toLowerCase();
        if (titleLower.contains("专家")) {
            return 100.0;
        } else if (titleLower.contains("主任")) {
            return 50.0;
        } else if (titleLower.contains("医师")) {
            return 30.0;
        } else {
            return 30.0; // 其他情况默认30元
        }
    }

    /**
     * 检查并更新过期的预约记录
     * 如果当前时间超过了预约时间段的结束时间，且状态为BOOKED，则更新为NO_SHOW
     */
    private void checkAndUpdateExpiredAppointments(List<Appointment> appointments, Map<String, Schedule> scheduleMap) {
        // 使用统一的时间工具类，确保时区一致
        LocalDateTime now = DateTimeUtil.now();
        
        for (Appointment appointment : appointments) {
            // 只处理状态为BOOKED的预约
            if (!"BOOKED".equalsIgnoreCase(appointment.getStatus())) {
                continue;
            }
            
            // 获取排班信息
            Schedule schedule = scheduleMap.get(appointment.getScheduleId());
            if (schedule == null || schedule.getWorkDate() == null || schedule.getTimeSlot() == null) {
                continue;
            }
            
            // 解析时间段，获取结束时间
            LocalDateTime appointmentEndTime = parseAppointmentEndTime(schedule.getWorkDate(), schedule.getTimeSlot());
            if (appointmentEndTime == null) {
                continue;
            }
            
            // 如果当前时间超过了预约结束时间，则更新状态为NO_SHOW
            if (now.isAfter(appointmentEndTime)) {
                System.out.println("检测到过期预约 - appointmentId: " + appointment.getAppointmentId() + 
                                 ", 预约结束时间: " + appointmentEndTime + ", 当前时间: " + now);
                
                // 使用 LambdaUpdateWrapper 只更新 status 字段，避免触碰分片键
                LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Appointment::getAppointmentId, appointment.getAppointmentId())
                            .set(Appointment::getStatus, "NO_SHOW");
                
                int updateResult = appointmentMapper.update(null, updateWrapper);
                if (updateResult > 0) {
                    // 更新内存中的状态，以便后续返回正确的状态
                    appointment.setStatus("NO_SHOW");
                    System.out.println("预约状态已更新为已过期 - appointmentId: " + appointment.getAppointmentId());
                } else {
                    System.err.println("更新预约状态失败 - appointmentId: " + appointment.getAppointmentId());
                }
            }
        }
    }

    /**
     * 解析预约结束时间
     * timeSlot 格式：'08:00-10:00'，返回结束时间（日期 + 结束时间）
     */
    private LocalDateTime parseAppointmentEndTime(LocalDate workDate, String timeSlot) {
        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 解析时间段，格式：'08:00-10:00'
            String[] parts = timeSlot.split("-");
            if (parts.length != 2) {
                System.err.println("时间段格式错误: " + timeSlot);
                return null;
            }
            
            // 获取结束时间
            String endTimeStr = parts[1].trim();
            LocalTime endTime = LocalTime.parse(endTimeStr);
            
            // 组合日期和时间
            return LocalDateTime.of(workDate, endTime);
        } catch (Exception e) {
            System.err.println("解析预约结束时间失败: " + timeSlot + ", 错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将数据库状态转换为前端显示的状态
     * BOOKED -> 未就诊
     * COMPLETED -> 已就诊
     * CANCELLED -> 已取消
     * NO_SHOW -> 已过期
     */
    private String mapStatusToDisplay(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "未知";
        }
        
        switch (status.toUpperCase()) {
            case "BOOKED":
                return "未就诊";
            case "COMPLETED":
                return "已就诊";
            case "CANCELLED":
                return "已取消";
            case "NO_SHOW":
                return "已过期";
            default:
                return status;
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean cancelAppointment(String userId, String appointmentId) {
        // 1. 查询预约记录
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "预约记录不存在");
        }

        // 2. 验证该预约是否属于当前用户
        // 通过 user_patient 表验证该预约的就诊人是否属于当前用户
        List<UserPatient> userPatientLinks = userPatientMapper.selectList(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
                        .eq(UserPatient::getPatientId, appointment.getPatientId())
        );

        if (userPatientLinks.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该预约不属于当前用户，无法取消");
        }

        // 3. 验证预约状态，只有"未就诊"（BOOKED）状态的预约才能取消
        if (!"BOOKED".equalsIgnoreCase(appointment.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有未就诊的预约才能取消");
        }

        // 4. 更新预约状态为 CANCELLED
        // ⚠️ 重要：使用 LambdaUpdateWrapper 只更新 status 字段，避免触碰分片键 hospital_id
        // 如果使用 updateById，MyBatis-Plus 会尝试更新所有字段（包括分片键），导致 ShardingSphere 报错
        LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Appointment::getAppointmentId, appointmentId)
                    .set(Appointment::getStatus, "CANCELLED");
        
        int updateResult = appointmentMapper.update(null, updateWrapper);

        if (updateResult <= 0) {
            System.err.println("预约取消失败 - appointmentId: " + appointmentId);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "取消预约失败，请重试");
        }

        // 5. 更新排班的已预约数量（bookedCount - 1）
        // 使用 SQL 原子操作，确保并发安全，并防止 bookedCount 变成负数
        LambdaUpdateWrapper<Schedule> scheduleUpdateWrapper = new LambdaUpdateWrapper<>();
        scheduleUpdateWrapper.eq(Schedule::getScheduleId, appointment.getScheduleId())
                .setSql("booked_count = GREATEST(0, booked_count - 1)");  // 使用 SQL 原子操作，确保不会小于 0
        int scheduleUpdateResult = scheduleMapper.update(null, scheduleUpdateWrapper);
        if (scheduleUpdateResult <= 0) {
            // 如果更新失败，记录警告但不影响取消预约操作（因为预约状态已经更新）
            System.err.println("警告：更新排班号源数量失败 - scheduleId: " + appointment.getScheduleId());
        }

        System.out.println("预约取消成功 - appointmentId: " + appointmentId + ", userId: " + userId + ", scheduleId: " + appointment.getScheduleId());
        return true;
    }

    @Override
    public Page<MedicalRecordDTO> getMedicalRecords(String userId, String month, int page, int pageSize) {
        Page<MedicalRecordDTO> pageResult = new Page<>(page, pageSize);

        // 首先通过userId找到对应的doctorId
        Doctor doctor = doctorMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Doctor>()
                .eq("user_id", userId)
        );

        if (doctor == null) {
            pageResult.setRecords(new java.util.ArrayList<>());
            pageResult.setTotal(0);
            return pageResult;
        }

        String doctorId = doctor.getDoctorId();

        // 然后找到该医生的所有预约ID，避免复杂的跨表查询
        List<String> appointmentIds = appointmentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Appointment>()
                .inSql("schedule_id", "SELECT schedule_id FROM schedule WHERE doctor_id = '" + doctorId + "'")
                .select("appointment_id")
        ).stream().map(Appointment::getAppointmentId).collect(java.util.stream.Collectors.toList());

        if (appointmentIds.isEmpty()) {
            pageResult.setRecords(new java.util.ArrayList<>());
            pageResult.setTotal(0);
            return pageResult;
        }

        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.in("appointment_id", appointmentIds);

        // 月份筛选
        if (month != null && !month.trim().isEmpty()) {
            queryWrapper.apply("DATE_FORMAT(visit_time, '%Y-%m') = '" + month + "'");
        }

        // 分页查询visit记录
        Page<Visit> visitPage = visitMapper.selectPage(new Page<>(page, pageSize), queryWrapper);

        // 转换为DTO
        Page<MedicalRecordDTO> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(visitPage.getTotal());

        java.util.List<MedicalRecordDTO> records = new java.util.ArrayList<>();
        for (Visit visit : visitPage.getRecords()) {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setRecordId(visit.getVisitId());
            dto.setVisitDate(visit.getVisitTime().toLocalDate());
            dto.setSummary(visit.getDiagnosis());

            // 从appointment表获取更多信息
            Appointment appointment = appointmentMapper.selectBasicById(visit.getAppointmentId());
            if (appointment != null) {
                dto.setOrderNo(appointment.getAppointmentId());
                dto.setStatus(appointment.getStatus());

                // 获取患者信息
                Patient patient = patientMapper.selectById(appointment.getPatientId());
                if (patient != null) {
                    dto.setPatientName(patient.getPatientName());
                    dto.setPatientPhone(patient.getPatientPhone());
                }

                // 获取排班信息（时间段）
                Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
                if (schedule != null) {
                    dto.setTimeSlot(schedule.getTimeSlot());

                    // 获取医生信息（科室）
                    Doctor scheduleDoctor = doctorMapper.selectById(schedule.getDoctorId());
                    if (scheduleDoctor != null && scheduleDoctor.getDepartmentId() != null) {
                        // 通过departmentId获取部门名称
                        Department department = departmentMapper.selectById(scheduleDoctor.getDepartmentId());
                        if (department != null) {
                            dto.setDepartmentName(department.getDepartmentName());
                        } else {
                            dto.setDepartmentName("");
                        }
                    } else {
                        dto.setDepartmentName("");
                    }
                } else {
                    dto.setTimeSlot("");
                }
            } else {
                dto.setOrderNo("");
                dto.setStatus("");
            }

            records.add(dto);
        }

        resultPage.setRecords(records);
        return resultPage;
    }

    // ========== 医生端API方法实现 ==========

    @Override
    public List<Appointment> getTodayAppointmentsByDoctorId(String doctorId, LocalDate today) {
        List<Appointment> appointments = appointmentMapper.selectTodayAppointmentsByDoctorId(doctorId, today);

        // 为每个appointment添加patient信息
        for (Appointment appointment : appointments) {
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient != null) {
                // 动态添加patient信息到appointment对象
                appointment.setPatientName(patient.getPatientName());
                appointment.setPatientGender(patient.getPatientGender());
                appointment.setPatientBirthday(patient.getPatientBirthday());
            }
        }

        return appointments;
    }

    @Override
    public List<AppointmentDetailDTO> getAppointmentWithDetails(String appointmentId) {
        List<AppointmentDetailDTO> appointments = appointmentMapper.selectAppointmentWithDetails(appointmentId);

        // 为每个appointment添加patient信息
        for (AppointmentDetailDTO appointment : appointments) {
            if (appointment != null) {
                // 单独查询patient信息
                Patient patient = patientMapper.selectById(appointment.getPatientId());
                if (patient != null) {
                    appointment.setPatientName(patient.getPatientName());
                    appointment.setPatientGender(patient.getPatientGender());
                    appointment.setPatientBirthday(patient.getPatientBirthday());
                    appointment.setPatientPhone(patient.getPatientPhone());
                }
            }
        }
        return appointments;
    }

    @Override
    public Appointment updateAppointmentStatus(String appointmentId, String status) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);
        if (appointments.size() == 1) {
            Appointment appointment = appointments.get(0);
            appointment.setStatus(status);
            appointmentMapper.updateAppointmentStatus(status, appointmentId);
            return appointment;
        }
        return null;
    }

    @Override
    public boolean startConsultation(String appointmentId, String visitTimeStr) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);
        Appointment appointment = null;

        if (appointments.size() == 1) {
            appointment = appointments.get(0);
        } else if (appointments.size() > 1) {
            // 如果有多条记录，选择状态为BOOKED的
            for (Appointment app : appointments) {
                if ("BOOKED".equals(app.getStatus())) {
                    appointment = app;
                    break;
                }
            }
        }

        if (appointment != null && "BOOKED".equals(appointment.getStatus())) {
            // 检查是否已存在visit记录
            List<Visit> existingVisits = visitMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit>()
                    .eq("appointment_id", appointmentId)
            );

            if (existingVisits.isEmpty()) {
                // 解析前端传递的本地时间字符串
                LocalDateTime visitTime = parseLocalDateTimeString(visitTimeStr);

                // 创建就诊记录
                Visit visit = new Visit();
                visit.setVisitId(UUID.randomUUID().toString().replace("-", ""));
                visit.setAppointmentId(appointmentId);
                visit.setHospitalId(appointment.getHospitalId());
                visit.setVisitTime(visitTime); // 设置为排班的工作日期时间
                visit.setDiagnosis(""); // 初始诊断为空

                visitMapper.insert(visit);
            }
            // 如果已存在visit记录，保持不变（可能是重新开始接诊）

            return true;
        }
        return false;
    }

    @Override
    public boolean completeConsultation(String appointmentId, String patientId, String diagnosis) {
        List<Appointment> appointments = appointmentMapper.selectByAppointmentId(appointmentId);
        Appointment appointment = null;
        if (appointments.size() == 1) {
            appointment = appointments.get(0);
        } else if (appointments.size() > 1) {
            // 如果有多条记录，根据patientId选择正确的记录
            for (Appointment app : appointments) {
                if (patientId.equals(app.getPatientId())) {
                    appointment = app;
                    break;
                }
            }
            // 如果没找到匹配的patientId，回退到原来的逻辑
            if (appointment == null) {
                for (Appointment app : appointments) {
                    if ("BOOKED".equals(app.getStatus())) {
                        appointment = app;
                        break;
                    }
                }
            }
        }

        if (appointment != null && ("BOOKED".equals(appointment.getStatus()) || "COMPLETED".equals(appointment.getStatus()))) {
            // 如果是BOOKED状态，先更新为COMPLETED
            if ("BOOKED".equals(appointment.getStatus())) {
                appointmentMapper.updateAppointmentStatus("COMPLETED", appointmentId);
            }

            // 更新visit记录的诊断结果（visit记录应该在startConsultation时已创建）
            List<Visit> existingVisits = visitMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit>()
                    .eq("appointment_id", appointmentId)
            );

            if (!existingVisits.isEmpty()) {
                // 更新现有记录，只更新诊断结果，保持visit_time不变
                Visit existingVisit = existingVisits.get(0);
                visitMapper.updateDiagnosis(diagnosis, existingVisit.getVisitId());
            } else {
                // 如果没有visit记录，说明startConsultation没有正常执行，这里作为兜底处理
                Visit visit = new Visit();
                visit.setVisitId(UUID.randomUUID().toString().replace("-", ""));
                visit.setAppointmentId(appointmentId);
                visit.setHospitalId(appointment.getHospitalId());
                visit.setVisitTime(LocalDateTime.now()); // 兜底情况下设置为当前时间（使用系统默认时区）
                visit.setDiagnosis(diagnosis);

                visitMapper.insert(visit);
            }
            return true;
        }
        return false;
    }

    @Override
    public String getDiagnosis(String appointmentId) {
        // 从visit表中查询诊断结果
        List<Visit> visits = visitMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Visit>()
                .eq("appointment_id", appointmentId)
                .orderByDesc("visit_time")
                .last("LIMIT 1")
        );

        if (!visits.isEmpty()) {
            return visits.get(0).getDiagnosis();
        }
        return null;
    }

    /**
     * 解析前端传递的本地时间字符串
     * 前端格式：2025/12/25 23:54:26
     * @param timeStr 时间字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseLocalDateTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            // 前端发送的是浏览器本地时间字符串（格式：2025/12/25 23:54:26）
            // 我们假设前端发送的是用户的本地时间（通常是Asia/Shanghai时区）
            String standardFormat = timeStr.replace("/", "-");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 解析为LocalDateTime（表示本地时间）
            LocalDateTime localTime = LocalDateTime.parse(standardFormat, formatter);

            // 转换为ZonedDateTime，指定为Asia/Shanghai时区
            java.time.ZonedDateTime zonedTime = localTime.atZone(java.time.ZoneId.of("Asia/Shanghai"));

            // 转换为UTC时间存储到数据库
            return zonedTime.withZoneSameInstant(java.time.ZoneId.of("UTC")).toLocalDateTime();

        } catch (Exception e) {
            System.err.println("解析时间字符串失败: " + timeStr + ", 使用当前时间");
            e.printStackTrace();
            // 返回当前UTC时间
            return LocalDateTime.now(java.time.ZoneOffset.UTC);
        }
    }
}

